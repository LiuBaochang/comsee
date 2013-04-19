package com.sohu.leadsearch;

import java.util.ArrayList;
import java.util.List;

import com.sohu.common.connectionpool.Result;

public class LeadResult implements Result {
	
	public static final class AdItem{
		public ArrayList data = null;
		public int total = 0;
		public int ret = 0;
		public int start = 1; 
		public int servicestyle = 0;
		public int showType = 0;
		public int adType = -1;
	}

	public String result = null;

	public long time = 0;

	List items = null;

	int totalNum = 0;
	
	public String adHead_cookie = null; 
	
	public String adHead_itemnum = null; 
	
	AdItem[] adItems = new AdItem[32];

	public List getItems() {
		return items;
	}

	public void setItems(List items) {
		this.items = items;
	}

	public int getTotalNum() {
		return this.totalNum;
	}

	public void setTotalNum(int tn) {
		this.totalNum = tn;
	}

	public LeadResult(String rt, long tm) {
		this.result = rt;
		this.time = tm;
	}
	public LeadResult(String rt) {
		this.result = rt;
		
	}
	public void setTime(long t) {
		this.time = t;
	}

	public long getTime() {
		return this.time;
	}

	public int setAdItem( int n, AdItem item ){
		if( n >= 0 && n < adItems.length ){
			adItems[n] = item;
			return 0;
		} else {
			return -1;
		}
	}
	public ArrayList getAdItemData( int n ){
		if( n >= 0 && n < adItems.length ){
			AdItem item = adItems[n];
			if( item != null ){
				return item.data;
			}
		}
		return null;
	}
	public int getAdItemTotal( int n ){
		if( n >= 0 && n < adItems.length ){
			AdItem item = adItems[n];
			if( item != null ){
				return item.total;
			}
		}
		return -1;
		
	}
	public int getAdItemServiceStyle( int n ){
		if( n >= 0 && n < adItems.length ){
			AdItem item = adItems[n];
			if( item != null ){
				return item.servicestyle;
			}
		}
		return 0;
		
	}
	public int getAdItemRet( int n ){
		if( n >= 0 && n < adItems.length ){
			AdItem item = adItems[n];
			if( item != null ){
				return item.ret;
			}
		}
		return -1;
		
	}
	public int getAdItemStart( int n ){
		if( n >= 0 && n < adItems.length ){
			AdItem item = adItems[n];
			if( item != null ){
				return item.start;
			}
		}
		return -1;
		
	}
	//�ٶȹȸ���ʽС����, 20111010
	public int getAdItemShowType( int n ){
		if( n >= 0 && n < adItems.length ){
			AdItem item = adItems[n];
			if( item != null ){
				return item.showType;
			}
		}
		return -1;
		
	}
	public int getAdItemType( int n ){
		if( n >= 0 && n < adItems.length ){
			AdItem item = adItems[n];
			if( item != null ){
				return item.adType;
			}
		}
		return -1;
		
	}


	public String getResult() {
		return result;
	}

	public String getAdHead_cookie() {
		return adHead_cookie;
	}
	
	/**
	 * 2010.10.25 Ʒ��ר����������bidding server��by buhailiang
	 * �����ߺ��ұ�����Ĵ���
	 */
	private String[] brands = null;
	public String[] getBrands() {
		return brands;
	}

	public void setBrands(String[] brands) {
		this.brands = brands;
	}




	/**
	 * ͨ��ʵ�ʻ�õĹ�����õ���Ӧ��ͳ�ƴ��룬�Թ���ҳչʾ��ͨ����ҳֱ�ӵ��ô˷�������
	 * 
	 * @param result
	 *            �����
	 * @return ���ͳ�Ƶ�HTML����
	 */
	public String getDebugString() {
		StringBuilder sb = new StringBuilder();
		int len = adItems.length;
		for (int i = 0; i < len; i++) {
			List total = getAdItemData(i);
			if (total != null && total.size() >= 0) {
				sb.append(i);
				sb.append(":");
				sb.append(total.size());
				sb.append(" ");
			}
		}
		String str = sb.toString();
		return str;
	}
	
}
