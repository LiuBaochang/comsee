package com.sohu.common.util.text;
//////////////////////////////////////////////////////////////////////
//
//		$Revision: 1.3 $
//		$Author: wangying $
//		$Date: 2006/08/06 02:58:33 $
//
//////////////////////////////////////////////////////////////////////


interface StringMatcher
{
	//ƥ�����ֽ�Ϊ��λ�������ĸ���СΪ256
	public static final int ALPHABET_SIZE = 256;

	/**
	 * 		��ʼ����ؼ���ƥ�����ĺ���
	 * @param keywords[]
	 *				�ؼ�������
	 * @param offset
	 *				��һ����Ҫɨ��Ĺؼ�����keywords[]�е���ʼλ��
	 * @param keywordnum
	 * 				��Ҫɨ��Ĺؼ��ʸ���
	 * 
	 * @return �ɹ�����1�����򷵻�-1
	 */
	public abstract int initialize(byte keywords[][], int offset, int keywordnum);

	/**
	 * 		����ƥ�����ĺ���
	 *
	 */
	public abstract void clear();

	/**
	 * 		��ؼ���ɨ�躯��
	 * @param text
	 * 			��ɨ���Ŀ���ı�
	 * @param offset
	 * 			��text�п�ʼɨ���λ��
	 * @param scanlen
	 * 			��offset��ʼɨ��ĳ���
	 * @return	ɨ����Ϸ���1�����򷵻ظ�ֵ
	 */
	public abstract int search(byte text[], int offset, int scanlen, Object obj);

	/**
	 * 		ÿ�ιؼ�������ʱ�Ĵ���������search()���ã�Ӧ�ó��������д�������
	 * @param iPattern
	 * 			ƥ��Ĺؼ���ID��initialize[offset]��IDΪ0�����ε���
	 * @param iPos
	 * 			ƥ��Ĺؼ���λ�ã�text[offset]��λ��Ϊ0�����ε���
	 * @return
	 * 			������ظ�ֵ��search()��������ֹ�����ı�ɨ�裬�����������ֵ
	 */
	public abstract int hit_report(int iPattern, int iPos, Object obj);
}
