package com.sohu.common.encoding;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Writer;

/**
 * singletonģʽ��ƴ���ʱ�, �ַ�(char)-�ַ���(String)��Ӧ��ϵ
 * ʹ��ǰ��Ҫ��ʼ��. PinYin.getInstance().init( {�ʱ��ļ�} );
 * ���û�г�ʼ��, ��getPinYin�����õ��մ�.
 * 
 * @author liumingzhu
 * @version 1.0
 *
 */
public class PinYin {
	
	// ���ô��ڵ�����
	private String[] map = new String[65536];
	// ����ʵ��
	private static PinYin instance = new PinYin();
	// �����ļ���, ����reload()����.
	String defaultFileName = null;
	
	/**
	 * public�Ĺ��췽��, ����ʵ�ֶ��ʵ��
	 *
	 */
	public PinYin(){
	}
	
	// ȡ��ʵ������
	public static PinYin getInstance(){
		return instance;
	}
	
	/**
	 * ʹ�û�����ļ�������reload����
	 *
	 */
	public void reload(){
		String filename = this.defaultFileName;
		if( filename != null ){
			init( filename );
		}
	}
	/**
	 * ����ƴ���ʱ�, ͬʱ����ʱ���ļ���.
	 * �ʱ��ʽ:{һ��GBK�ַ�}{��Ӧƴ����ascii��}\n
	 * @param filename ���ص��ļ���
	 * @return ����״̬, 0 - ��������; -1 - �ļ���Ϊnull; -2 - IO����
	 */
	public synchronized int init(String filename ){
		
		this.defaultFileName = filename;
		
		if( filename == null ) return -1;
		int ret = -1;
		
		BufferedReader reader = null;
		
		System.arraycopy(staticMap, 0, map,0,staticMap.length );
		try{
			reader = new BufferedReader(
					new InputStreamReader( new FileInputStream(filename), "GBK")
				);
			String line;
			
			while( (line=reader.readLine())!= null ){
				line = line.trim();
				if( line.length()==0 ) continue;
				char c = line.charAt(0);
				String pinyin = line.substring(1).trim();
				if(pinyin.length()==0 ) continue;
				map[(int)c] = pinyin;
			}
			ret = 0;
		}catch( IOException e){
			ret = -2;
			e.printStackTrace();
		}finally {
			if( reader != null ){
				try{
					reader.close();
				}catch( IOException e){}
				reader = null;
			}
		}
		return ret;
	}
	/**
	 * �õ������ַ�����ƴ����ﴮ
	 * @param line ԭʼ��
	 * @return �������Ϊnull, �򷵻�null, ���򷵻ض�Ӧ��ƴ��
	 */
	public String getPinYin(String line ){
		if( line == null ) return null;
		
		StringBuffer sb = new StringBuffer( line.length()*4 );
		for( int i=0; i< line.length();i++){
			char c = line.charAt(i);
			sb.append( getPinYin(c) );
		}
		return sb.toString();
	}

	/**
	 * �õ������ַ���ƴ��
	 * @param c ָ���ַ�
	 * @return ��Ӧ��ƴ����. ������ַ�û��ƴ��, �򷵻�"";
	 */
	public String getPinYin( char c ){
		String list = map[(int)c];
		if( list != null  ){
			return list;
		} else {
			return "";
		}
		
	}
	
	/**
	 * ����ʵʱ�޸�ƴ���ʱ�
	 * @param c Ҫ�޸�ƴ�����ַ�
	 * @param s �޸ĵĽ��
	 */
	public void setPinYin( char c , String s ){
		map[ (int)c ] = s;
	}
	
	// ԭ�ʱ���û�����ֺ���ĸ��ƴ��, ���ﲹ��
	private static String[] staticMap = new String[127];
	static {
		for( int i='a'; i<='z'; i++ ){
			staticMap[i] = ""+((char)i);
		}
		for( int i='A'; i<='Z'; i++ ){
			staticMap[i] = ""+(char)((char)i-'A'+'a');
		}
		staticMap[(int)'0'] = "ling";
		staticMap[(int)'1'] = "yi";
		staticMap[(int)'2'] = "er";
		staticMap[(int)'3'] = "san";
		staticMap[(int)'4'] = "si";
		staticMap[(int)'5'] = "wu";
		staticMap[(int)'6'] = "liu";
		staticMap[(int)'7'] = "qi";
		staticMap[(int)'8'] = "ba";
		staticMap[(int)'9'] = "jiu";

	}
	
	public void dump(Writer out )throws IOException{
		for(int i=0; i<map.length; i ++){
			String list = map[i];
			if( list != null ){
				out.append((char)i);
				out.append('\t');
				out.append( list );
			}
		}
	}

}
