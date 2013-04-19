package com.sohu.common.blacklist;
/**
 * 
 * @author liumingzhu
 *
 */
public class CharFilter {
	public static final int SPACE = 0;
	public static final int DIGIT = 1;
	public static final int LETTER = 2;
	public static final int HANZI = 3;
	public static final int WHITE = 4;
	
	/*
	 * ���崦��, �� ��ĸ, ����, ���� �ֱ𻮷ֳɲ�ͬ�Ĵ�
	 */
	public static int charType(char ch ){
		if( Character.isWhitespace( ch )){
			return SPACE;
		}
		if( ch < 0x7f ){
			if( Character.isDigit( ch )){
				return DIGIT;
			} else if( Character.isLetter( ch ) ){
				return LETTER;
			} else {
				return WHITE;
			}
		
		} else {
			if(  ch !='��' && ch !='��'
				&& ch !='��' && ch != '��' && ch != '��' 
				&& ch != '��' && ch != '��'	){
				return HANZI;
			} else {
				return WHITE;
			}

		}
	}

}
