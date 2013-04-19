/*
 * Created on 2003-11-24
 *
 */
package com.sohu.tinysearch;

import java.util.*;

/**
 * ��ѯ�����һ�� 
 * @author KirbyZhou
 */
public class TinySearchResultItem {
	private boolean isNewValueSet = false;
	// item������. 0��ʾδ֪
	int type = 0;
	// item�����ݴ�.
	String value = null;
	// item��ת������ʽ.
	List listValue = null;
	// ��չ
	Object ext = null;
	
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
	public String getValue() {
		return value;
	}
	public List getValueList(){
		if( isNewValueSet ){
			ArrayList tmp = new ArrayList();
			int index = 0 ;
			while ( index >=0 ){
				int rindex = value.indexOf(TinySearch.CONTENT_SEPERATOR, index);
				if( rindex >= 0 ){
					if( index == rindex ){
						tmp.add( "" );
					} else {
						tmp.add( value.substring(index, rindex));
					}
				}
				else { // �����β
					if( index == value.length() ){
						tmp.add( "" );
					} else {
						tmp.add(value.substring(index));
					}
					break;
				}
				index = rindex + TinySearch.CONTENT_SEPERATOR.length();
			}
			if( tmp.size() > 0 ){
				listValue = tmp;
			}
		}
		return listValue;
	}
	public void setValue(String value) {
		if( this.value==null || !this.value.equals(value)){
			this.value = value;
			isNewValueSet = true;
		}
	}
	public Object getExt() {
		return ext;
	}
	public void setExt(Object ext) {
		this.ext = ext;
	}
}
