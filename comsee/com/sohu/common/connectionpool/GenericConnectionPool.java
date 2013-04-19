/*
 * Created on 2003-11-24
 *
 */
package com.sohu.common.connectionpool;

import java.io.IOException;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.SocketTimeoutException;
//import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.Random;

import org.apache.commons.logging.Log;
import org.apache.commons.pool.KeyedObjectPool;
import org.apache.commons.pool.KeyedPoolableObjectFactory;
import org.apache.commons.pool.impl.GenericKeyedObjectPool;

//import com.sohu.websearch.Request;
//import com.sohu.websearch.Result;
//import com.sohu.websearch.ServerConfig;
//import com.sohu.websearch.ServerStatus;

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
public abstract class GenericConnectionPool {

	private Log logger = getLogger();
	
	/// random
	protected static final Random random = new Random();
	
	/// ���������״̬��Ϣ
	protected ServerStatus[] servers ;
	protected ServerConfig serverConfig = new ServerConfig();
	///	socket����ʧ��ʱ�����Զ�ѡ��һ��������ӣ����Ʒ��inplaceConnectionLife��query���Զ��Ͽ�
//	protected int inplaceConnectionLife = 500;
	///	client��ʱ�ĺ�����
	private int timeOutMillseconds = 5000;
	/// client���ӳ�ʱ������
	private int connectTimeOutMillseconds = 100;

	/// ���ӳ�
	private KeyedObjectPool pool  =null ;
	
	/**
	 * ������ʵ��
	 */
	protected GenericConnectionPool(KeyedPoolableObjectFactory factory, GenericKeyedObjectPool.Config config){
		this.pool= new GenericKeyedObjectPool( factory, config);
	}
	/**
	 * ��������ѡ�����.��Cache����������hashֵ���������
	 * @param obj
	 * @return
	 */
	public abstract int getServerId(Object obj);
	/**
	 * ��ü�¼��ʵ��
	 * @return
	 */
	protected abstract Log getLogger();

	/* (non-Javadoc)
	 * @see com.sohu.websearch.pool.SetServersAble#setServers(java.lang.String)
	 */
	public void setServers(String multiservers) throws IllegalArgumentException {
		
		serverConfig.initServerConfig(multiservers);
		
		synchronized (this){

			this.servers = serverConfig.getAllStatus();

		}
	}

