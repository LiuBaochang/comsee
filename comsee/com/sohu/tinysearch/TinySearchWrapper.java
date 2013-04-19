 /*
 * Created on 2005-9-16
 *
 * TinySearch��ѯ���ܵĺ�̨�ӿڡ�
 */
package com.sohu.tinysearch;

import java.util.HashMap;

import com.sohu.tinysearch.async.AsyncTinySearchPool;

/**
 * @deprecated
 * @author Administrator
 *
 * ������TinySearch��Э�鳣�����Լ����ýӿ�
 */
public class TinySearchWrapper {
	
	// #see websearch2/Code/TinySearch/TinySearch.h


	/**
	 * @deprecated
	 */
	public static final String CONTENT_SEPERATOR = "<br>";
/**
    Singer,                 // 1: 00000001 ����
    Song,                   // 2: 00000002 ����
    Image,                  // 3: 00000004 ͼƬ
    News,                   // 4: 00000008 ����
    Blog,                   // 5: 00000010 ����
    Talkbar,                // 6: 00000020 ˵��
    Weather,                // 7: 00000040 ����
    Stock,                  // 8: 00000080 ��Ʊ
    Location,               // 9: 00000100 λ��
    House,                  // 10:00000200 ¥��
    Book,                   // 11:00000400 ͼ��
    Game,                   // 12:00000800 ��Ϸ
    Software,               // 13:00001000 ���
    Word,                   // 14:00002000 ����
    Idiom,                  // 15:00004000 ����
    Eng2Chn,                // 16:00008000 Ӣ��
    AreaCode,               // 17:00010000 ����
    Mobile,                 // 18:00020000 �ֻ���
    PostCode,               // 19:00040000 �ʱ�
    Wikilib,                // 20:00080000 ά��
    People,                 // 21:00100000 ����
    Cate                    // 22:         ����
    Mtime                   // 23:         ��Ӱ 
    TINY_COUNT              // 24:          END
    */
	
    public static final int TINY_CODE_Singer =  1;
        public static final int TINY_CODE_Song =  2;
        public static final int TINY_CODE_Image =  3;
        public static final int TINY_CODE_News =  4;
        public static final int TINY_CODE_Blog =  5;
        public static final int TINY_CODE_Talkbar = 6; 
        public static final int TINY_CODE_Weather =  7;
        public static final int TINY_CODE_Stock =  8;
        public static final int TINY_CODE_Location = 9; 
        public static final int TINY_CODE_House =  10;
        public static final int TINY_CODE_Book =  11;
        public static final int TINY_CODE_Game =  12;
        public static final int TINY_CODE_Software = 13; 
        public static final int TINY_CODE_Word =  14;
        public static final int TINY_CODE_Idiom =  15;
        public static final int TINY_CODE_Eng2Chn =  16;
        public static final int TINY_CODE_AreaCode =  17;
        public static final int TINY_CODE_Mobile =  18;
        public static final int TINY_CODE_PostCode =  19;
        public static final int TINY_CODE_Wikilib =  20;
        public static final int TINY_CODE_People =  21;
        public static final int TINY_CODE_Cate =  22;
        public static final int TINY_CODE_Mtime =  23;
        public static final int TINY_CODE_Car =  24;
        public static final int TINY_CODE_Weather_L =  25;
        public static final int TINY_CODE_Location_L = 26; 
        
	public static final int TINY_AREACODE = 15;
	public static final int TINY_ENG2CHN = 14;
	public static final int TINY_WORD = 13;
	public static final int TINY_HOUSE = 12;
	public static final int TINY_IDIOM = 11;
	public static final int TINY_LOCATION = 10;
	public static final int TINY_MOBILE = 9;
	public static final int TINY_ADDR2POST = 8;
	public static final int TINY_POST2ADDR = 7;
	public static final int TINY_STOCK = 6;
	public static final int TINY_WEATHER = 5;
	public static final int TINY_PEOPLE = 4;
	public static final int TINY_SONG = 3;
	public static final int TINY_ALBUM = 2;
	public static final int TINY_ARTIST = 1;
	public static final int TINY_UNDEF = 0;
	public static final int LOCAL_IP_SEARCH = -1;
	public static final int LOCAL_CALC_SEARCH = -2;
	public static final int LOCAL_CELL_SEARCH = -3;
	public static final int ZIP_SEARCH = TINY_POST2ADDR;
	public static final int DICT_SEARCH = -4;
	
	public static final String ATTRIBUTE_NAME = "tinysearch";
	
	public static final String NORMAL_TINY = "tinysearch.servers";

	public static final String QC_HINT_TINY = "qc_hint.tinysearch.servers";

	public static final String AD_TINY = "ad.tinysearch.servers";

	public static final String HINT_TINY = "hint.tinysearch.servers";

	public static final String QC_TINY = "qc.tinysearch.servers";

	protected static HashMap asyncMap = new HashMap();
	protected static HashMap map = new HashMap();

	public static AsyncTinySearchPool getAsyncInstance(String key, boolean create)
			throws IllegalArgumentException {

		AsyncTinySearchPool pool = (AsyncTinySearchPool) asyncMap.get(key);
		if (create && pool == null) {
			pool = new AsyncTinySearchPool(key);
			asyncMap.put(key, pool);
		}
		return pool;
	}

	/**
	 * ͨ���������������ַ�����ȡ��pool
	 * 
	 * @param key
	 * @return
	 * @throws IllegalArgumentException
	 */
	public static AsyncTinySearchPool getAsyncInstance(String key)
			throws IllegalArgumentException {
		return getAsyncInstance(key, true);
	}

	
	public static TinySearchConnectionPool getInstance(String key , boolean create) throws IllegalArgumentException
	{
		
		TinySearchConnectionPool pool = (TinySearchConnectionPool)map.get(key );
		if( create 
				&& pool == null ){
			pool = new TinySearchConnectionPool();
			pool.setServers(key);
			map.put(key, pool );
		}
		return pool;
	}
	/**
	 * ͨ���������������ַ�����ȡ��pool
	 * @param key
	 * @return
	 * @throws IllegalArgumentException
	 */
	public static TinySearchConnectionPool getInstance( String key ) throws IllegalArgumentException {
		return getInstance( key, true );
	}
	
}
