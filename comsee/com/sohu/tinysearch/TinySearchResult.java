/*
 * Created on 2005-11-24
 *
 */
package com.sohu.tinysearch;

import java.util.List;

import com.sohu.common.connectionpool.Result;

/**
 * tinysearch��ѯ�Ľ��
 * 
 * @author Mingzhu Liu
 *  
 */
public class TinySearchResult implements Result{
	// ��ѯ�����
	List items = null;
	// ��ѯ��ʱ
	long time = 0;
	
	// �������
	TinySearchResultItem adItems[] ;

	// ������������

	// QueryCorrection������
	// 0��ʾ����qrֵ
	// 1��ʾƴ��
	// 2��ʾ����
	int qrType = 0;
	// qr ����
	int qrNum = 0;
	// qr������, ��'\t'�ֿ�
	String qr = null;
	// ԭʼ��qrͷ
	String qrHeader = null;
	
	// Hint������(��û��)
	int hintType = 0;
	// Hint ����
	int hintNum = 0;
	// hint������,��'\t'�ֿ�
	String hint = null;
	// ԭʼ��hintͷ
	String hintHeader = null;
	//tinyEngine��ԭʼ��qaͷ����Ϣ
	String qaHeader = null;
	//tinyEngine��������̨�����صĲ�ѯ��
	String qaWord = null;
	//tinyEngine�����id��64λ��ÿλ����һ�����Ŀǰû�ж�����
	//��������-1��ʱ�򣬱�ʾΪ�ʴ��࣬Ҫ��cacheЭͬ�жϣ��������ǲ���
	long qaCatid = 0;
	//tinyEngine������id��32λ��ÿλ����һ�������������ж����������
	int qaSignid = 0;

	// ��һ����Ч�Ľ������ֵ
	int firstValid = 0;
	
	public List getItems() {
		return items;
	}
	public void setItems(List items) {
		this.items = items;
	}
	public long getTime() {
		return time;
	}
	public void setTime(long time) {
		this.time = time;
	}
	public int getFirstValid() {
		return firstValid;
	}
	public void setFirstValid(int firstValid) {
		this.firstValid = firstValid;
	}
	public int getQrType(){
		return this.qrType;
	}
	public void setQrType(int qt){
		this.qrType = qt;
	}
//	��һ������tinyEngine�Ĵ���
	public void setQaHeader(String header) {
		this.qaHeader = header;
	}
	public String getQaHeader() {
		return this.qaHeader;
	}
	public void setQaCategory(Long catid) {
		this.qaCatid = catid;
	}
	public Long getQaCategory() {
		return this.qaCatid;
	}
	public void setQaSignid(int sid) {
		this.qaSignid = sid;
	}
	public int getQaSignid() {
		return this.qaSignid;
	}
	public void setQaWord(String qword) {
		//TODO��maybe need some handle
		this.qaWord = qword;
	}
	public String getQaWord() {
		return this.qaWord;
	}
	
//	private boolean isQrChecked = false;//����Ƿ񾭹������δ����
//	private boolean isQrClear = true;
//	private boolean isHintChecked = false; 
//	private boolean isHintClear = true; //����Ƿ񾭹������δ����
//	Perl5Matcher matcher = null;

//	public static String fuck(String query , char deli, Perl5Matcher matcher){
//		if( query != null ){
//			StringBuffer sb = new StringBuffer();
//			int left=0;
//			do {
//				int right = query.indexOf( deli, left );
//				
//				String term;
//				if( right== -1 ) right = query.length();
//				if( right<left ){
//					term = query.substring(left);
//				} else if( right==left ){
//					left = right+1;
//					continue;
//				} else{
//					term = query.substring(left, right );
//				}
//				if( sb.length() !=0 ){
//					sb.delete(0, sb.length() );
//				}
//				if(  PageMask.getInstance().matches(term, false, matcher) 
//						|| NewBlackList.getInstance().matches( term, matcher, sb)!=null ){
//					return null;
////					if( sb == null ){
////						sb = new StringBuffer( query.length() );
////						for( int i=0; i< left; i++ ){
////							sb.append( query.charAt(i) );
////						}
////					}
////					
////				} else {
////					if( sb != null ){
////						if( sb.length()>0 ){
////							sb.append('\t');
////						}
////						sb.append( term );
////					}
//				}
//				left = right+1;
//			}while( left< query.length());
//			
////			if( sb != null ){
////				this.qr = sb.toString();
////			}
//		}
//		return query;
//	}
	public String getQr(){
		return this.qr;
	}
	public void setQr(String qr){
		this.qr = qr;
	}
	public void setHintType( int ht){
		this.hintType = ht;
	}
	public int getHintType ( ) {
		return this.hintType;
	}
	public String getHint() {
		return this.hint;
	}
	public void setHint(String hint ){
		this.hint = hint;
	}
	public String getQrHeader(){
		return this.qrHeader;
	}
	public void setQrHeader( String qh ){
		this.qrHeader = qh;
	}
	public String getHintHeader(){
		return this.hintHeader;
	}
	public void setHintHeader(String hh ){
		this.hintHeader = hh;
	}
	public int getHintNum(){
		return this.hintNum;
	}
	public void setHintNum(int hn ){
		this.hintNum = hn;
	}
	public int getQrNum(){
		return this.qrNum;
	}
	public void setQrNum( int qn){
		this.qrNum = qn;
	}
	public void setAdItems(TinySearchResultItem[] adItems) {
		this.adItems = adItems;
	}
	public TinySearchResultItem getAdItem( int idx ){
		TinySearchResultItem adItems[] = this.adItems;
		if( adItems == null ) return null;
		if( idx <0 || adItems.length <= idx ) return null;
		
		return adItems[ idx ];
	}
	public void setAdItem( int idx, TinySearchResultItem item) throws IndexOutOfBoundsException {
		if( item == null ) return;
		
		if( idx > TinySearch.MAX_AD_ITEM_NUM || idx <0 ) throw new IndexOutOfBoundsException( ""+idx );
		
		if( this.adItems == null ){
			this.adItems = new TinySearchResultItem [ TinySearch.MAX_AD_ITEM_NUM ];
		}
		this.adItems [ idx ] = item ;
	}
}
