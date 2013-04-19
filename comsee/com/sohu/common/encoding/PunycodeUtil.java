package com.sohu.common.encoding;
public class PunycodeUtil {
	//���������ĺ�׺����
	public static final String[] domainSuffixArray = new String[]{".cn", ".�й�", ".��˾", ".����"};
	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		System.out.println(containHanzi("wo1��w"));
		System.out.println(ChDomain2PunyDomain("�������ơ��й�"));
		System.out.println("Aa".toLowerCase());
	}
	/**
	 * �Ƿ���������ַ�
	 * @param str
	 * @return
	 */
	private static boolean containHanzi(String str) {
		boolean isGB2312 = false;
		if(str==null)
			return isGB2312;
		for (int i = 0; i < str.length(); i++) {
			int temp = str.charAt(i);
			if ( temp > 255 ) {
				isGB2312 = true;
				break;
			}
		}
		return isGB2312;
	}
	/**
	 * ����������ת��ΪС����������������������������������򷵻�null��
	 * ������.cn / ����.�й� / ����.��˾/����.���硱 �����С�.���롰������Ч��
	 * @param HanziStr
	 * @return
	 * @throws Exception 
	 */
	public static String ChDomain2PunyDomain(String ChDomain) throws Exception {
		if(ChDomain == null)
			return null;
		
		//��һ���û�����
		ChDomain = ChDomain.replace("��", ".");//��һ�������ָ��
		ChDomain = ChDomain.replace("��", " ");
		ChDomain = ChDomain.trim();
		ChDomain = ChDomain.toLowerCase();//��תС
		
		//��׺����û��ƥ�䵽��һ��������
		int i;
		for (i = 0; i < domainSuffixArray.length; i++) 
			if(ChDomain.endsWith(domainSuffixArray[i]))
				break;
		if(i==domainSuffixArray.length)
			return null;
		//�������治�ܴ��ո�
		if (ChDomain.indexOf(" ") >= 0) {
			return null;
		}
		
		//����׺������Ĳ����Ƿ��������
		int pos = ChDomain.lastIndexOf(".");
		if(pos == -1)	return null;
		String ChDomainContent = ChDomain.substring(0, pos);
		if(!containHanzi(ChDomainContent))	return null;
		
		//�����Ե�Ϊ��ͷ�ַ��������ж�Ϊ�Ƿ���������
		if(ChDomain.startsWith("."))
			return null;

		String[] ChDomainParts = ChDomain.split("\\.");
		StringBuffer punyDomain_sb = new StringBuffer();
		for (int j = 0; j < ChDomainParts.length; j++) {
			String temp = ChDomainParts[j];
			if(j!=0)
				punyDomain_sb.append(".");
			punyDomain_sb.append( Punycode.encodeUrl(temp) );
		}
		if("".equals( punyDomain_sb.toString() ) )
			return null;
		return "http://"+punyDomain_sb.toString();
	}
	
}
