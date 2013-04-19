package com.sohu.sohudb;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sohu.common.connectionpool.async.AsyncGenericConnectionPool;

public class AsyncSohuDBPool extends AsyncGenericConnectionPool {

    public AsyncSohuDBPool(String name) {
        super(new AsyncSohuDBClientFactory(), name);

        // SohuDBPool���ӵĲ���cache����������һ��ʧЧ�󣬲���ҪҲ��Ӧ���Զ��л�����һ������
        this.setAutoSwitchToNextServer(false);
    }

    public AsyncSohuDBPool() {
        this("SohuDB");
    }

    private static final Log logger = LogFactory
                                            .getLog(AsyncSohuDBPool.class);

    protected Log getLogger() {
        return logger;
    }
}