	/**
	 * ȷ�������ѽ���
	 * ע�⣬���Ӳ��Բ�ͬʱ,��Ҫ���ش˺�����
	 * �������е����Ӳ���:
	 * ��Ӧ���������޷�����,�����ѡ����һ������������������.
	 * 1. ���ѡ��ķ�����������ԭ���ķ�����,����Ҫ����,�����������,���β��ɹ�,�͵�������.
	 * 2. ������ѡ��ķ��������ӳɹ�,���趨һ���Ƚ϶̵�����ʱ��.
	 * 3. ������ѡ��ķ�����Ҳ���ɹ�,���쳣���ظ�������
	 * @param request
	 * @return
	 * @throws IOException
	 */
	protected void ensureConnection( int serverId , QueryClient client)
			throws IOException
			{
		if ( client.isValid() ) return;
		
		client.connect(	servers[serverId].getAddr(), getConnectTimeoutMillis(),getSocketTimeoutMillis() );
		client.setLife(Integer.MAX_VALUE);
	}
	/**
	 * ִ�в�ѯ
	 * @param request
	 * @return
	 * @throws IOException
	 */
	public Result query(Request request) 
	{
		int serverId = getServerId( request );
		
		if( serverId <0 || serverId>=this.getServerIdCount() ) return null;
		ServerStatus serverStatus = servers[serverId];
		
		request.setServerInfo( serverStatus.getServerInfo() );

		if( logger!=null && logger.isDebugEnabled() ){
			logger.debug("should connect to " + serverId + "th server " + request.getRequestId());
		}
		long timeAfterError =
			System.currentTimeMillis() - serverStatus.getDowntime();
		//	��������δ������Ϣһ��ʱ��
		if( ! serverConfig.isServerAvaliable(serverId) ){
			
			if( logger!=null && logger.isDebugEnabled() ){
				logger.debug("recovering from socket Error("+ timeAfterError+ "ms before) " + request.getRequestId());
			}
			return null;
		}

		QueryClient client = null;
		boolean success = false;
		// ��ѯ���
		Result result = null;
		try {
			client = (QueryClient) pool.borrowObject( serverStatus.getKey() );
		} catch ( NoSuchElementException e ){
			serverStatus.queueTimeout();
			if( logger.isErrorEnabled() ){
				logger.error("BORROW_OBJECT_FAILED", e);
			}
		} catch ( Exception e){
			serverStatus.queueTimeout();
			if( logger.isErrorEnabled() ){
				logger.error("BORROW_OBJECT_FAILED UNSPEC", e);
			}
		}

		// ����Ƿ��õ�����
		if( client == null ) return null;
		
		try{
			ensureConnection( serverId, client);
			result = client.query(request);
			success = true;
			serverStatus.success();
		} catch (ConnectException e) {
			request.serverDown();
			serverStatus.connectTimeout();
			if( logger!=null && logger.isWarnEnabled() ){
				logger.warn( "CONN_FAIL : KEY: "+ serverStatus.getKey()+ " : " + request.getRequestId(), e);
			}
		} catch (SocketTimeoutException e) {
			request.socketTimeout();
			serverStatus.transferTimeout();
			if( logger!=null && logger.isWarnEnabled() ){
				logger.warn( "TRANS_TIMEOUT : KEY: "+ serverStatus.getKey()+ " : " + request.getRequestId(), e);
			}
		} catch( IOException e ){
			request.serverDown();
			serverStatus.transferTimeout();
			if( logger!=null && logger.isWarnEnabled() ){
				logger.warn( "unexpected exception : " + request.getRequestId(), e);
			}
		}finally {
			if ( !success ){
				client.close();
			}
			try{
				returnClient( serverId , client);
			}catch(Exception e){
				if( logger!=null && logger.isWarnEnabled() ){
					logger.warn( "unexpected exception : " + request.getRequestId(), e);
				}
			}
			client = null;
		}
		return result;
	}

	/**
	 * @return		ÿ��������������ٸ�����
	 */
	public int getMaxConnectionsPerServer() {
		return ((GenericKeyedObjectPool) pool).getMaxActive();
	}

	/**
	 * @param c		ÿ��������������ٸ�����
	 */
	public void setMaxConnectionsPerServer(int c) {
		((GenericKeyedObjectPool) pool).setMaxActive(c);
	}

