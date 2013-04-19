package com.sohu.common.util;

import com.sohu.common.connectionpool.Request;
/**
 * Comsee���ð���һЩ��������Щ������������comsee�����Ƕ�comsee���Ĳ�����
 * ��ˣ����ֻ������JDK�ķ������벻Ҫ���������档
 * @author Cui Weibing
 * @date 2009.6.1
 */
public class ComseeUtil {
	/**
	 * ���ݴ����������Ϣ������ָ����server��Ϣ�����������־��
	 * @param qr ��װ������ص���Ϣ
	 * @param serverInfoBuffer ���server��Ϣ��buffer
	 */
	public static void updateServerInfo(Request qr, StringBuffer serverInfoBuffer){
		String tmp = null;
		String status = "-1000";
		String time = "-1";
		if( qr != null ){
			tmp = qr.getServerInfo();
			status = String.valueOf(qr.getStatus());
			time = String.valueOf(qr.getTime());
		}
		if( tmp == null ){
			tmp = "null";
		}
		serverInfoBuffer.append( tmp );
		serverInfoBuffer.append( '&' );
		serverInfoBuffer.append( status );
		serverInfoBuffer.append( '&' );
		serverInfoBuffer.append( time );
		serverInfoBuffer.append( '_' );
	}

}
