package com.sohu.common.encoding;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;

/**
 * �ַ�ӳ��� (Unicode char -> Unicode char)
 * @author liumingzhu
 *
 */
public class CharMap {

	private static final int MAP_LENGTH = 65536;
	
	protected boolean inited = false;
	protected char[] map = new char[MAP_LENGTH];
	
	public CharMap(){	
	}
	
     /**
     * ��ʼ�����̡�����װ���ڴ�.
     * �ļ���ʽ: 65536*2�Ķ������ļ�, ����Ϊunicode n ��ӳ���ַ���unicodeֵ
     * @param filename ӳ���������ļ�
     */
    public void load(String filename) throws IllegalArgumentException {
    	try{
    		load(new FileInputStream(filename));
    	} catch ( FileNotFoundException e){
			throw new IllegalArgumentException("CharMap source file NOT EXISTS or UNREADABLE: "+filename);
		} catch ( SecurityException e){
			throw new IllegalArgumentException("CharMap source file UNREADABLE(Security Reason): "+filename);
		} catch ( Exception e) {
			throw new IllegalArgumentException("CharMap source file NOT EXISTS or UNREADABLE: "+filename);
		}
    }
    public void load(InputStream stream) throws Exception {
    	//FileInputStream fis = null;
    	BufferedInputStream fis = null;
		try {
			//fis = new FileInputStream(filename);
			fis = new BufferedInputStream(stream);
			for (int i = 0; i < MAP_LENGTH; i++) {
				int l = fis.read();
				int h = fis.read();
				map[i] = (char) (h * 256 + l);
			}
			inited = true;
		} catch ( NullPointerException e){
			throw new IllegalArgumentException("CharMap source file is NULL!");
		} catch ( IOException e) {
			throw e;
		}finally {
			if( fis != null ){
				try{
					fis.close();
				}catch(IOException e){}
				fis = null;
			}
		}
	}

    /**
     * �洢���̡�������Ϊ�����ļ�.
     * �ļ���ʽ: 65536*2�Ķ������ļ�, ����Ϊunicode n ��ӳ���ַ���unicodeֵ
     * @param filename ӳ���������ļ�
     */
    public void save(String filename) throws IllegalArgumentException {
    	FileOutputStream fis = null;
		try {
			fis = new FileOutputStream(filename);
			for (int i = 0; i < MAP_LENGTH; i++) {
				fis.write((int)map[i] );
				fis.write( ((int)map[i]) >>> 8 );
			}
			fis.flush();
			fis.close();
		} catch ( NullPointerException e){
			throw new IllegalArgumentException("CharMap source file is NULL!");
		} catch ( FileNotFoundException e){
			throw new IllegalArgumentException("CharMap source file NOT EXISTS or UNREADABLE: "+filename);
		} catch ( SecurityException e){
			throw new IllegalArgumentException("CharMap source file UNREADABLE(Security Reason): "+filename);
		} catch ( IOException e) {
			throw new IllegalArgumentException("CharMap source file NOT EXISTS or UNREADABLE: "+filename);
		}finally {
			if( fis != null ){
				try{
					fis.close();
				}catch(IOException e){}
				fis = null;
			}
		}
	}

    /**
     * �������, �õ�ӳ���ַ�
     * @param c
     * @return
     */
    public char map(char c){
    	if( inited )
    		return map[(int)c];
    	else
    		return c;
    }
    /**
     * �������ӳ���ϵ, ���ַ�������ת��
     * @param str
     * @return
     */
    public String map(CharSequence str){
    	if( str == null ){
    		return null;
    	}
    	StringBuffer sb = new StringBuffer( str.length() );
    	
    	for( int i=0; i<str.length();i++){
    		sb.append( map(str.charAt(i)) );
    	}
    	return sb.toString();
    }
    /**
     * ���ַ�������ӳ�䲢���
     * @param str
     * @param out
     * @throws IOException
     */
    public void convert( CharSequence str, Writer out) throws IOException{
    	if( str == null ) return ;
    	for( int i=0; i<str.length(); i++){
    		out.write( map(str.charAt(i)) );
    	}
    }
    /**
     * ������е�ĳ���ַ������޸�
     * @param o
     * @param n
     */
    public void correct( char o, char n ){
    	map[(int)o] = n;
    }
    public void correct( char[]src){
    	correct( src, 0, 0, src.length);
    }
    public void correct( char[]src, int dstIndex ){
    	correct( src, 0, dstIndex, src.length);
    }
    public void correct( char[]src, int startIndex, int destIndex, int length ){
    	System.arraycopy(src, startIndex, map, destIndex, length);
    }
}