	/**
	 * @return
	 */
	public int getServerIdCount() {
		if( this.servers == null ){
			return 0;
		} else {
			return servers.length;
		}
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
	 * @return
	 */
	public long getSleepMillisecondsAfterTimeOutError() {
		return serverConfig.getSleepMillisecondsAfterTimeOutError();
	}

	/**
	 * @param i
	 */
	public void setSleepMillisecondsAfterTimeOutError(int i) {
		serverConfig.setSleepMillisecondsAfterTimeOutError( i );
	}

	/**
	 * @return
	 */
	public int getTimeOutMillseconds() {
		return getSocketTimeoutMillis();
	}

	/**
	 * @param i
	 */
	public void setTimeOutMillseconds(int i) {
		setSocketTimeoutMillis(i);
	}

	/**
	 * @param i
	 */
	public void setPoolTimeOutMillseconds(long i) {
		((GenericKeyedObjectPool) pool).setMaxWait(i);
	}

	/**
	 * @return
	 */
	public int getMaxErrorsBeforeSleep() {
		return serverConfig.getMaxErrorsBeforeSleep();
	}

	/**
	 * @param i
	 */
	public void setMaxErrorsBeforeSleep(int i) {
		serverConfig.setMaxErrorsBeforeSleep(i);
	}

	/**
	 * @param i
	 */
	public long getPoolTimeOutMillseconds() {
		return ((GenericKeyedObjectPool) pool).getMaxWait();
	}

	/**
	 * @return pool
	 */
	public KeyedObjectPool getInternalPool() {
		return pool;
	}

	/**
	 * @param i
	 * @return	��į������
	 */
	public InetSocketAddress getServer(int i) {
		return servers[i].getAddr();
	}

	public void returnClient(int serverId, QueryClient obj) throws Exception{
		pool.returnObject(servers[serverId].getKey(), obj);
	}
	

	/**
	 * @return	���ö೤��connection���Ա�ɨ�����ر�
	 */
	public long getMinEvictableIdleTimeMillis() {
		return ((GenericKeyedObjectPool) pool).getMinEvictableIdleTimeMillis();
	}

	/**
	 * @return	ÿ��ɨ����ٸ�����connection
	 */
	public int getNumTestsPerEvictionRun() {
		return ((GenericKeyedObjectPool) pool).getNumTestsPerEvictionRun();
	}

	/**
	 * @return	ÿ���೤ʱ��ɨ��һ������connection
	 */
	public long getTimeBetweenEvictionRunsMillis() {
		return ((GenericKeyedObjectPool) pool)
			.getTimeBetweenEvictionRunsMillis();
	}

	/**
	 * @param minEvictableIdleTimeMillis	���ö೤��connection���Ա�ɨ�����ر�
	 */
	public void setMinEvictableIdleTimeMillis(long minEvictableIdleTimeMillis) {
		((GenericKeyedObjectPool) pool).setMinEvictableIdleTimeMillis(
			minEvictableIdleTimeMillis);
	}

	/**
	 * @param numTestsPerEvictionRun		ÿ��ɨ����ٸ�����connection
	 */
	public void setNumTestsPerEvictionRun(int numTestsPerEvictionRun) {
		((GenericKeyedObjectPool) pool).setNumTestsPerEvictionRun(
			numTestsPerEvictionRun);
	}

	/**
	 * @param timeBetweenEvictionRunsMillis	ÿ���೤ʱ��ɨ��һ������connection
	 */
	public void setTimeBetweenEvictionRunsMillis(long timeBetweenEvictionRunsMillis) {
		((GenericKeyedObjectPool) pool).setTimeBetweenEvictionRunsMillis(
			timeBetweenEvictionRunsMillis);
	}

	/**
	 * @return Returns the inplaceConnectionLife.
	 */
	public int getInplaceConnectionLife() {
		return -1;
//		return inplaceConnectionLife;
	}
	
	/**
	 * @param inplaceConnectionLife The inplaceConnectionLife to set.
	 */
	public void setInplaceConnectionLife(int inplaceConnectionLife) {
//		this.inplaceConnectionLife = inplaceConnectionLife;
	}
	public void setSleepMillisecondsAfterQueueTimeOut(long time){
		this.serverConfig.setSleepMillisecondsAfterQueueTimeOut(time);
	}
	/**
	 * ���ĳ̨��������pool�е�keyֵ.
	 * @param i
	 * @return
	 */
	public Object getServerKey(int i){
		if( i>=0 && servers != null && servers.length>i){
			return servers[i].getKey();
		}
		return null;
	}
	public int getConnectTimeoutMillis() {
		return connectTimeOutMillseconds;
	}
	public void setConnectTimeoutMillis(int connectTimeOutMillseconds){
		this.connectTimeOutMillseconds = connectTimeOutMillseconds;
	}
	public int getSocketTimeoutMillis(){
		return this.timeOutMillseconds;
	}
	public  void setSocketTimeoutMillis(int socketTimeoutMillseconds){
		this.timeOutMillseconds = socketTimeoutMillseconds;
	}

}