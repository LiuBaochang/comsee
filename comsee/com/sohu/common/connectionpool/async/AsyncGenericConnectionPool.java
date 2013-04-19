/*
 * Created on 2006-11-24
 *
 */
package com.sohu.common.connectionpool.async;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.Selector;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;

/**
 * ���ӳ�
 * 
 * 1. �ṩ��������. ����������,���ⲻ�����κεĴ�����Ϣ.
 * 2. ����������. ������������ַ.
 * 3. ֧�ֶ��߳�
 * 
 * @author LiuMingzhu (mingzhuliu@sohu-inc.com)
 * 
 */
public abstract class AsyncGenericConnectionPool extends ServerConfig {

    // / random
    protected static final Random random                   = new Random();

    // / ���������״̬��Ϣ
    protected ServerStatus[]      status;
    // / socket����ʧ��ʱ�����Զ�ѡ��һ��������ӣ����Ʒ��inplaceConnectionLife��query���Զ��Ͽ�
    protected int                 inplaceConnectionLife    = 500;

    Selector                      selector;

    protected Receiver            recver;
    protected Sender              sender;
    protected Checker             checker;

    protected Object              recverLock               = new Object();
    protected Object              senderLock               = new Object();

    protected AsyncClientFactory  factory;

    /**
     * ��AsyncGenericConnectionPool����������cache serverʱ��������ĳһ������������£��Զ�������ת����һ������һ���Ĺ���ת�򣬱�֤��ͬ�������Դ���ͬ��Server�ϣ�
     * �������Ĭ���ǿ�����
     * ���AsyncGenericConnectionPool��������DB Serverʱ��ĳһ�����ˣ��ٻ���Ļ�Ҳû�ã���������¿��������ཫ�˹��ܹص�
     */
    private boolean               isAutoSwitchToNextServer = true;

    /**
     * ������ʵ��
     */
    protected AsyncGenericConnectionPool(AsyncClientFactory factory, String name) {
        this.factory = factory;
        if (name != null) {
            this.name = name;
        }
    }

    public void init() throws Exception {

        ArrayList servers = new ArrayList();

        if (this.servers == null)
            throw new IllegalArgumentException("config is NULL");

        String[] list = pat.split(this.servers);

        for (int i = 0; i < list.length; i++) {
            ServerStatus ss = new ServerStatus(list[i], this);
            servers.add(servers.size(), ss);
        }

        ServerStatus[] serverStatus = (ServerStatus[]) servers.toArray(new ServerStatus[servers.size()]);

        selector = Selector.open();

        this.status = serverStatus;

        recver = new Receiver(this);
        sender = new Sender(this);

        recver.startThread();
        sender.startThread();

        if (this.maxResponseTime > 0) {
            checker = new Checker(this);
            checker.startThread();
        }
    }

    /**
     * ��ü�¼��ʵ��
     * 
     * @return
     */
    protected abstract Log getLogger();

