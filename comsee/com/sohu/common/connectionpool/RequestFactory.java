package com.sohu.common.connectionpool;

public interface RequestFactory {
	
	/**
	 * ��������ʵ�ʷ��������Request
	 * @return
	 */
	public Request newRequest();

	/**
	 * ��������̽���õ�Request
	 * @return
	 */
	public Request newProbeRequest();
}
