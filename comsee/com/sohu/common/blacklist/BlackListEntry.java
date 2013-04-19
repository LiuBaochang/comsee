package com.sohu.common.blacklist;

/**
 * item in the BlackList
 * Created on 2006-10-30
 * @author Liu Mingzhu (mingzhuliu@sohu-inc.com)
 *
 */
public class BlackListEntry {
//	private int docid;
	public Object key;
	public boolean isPolitic;
	public int mask;
	public int rule;
	
	//��summary����Щʱ��εĽ������
	public long startTime = 0;
	public long endTime = 0;
	
	public String getUrl(){
		return null;
	}
	public boolean isWhite(){
		return mask == 0x0f;
	}
}