    public int sendRequest(AsyncRequest request) {

        if (request == null) {
            return -1;
        }

        if (!request.isValid()) {
            request.illegalRequest();
            return -2;
        }

        int serverCount = this.getServerIdCount();
        int ret = request.getServerId(serverCount);

        if (!isServerAvaliable(ret) && request.clonableRequest && request.connectType == AsyncRequest.NORMAL_REQUEST
                && isServerShouldRerty(ret)) {
            // debug bart
            System.out.println("[pool " + request.ruid + "]Retry server " + getStatus(ret).serverInfo);
            // ����һ���������󣬱ض����͸�����
            AsyncRequest req = request.clone();
            req.connectType = AsyncRequest.RETRY_REQUEST;
            ServerStatus ss = getStatus(ret);
            if (ss != null) {
                ss.retryCount++;
            }
            sendRequest(req);
        }

        // �����ǰ��������ͨ�����ҷֻ���Ŀ�������down�ˣ�����isAutoSwitchToNextServer����Ϊ���Զ�ѡ����һ�����û�����ֱ�ӷ��ش���
        if (!isServerAvaliable(ret) && request.connectType == AsyncRequest.NORMAL_REQUEST && !isAutoSwitchToNextServer) {
            request.serverDown("No server available, and no alternatives will be picked");
            return -1;
        }

        /**
         * ���´��������Ѱ��һ�����ܵ���һ�������������������͹�ȥ����������������������
         * 1. ��ǰ������һ��Ӱ�����󣨼���ServerStatus��Ϊ�������״̬�������ģ����������󲻷�������Ӧ�÷���Ŀ�꣬������һ�����õķ�����
         * 2. ԭ��������ͨ���󣬵���Ŀ���cache server��available������Ҫ��ѡ��һ�����õĻ�����������
         * Ѱ����һ��Ч���Ĳ��Կ��Ա�֤��down����server���������£���ͬ��ѯ����Ŀ����ֲ��������ֻ��ռ䣬��ͬ����ѯ��Ŀ���ǹ̶��ġ�
         * 
         * ���ڵ�2����������������pool���ӵ�Ŀ���Ƿ�cache�ģ�����ͨ����isAutoSwitchToNextServer��Ϊfalse���ص��Զ�Ѱ����һ���Ĺ��ܣ�
         * ��ʱ���ԭ�����Ŀ��server���ˣ�ֱ�ӷ���ʧ�ܡ�
         */
        if (request.connectType == AsyncRequest.SHADOW_NORMAL_REQUEST
                || request.connectType == AsyncRequest.SHADOW_QUEUE_REQUEST ||
                (!isServerAvaliable(ret) && request.connectType != AsyncRequest.RETRY_REQUEST)) {

            // System.out.println("server is not avaliable");
            int avaliableServerCount = 0;
            for (int i = 0; i < getServerIdCount(); i++) {
                if (isServerAvaliable(i)) {
                    avaliableServerCount++;
                }
            }

            // ����Ӱ�����󣬲��ܷ��͸�����
            if ((request.connectType == AsyncRequest.SHADOW_NORMAL_REQUEST || request.connectType == AsyncRequest.SHADOW_QUEUE_REQUEST)
                    && isServerAvaliable(ret)) {
                avaliableServerCount--;
            }

            if (avaliableServerCount <= 0) {
                request.serverDown("��ǰ�޿���server");
                return -1;
            }

            // ���Դ���.
            int inc = (request.getServerId(avaliableServerCount)) + 1;

            int finalIndex = ret;

            int i = 0;
            do {
                int j = 0;
                boolean find = false;
                do {
                    finalIndex = (finalIndex + 1) % serverCount;
                    if (isServerAvaliable(finalIndex) && (finalIndex != ret)) {
                        find = true;
                        break;
                    }
                    j++;
                } while (j < serverCount);

                if (!find) {
                    request.serverDown("����޿���server");
                    return -1;
                }

                i++;
            } while (i < inc);

            ret = finalIndex;
        }

        int serverId = ret;

        if (serverId < 0 || serverId >= this.getServerIdCount()) {
            request.serverDown("ServerId�������");
            return -1;
        }

        ServerStatus ss = getStatus(serverId);

        if (ss == null) {
            request.serverDown("���ܻ�ȡserver״̬");
            return -2;
        }

        request.setServer(ss);
        request.setServerInfo(ss.getServerInfo());
        request.queueSend();
        sender.senderSendRequest(request);

        return 0;
    }

    /**
     * ��ĳ̨server��ʱ���𣬲������䷢������
     * ���߽�ĳ̨server���¼���
     * 
     * @param ip
     *            :������ip���ַ, action:�������
     * @return �ɹ����
     */
    public boolean holdServer(String ip, boolean action) {
        String addr = null;
        int i = 0;
        for (; i < getServerIdCount(); i++) {
            addr = status[i].addr.toString();
            if (addr.indexOf(ip) >= 0) {
                break;
            }
        }
        return holdServer(i, action);
    }

    /**
     * ��ĳ̨server��ʱ���𣬲������䷢������
     * ���߽�ĳ̨server���¼���
     * 
     * @param key
     *            :��������key, action:�������
     * @return �ɹ����
     */
    public boolean holdServer(int key, boolean action) {
        ServerStatus ss = null;
        if (status != null && key >= 0 && key < status.length) {
            ss = status[key];
        }
        StringBuffer sb = new StringBuffer();
        
        if (ss == null) {
        	sb.append("swithed nothing");
        	System.out.println(sb.toString());
            return false;
        }

        ss.swithcer = action;
        
        
        try{
			sb.append(ss.addr.toString());
			sb.append(" is switched ");
			sb.append(action?"on":"off");
			sb.append(" at ");
			sb.append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
        }catch(Exception e){
        	sb.append("swithed error");
        }
        System.out.println(sb.toString());

		return true;
    }

