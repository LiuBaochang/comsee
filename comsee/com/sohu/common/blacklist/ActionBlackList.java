package com.sohu.common.blacklist;

import java.util.ArrayList;

public class ActionBlackList {
	AdAhoCorasickMultiBlackList ahoMatcher = null; //�ж�����Ĺؼ���
	ArrayList<BlackListEntry> assistMatcher = null; //�ж��ศ���ؼ���
	private static int MINFOUND = 3; //�ҵ�3���ؼ��ʣ�����Ϊ����ģ��������������������ʲô����϶�������
	
	public ActionBlackList(AdAhoCorasickMultiBlackList aho, ArrayList<BlackListEntry> assist){
		this.ahoMatcher = aho;
		this.assistMatcher = assist;
	}
	
	public BlackListEntry findMatch( String key ){
		return findMatch( key, null);
	}
	
	public BlackListEntry findMatch( CharSequence key, StringBuffer sb){
		if( key == null ) {
			return null;
		}
		
		if( sb == null ){
			sb = new StringBuffer ( key.length() );
		} else {
			sb.delete( 0, sb.length() ); 
		}

		for( int i=0; i<key.length(); i++){
			char ch = key.charAt( i );
			int type = CharFilter.charType( ch );
			if( type != CharFilter.WHITE
					&& type != CharFilter.SPACE ){
				sb.append( ch );
			}
		}
		return get( sb.toString() );
		
	}
	
	public BlackListEntry get( String key ){
		BlackListEntry ret = null;
		int reason = -4;
		do{
			if (key == null || key.length()==0){
				reason = -3;
				break;
			}
			if (this.ahoMatcher == null){
				reason = -2;
				break;
			}
			ArrayList<BlackListEntry> matched = this.ahoMatcher.findMatch(key);
			if (matched == null || matched.size() <= 0){
				reason = -1;
				break;
			}

			ret = new BlackListEntry();
			ret.key = "";
			ret.mask = matched.get(0).mask;
			ret.startTime = matched.get(0).startTime;
			ret.endTime = matched.get(0).endTime;
			boolean found = false;
			//�����б��ֱ��¼�����������������ľ��Ժ�����Ԫ�غͻ������
			//�������еĺ������ֱ���A, C, ABC, CD, BC
			//unclude�а���A,C
			//include�а���ABC,CD, BC
			ArrayList<BlackListEntry> uncludeList = new ArrayList<BlackListEntry>();
			ArrayList<BlackListEntry> includeList = new ArrayList<BlackListEntry>();
			for (int i = 0; i < matched.size(); i++){
				//�ҵ���Сmask
				if (ret.rule > matched.get(i).rule){
					ret.mask = matched.get(i).mask;
					ret.key = matched.get(i).key;
				}
				//�����Ƿ�����С��mask��һ��ȡ���ϸ��ʱ��
				if (ret.startTime != 0 && (matched.get(i).startTime == 0 || ret.startTime > matched.get(i).startTime)){
					ret.startTime = matched.get(i).startTime;
				}
				if (ret.endTime != 0 && (matched.get(i).endTime == 0 || ret.endTime < matched.get(i).endTime)){
					ret.endTime = matched.get(i).endTime;
				}
				String tmpkey = (String)matched.get(i).key;
				ret.key = ret.key + "core:"+tmpkey;
				if (key.equals(tmpkey)){
					found = true;
				}
				//����һ���ҵ��ĺ������б�������ABC, A, C�����˳� A C����
				//����ABCD����Ϊ������ABC, A, C�������ж�Ϊ������3����ʵ��Ӧ��������������
				boolean isContainingOther = false;
				for (int j = 0; j < matched.size(); j++){
					if (i != j){
						if (tmpkey.indexOf((String)matched.get(j).key) >= 0){
							isContainingOther = true;
							break;
						}
					}
				}
				if (!isContainingOther){
					uncludeList.add(matched.get(i));
				}else{
					includeList.add(matched.get(i));
				}
			}
			if (found){
				//���ҵ���ȫƥ����
				reason = 5;
				break;
			}
			if (uncludeList.size() >= MINFOUND || uncludeList.size() == 0){
				//�������еĺ��Ĵ�>2��
				reason = 1;
				break;
			}
			
			String remainingString = key;
			//����ѯ�������������滻��
			for (int i = 0; i < includeList.size(); i++){
				remainingString = remainingString.replace((String)includeList.get(i).key, "");
			}
			for (int i = 0; i < uncludeList.size(); i++){
				remainingString = remainingString.replace((String)uncludeList.get(i).key, "");
			}
			if ("".equals(remainingString)){
				//��ȫ�����˺����������
				reason = 4;
				break;
			}
			//����ʣ���ַ��Ƿ������е��Ӵ����ǵĻ���ʾ��ȫƥ��
			found = false;
			for (int i = 0; i < matched.size(); i++){
				//�ҵ���Сmask
				String tmpkey = (String)matched.get(i).key;
				if (tmpkey.indexOf(remainingString) >= 0){
					found = true;
					break;
				}
			}
			if (found){
				reason = 2;
				break;
			}
			int nowFound = uncludeList.size();
			if (this.assistMatcher == null){
				//û�и����ʱ�ֱ���˳�
				ret = null;
				reason = -5; 
				break;
			}
			found = false;
			for (int i = 0; i < this.assistMatcher.size(); i++){
				BlackListEntry tmp = this.assistMatcher.get(i);
				String tmpKey = (String)tmp.key;
				if (remainingString.indexOf(tmpKey) >= 0){
					ret.key = ret.key + "assist:"+tmpKey;
					//ʣ���ѯ�ʵ��ڸø����ʣ�˵����ȫƥ��
					remainingString = remainingString.replace(tmpKey, "");
					if ("".equals(remainingString)){
						reason = 6;
						found = true;
						break;
					}
					//�鿴�Ƿ��Ѿ��ҵ�3��ƥ��Ĺؼ���
					nowFound ++;
					if (nowFound >= MINFOUND){
						reason = 7;
						found = true;
						break;
					}
				}
			}
			if (found){
				break;
			}
			//δ�ҵ�
			ret = null;
		}while(false);
		return ret;
	}
}
