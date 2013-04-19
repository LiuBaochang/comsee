package com.sohu.common.util;

/**
 * ��������byte[]�Ͷ���, �ṩ��String���ﶨ���indexOf, startsWith�ȳ��÷��������Ƶķ���.
 * 
 * @author liumingzhu
 * @version 0.9
 *
 */
public class ByteArrayUtils {
	/**
	 * ������String.indexOf()�ķ���. ��byte�����в���seq�а�����ascii���λ��.
	 * 
	 * @param data  ���ܰ����������ַ���������.
	 * @param start ������ʼλ��
	 * @param end   ��������λ��
	 * @param seq   ���������ַ���
	 * @return  �������ַ�����byte�����е���ʼλ��.
	 * 
	 * ���data��seqΪnull, ���׳�NullPointerException
	 */
	public static int indexOf( byte[] data, int start, int end, CharSequence seq ){
		if( end - start < seq.length()
				|| seq.length() <= 0 ){
			return -2;
		}
		int seq_len = seq.length();
		int newend = end - seq_len + 1;
		
		int i = start;
		for( ;
				( i < newend  );
			i++ ){
			int k = 0;
			for( int j = i ;
				( k < seq_len
						&& data[j] == (byte)seq.charAt(k) );
				j++, k++){
			}
			if( k == seq_len ){
				break;
			}
		}
			;
		if( i < end ){
			return i;
		} else {
			return -1;
		}
		
	}
	/**
	 * ������String.indexOf()�ķ���. ��byte�����в���seq�а�����byte�����λ��.
	 * 
	 * @param data  ���ܰ����������ַ���������.
	 * @param start ������ʼλ��
	 * @param end   ��������λ��
	 * @param seq   ��������byte��
	 * @return  �������ַ�����byte�����е���ʼλ��.
	 * 
	 * ���data��seqΪnull, ���׳�NullPointerException
	 */
	public static int indexOf( byte[] data, int start, int end, byte[] seq ){
		if( end - start < seq.length
				|| seq.length <= 0 ){
			return -2;
		}
		int seq_len = seq.length;
		int newend = end - seq_len + 1;
		
		int i = start;
		for( ;
				( i < newend  );
			i++ ){
			int k = 0;
			for( int j = i ;
				( k < seq_len
						&& data[j] == seq[k] );
				j++, k++){
			}
			if( k == seq_len ){
				break;
			}
		}
			;
		if( i < end ){
			return i;
		} else {
			return -1;
		}
		
	}

	/**
	 * ������String.startsWith()����, ���byte�����Ƿ���ĳ�ַ�����ͷ.
	 * 
	 * @param data  ������ĳ�ַ�����ͷ��byte���� 
	 * @param start ��ʼλ��
	 * @param end   ����λ��
	 * @param seq   ���������ַ���
	 * @return  1 - ��ʾΪ��
	 */
	public static int startsWith( byte[] data, int start, int end, CharSequence seq ){
		if( data == null ) {
			return -1;
		}
		if( seq == null ){
			return -2;
		}
		if( start < 0 ){
			return -3;
		}
		if( end > data.length ){
			return -4;
		}
		if( end - start < seq.length() ){
			return -5;
		}
		end = start + seq.length();
		
		int i = start;
		for( int j=0;
				( i < end
					 && data[i] == (byte)seq.charAt(j) );
			i++, j++ )
			;
		if( i == end ){
			return 1;
		} else {
			return 0;
		}
	}
	/**
	 * ��#@see startsWith( byte[], int, int, CharSequence )
	 * @param data  ������ĳ�ַ�����ͷ��byte���� 
	 * @param start ��ʼλ��
	 * @param end   ����λ��
	 * @param d     ��������byte��
	 * @return  1 - ��ʾΪ��
	 */
	public static int startsWith( byte[] data, int start, int end, byte[] d ){
		if( data == null ) {
			return -1;
		}
		if( d == null ){
			return -2;
		}
		if( start < 0 ){
			return -3;
		}
		if( end > data.length ){
			return -4;
		}
		if( end - start < d.length ){
			return -5;
		}
		
		int i= start;
		for( int j=0;
				( i < end
					 && data[i] == d[j] );
			i++, j++ )
			;
		if( i == end ){
			return 1;
		} else {
			return 0;
		}
	}
	private static char[] hexchars = { '0', '1', '2', '3', '4', '5', '6', '7',
		'8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };

	/**
	 * ��һ��byte����ת���ɿɶ���HEX ASCII�봮.
	 * 
	 * @param bytes Ҫ��ӡ��byte��
	 * @param dir   ��ӡ�ķ���. 1 - ��byte����˳��; 0 - ����
	 * @return ����ִ�
	 */
	public static String BytesToHexString(byte[] bytes, int dir) {
		char[] buf = new char[bytes.length * 2];
		for (int i = 0; i < bytes.length; ++i) {
			int v = (bytes[i] > -1) ? bytes[i] : (bytes[i] + 0x100);
			if( dir > 0 ){
				buf[i * 2] = hexchars[v / 0x10];
				buf[i * 2 + 1] = hexchars[v % 0x10];
			} else {
				buf[bytes.length * 2 - i * 2 - 2] = hexchars[v / 0x10];
				buf[bytes.length * 2 - i * 2 - 1] = hexchars[v % 0x10];
			}
		}
		return new String(buf);
	}

	/**
	 * ģ��C�����н���������ת�����������������ֵ�Ĳ�����littleEndian
	 * @param ar
	 * @param start
	 * @return
	 */
    public static long byte2long(byte[] ar, int start){
        long ret = 0;
        for( int i=0;i<8;i++){
                ret |= ((long)ar[start+i] & 0xffl)<< (i<<3);
        }
        return ret;
    }
    
	public static final int htonl(int t){
		int t0 = t & 0xFF;
		int t1 = (t >> 8) & 0xFF;
		int t2 = (t >> 16) & 0xFF;
		int t3 = (t >> 24) & 0xFF;
		
		return (t0 << 24) + (t1 << 16) + (t2 << 8) + t3; 
	}
	public static final short htons(short t){
		int t0 = t & 0xFF;
		int t1 = (t >> 8) & 0xFF;
		
		return (short)((t0 << 8) + t1); 
	}

}