    /**
     * @return
     */
    public int getServerIdBits() {
        return 0;
    }

    /**
     * @return
     */
    public int getServerIdMask() {
        return 0;
    }

    /**
     * @param i
     * @return ��į������
     */
    public InetSocketAddress getServer(int i) {
        return status[i].getAddr();
    }

    /**
     * @return Returns the inplaceConnectionLife.
     */
    public int getInplaceConnectionLife() {
        return inplaceConnectionLife;
    }

    /**
     * @param inplaceConnectionLife
     *            The inplaceConnectionLife to set.
     */
    public void setInplaceConnectionLife(int inplaceConnectionLife) {
        this.inplaceConnectionLife = inplaceConnectionLife;
    }

    private static Pattern pat = Pattern.compile("\\s+");

    public ServerStatus[] getAllStatus() {
        return status;
    }

    /**
     * ����ָ����ŵķ�������״̬����.
     * 
     * @param i
     * @return ���ָ����ŵķ�����������,�򷵻�null
     */
    public ServerStatus getStatus(int i) {
        if (status != null
                && i >= 0
                && i < status.length) {
            return status[i];
        }
        else {
            return null;
        }
    }

    public boolean isServerShouldRerty(int i) {
        ServerStatus ss = null;
        if (status != null && i >= 0 && i < status.length) {
            ss = status[i];
        }
        if (ss == null) {
            return false;
        }

        return ss.isServerShouldRerty();
    }

    public boolean isServerAvaliable(int i) {
        ServerStatus ss = null;
        if (status != null && i >= 0 && i < status.length) {
            ss = status[i];
        }
        if (ss == null) {
            return false;
        }

        boolean ret = ss.isServerAvaliable();
        if (!ret) {
            Log logger = getLogger();
            if (logger != null && logger.isTraceEnabled())
                logger.trace("server is not avaliable:" + ss.getServerInfo());
        }
        return ret;
    }

    /**
     * �������i��Ӧ�ķ����������ӳ��ж�Ӧ�ļ�ֵ.
     * �����Ӧ�ķ������Ƿ�(������),�򷵻�null;
     * 
     * @param i
     * @return
     */
    public Object getServerKey(int i) {
        if (status != null
                && i >= 0
                && i < status.length
                && status[i] != null
                && status[i].key != null) {
            return status[i].key;
        }
        else {
            return null;
        }
    }

    /**
     * ������ע���Ŀ�����������
     * 
     * @return
     */
    public int getServerIdCount() {
        if (status == null) {
            return 0;
        }
        else {
            return status.length;
        }
    }

    public InetSocketAddress getSocketAddress(int i) {
        if (status == null
                || i < 0
                || i >= status.length
                || status[i] == null) {
            return null;
        }
        else {
            return status[i].getAddr();
        }
    }

    public void finalize() {
        destroy();
    }

    public void destroy() {
        sender.stopThread();
        sender = null;
        recver.stopThread();
        recver = null;
        ServerStatus[] temp = status;
        status = null;
        if (temp != null) {
            for (int i = 0; i < temp.length; i++) {
                ServerStatus ss = temp[i];
                if (ss == null)
                    continue;
                ss.destroy();
            }
        }
        try {
            this.selector.close();
        }
        catch (IOException e) {

        }
    }

    public String status() {
        StringBuffer sb = new StringBuffer();
        sb.append("\nPool Status: ");
        sb.append(this.getName());
        sb.append('\n');

        for (int i = 0; i < this.status.length; i++) {
            status[i].status(sb);
        }

        if (getLogger().isInfoEnabled()) {
            getLogger().info(sb.toString());
        }
        return sb.toString();
    }

    protected boolean getIsAutoSwitchToNextServer() {
        return this.isAutoSwitchToNextServer;
    }

    protected void setAutoSwitchToNextServer(boolean isAutoSwitchToNextServer) {
        this.isAutoSwitchToNextServer = isAutoSwitchToNextServer;
    }

}
