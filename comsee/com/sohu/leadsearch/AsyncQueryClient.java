package com.sohu.leadsearch;

import java.nio.channels.SelectionKey;

import com.sohu.common.connectionpool.ServerConfig;


public interface AsyncQueryClient {

	/**
	 * ����������ݵ�handler,
	 * ��Selector�̵߳���, 
	 * ���ڽ����ݴ�ϵͳ��������������,�������ݼ�����֤,��֤���ݰ�����Ч��.
	 * ע��: ���Բ����������׳��쳣.
	 * ʵʱʱע��ͬ����̵߳Ļ���
	 * @param key
	 */
	public void handleInput( SelectionKey key );
	
//	/**
//	 * ����������������handler,
//	 * @param obj
//	 */
//	public void handleOutput( Object obj );
//	
//	public void notifyInput();
	/**
	 * �ر�. �ͷŵ����е���Դ.
	 */
	public void close();
	
	/**
	 * ���������÷���.
	 * �������÷����������ò���.
	 * @param config ͨ�����ò����Ĵ洢�ṹ
	 * @param i ��ǰ��������������
	 */
	public void setServerConfig(ServerConfig config, int i);
	
	public int getIndex();
}
