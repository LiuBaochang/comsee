package com.sohu.common.util;

import java.util.HashMap;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

import org.mozilla.intl.chardet.nsDetector;
import org.mozilla.intl.chardet.nsICharsetDetectionObserver;

import com.sohu.common.encoding.GB2312Charset;
import com.sohu.frontweb.encoding.CharsetConverter;

public class URLUtils {

	public static final String HTTP_PROTOCOL_HEAD = "http://";
	public static final int HTTP_PROTOCOL_HEAD_LENGTH = HTTP_PROTOCOL_HEAD
			.length();
	public static final String HTTPS_PROTOCOL_HEAD = "https://";
	public static final int HTTPS_PROTOCOL_HEAD_LENGTH = HTTPS_PROTOCOL_HEAD
			.length();

	public static final boolean isLetterOrDigit(char c) {
		return ('0' <= c && c <= '9') || ('a' <= c && c <= 'z')
				|| ('A' <= c && c <= 'Z');
	}

	/**
	 * ��ȡurl���������֣�ע�⣺�������ְ����˿ںš�
	 * 
	 * @param url
	 * @return
	 */
	public static final String getDomain(String url) {
		// 0. ��������
		if (url == null)
			return null;

		// 1. �޳�����
		String domain;

		int l = 0;
		if (url.startsWith(HTTP_PROTOCOL_HEAD)) {
			l = HTTP_PROTOCOL_HEAD_LENGTH;
		} else if (url.startsWith(HTTPS_PROTOCOL_HEAD)) {
			l = HTTPS_PROTOCOL_HEAD_LENGTH;
		}
		int slash = url.indexOf('/', l);
		if (slash > 0) {
			domain = url.substring(l, slash);
		} else {
			if (l > 0) {
				domain = url.substring(l);
			} else {
				domain = url;
			}
		}
		return domain;
	}

	public static final String getDomainWithoutPort(String url) {
		String domain = getDomain(url);
		if (domain == null)
			return null;
		int idx = domain.indexOf(':');
		if (idx > 0) {
			return domain.substring(0, idx);
		} else {
			return domain;
		}
	}

	public static final String getDomainWithProtocal(String url) {
		// 0. ��������
		if (url == null)
			return null;

		// 1. �޳�����
		String domain;

		int l = 0;
		if (url.startsWith(HTTP_PROTOCOL_HEAD)) {
			l = HTTP_PROTOCOL_HEAD_LENGTH;
		} else if (url.startsWith(HTTPS_PROTOCOL_HEAD)) {
			l = HTTPS_PROTOCOL_HEAD_LENGTH;
		}

		int slash = url.indexOf('/', l);
		if (slash > 0) {
			domain = url.substring(0, slash);
		} else {
			domain = url;
		}
		return domain;
	}

	public static final String regulateUrl(String url) {
		if (url == null)
			return null;

		boolean needProt = false;
		boolean needTail = false;
		if (!url.startsWith("http://") && !url.startsWith("https://")) {
			needProt = true;
		}
		if (isDomain(url) && !url.endsWith("/")) {
			needTail = true;
		}
		if (needProt) {
			url = "http://" + url;
		}
		if (needTail) {
			url += '/';
		}

		return url;
	}

	/**
	 * �ж�һ���ַ����Ƿ�ΪURL�������ع�һ�����URL�ַ����� ��һ������1.��http://��ͷ; 2.�˿ں�Ϊ80ʱ��Ҫʡ��; 3.
	 * ������ʱ��Ҫ��"/"����β
	 * 
	 * @param query
	 *            ����ַ���
	 * @return �����ΪURLʱ�����ع�һ���Ľ�������򷵻�null��
	 */
	private static final String[] traditionalUrlPostfix = { ".com", ".biz",
			".pro", ".aero", ".coop", ".museum", ".mobi", ".edu", ".gov",
			".info", ".mil", ".name", ".net", ".org", ".jobs", ".travel",
			".mil", ".arpa", ".int", ".cat", ".asia", ".tel" };
	private static final String[] internationalTraditionalUrlPostfix = {
			".com", ".net", ".edu", };
	private static final String[] regionalUrlPostfix = {

	".ac", ".ad", ".ae", ".af", ".ag", ".ai", ".al", ".am", ".an", ".ao",
			".aq", ".ar", ".as", ".at", ".au", ".aw", ".az", ".ba", ".bb",
			".bd", ".be", ".bf", ".bg", ".bh", ".bi", ".bj", ".bm", ".bn",
			".bo", ".br", ".bs", ".bt", ".bv", ".bw", ".by", ".bz", ".ca",
			".cc", ".cd", ".cf", ".cg", ".ch", ".ci", ".ck", ".cl", ".cm",
			".cn", ".co", ".cr", ".cs", ".cu", ".cv", ".cx", ".cy", ".cz",
			".de", ".dj", ".dk", ".dm", ".do", ".dz", ".ec", ".eu", ".fi",
			".fj", ".fk", ".fm", ".fo", ".fr", ".fx", ".ga", ".gb", ".gd",
			".ge", ".gf", ".gh", ".gi", ".gl", ".gp", ".gq", ".gf", ".gm",
			".gn", ".gr", ".gs", ".gt", ".gu", ".gw", ".gy", ".hk", ".hm",
			".hn", ".hr", ".ht", ".hu", ".id", ".ie", ".il", ".in", ".io",
			".iq", ".ir", ".is", ".it", ".jm", ".jo", ".jp", ".ke", ".kg",
			".kh", ".ki", ".km", ".kn", ".kp", ".kr", ".kw", ".ky", ".kz",
			".la", ".lb", ".lc", ".li", ".lk", ".lr", ".ls", ".lt", ".lu",
			".lv", ".ly", ".ma", ".mc", ".md", ".mg", ".mh", ".mk", ".ml",
			".mm", ".mn", ".mo", ".mp", ".mq", ".mr", ".ms", ".mt", ".mu",
			".mv", ".mw", ".mx", ".my", ".mz", ".na", ".nc", ".ne", ".nf",
			".ng", ".ni", ".nl", ".no", ".np", ".nr", ".nt", ".nu", ".nz",
			".om", ".pa", ".pe", ".pf", ".pg", ".ph", ".pk", ".pl", ".pm",
			".pn", ".pt", ".pr", ".pw", ".py", ".qa", ".re", ".ro", ".ru",
			".rw", ".sa", ".sb", ".sc", ".sd", ".se", ".sg", ".sh", ".si",
			".sj", ".sk", ".sl", ".sm", ".sn", ".so", ".sr", ".st", ".su",
			".sv", ".sy", ".sz", ".tc", ".td", ".tf", ".tg", ".th", ".tj",
			".tk", ".tm", ".tn", ".to", ".tp", ".tr", ".tt", ".tv", ".tw",
			".tz", ".ua", ".ug", ".uk", ".um", ".us", ".uy", ".uz", ".va",
			".vc", ".ve", ".vg", ".vi", ".vn", ".vu", ".wf", ".ws", ".ye",
			".yt", ".yu", ".za", ".zm", ".zr", ".zw", ".ad", ".ae", ".af",
			".ag", ".ai", ".al", ".am", ".an", ".ao", ".aq", ".ar", ".as",
			".at", ".au", ".aw", ".az", ".ba", ".bb", ".bd", ".be", ".bf",
			".bg", ".bh", ".bi", ".bj", ".bm", ".bn", ".bo", ".br", ".bs",
			".bt", ".bv", ".bw", ".by", ".bz", ".ca", ".cc", ".cf", ".cg",
			".ch", ".ci", ".ck", ".cl", ".cm", ".cn", ".co", ".cq", ".cr",
			".cu", ".cv", ".cx", ".cy", ".cz", ".de", ".dj", ".dk", ".dm",
			".do", ".dz", ".ec", ".ee", ".eg", ".eh", ".es", ".et", ".ev",
			".fi", ".fj", ".fk", ".fm", ".fo", ".fr", ".ga", ".gb", ".gd",
			".ge", ".gf", ".gh", ".gi", ".gl", ".gm", ".gn", ".gp", ".gr",
			".gt", ".gu", ".gw", ".gy", ".hk", ".hm", ".hn", ".hr", ".ht",
			".hu", ".id", ".ie", ".il", ".in", ".io", ".iq", ".ir", ".is",
			".it", ".jm", ".jo", ".jp", ".ke", ".kg", ".kh", ".ki", ".km",
			".kn", ".kp", ".kr", ".kw", ".ky", ".kz", ".la", ".lb", ".lc",
			".li", ".lk", ".lr", ".ls", ".lt", ".lu", ".lv", ".ly", ".ma",
			".mc", ".md", ".me", ".mg", ".mh", ".ml", ".mm", ".mn", ".mo",
			".mp", ".mq", ".mr", ".ms", ".mt", ".mv", ".mw", ".mx", ".my",
			".mz", ".na", ".nc", ".ne", ".nf", ".ng", ".ni", ".nl", ".no",
			".np", ".nr", ".nt", ".nu", ".nz", ".om", ".pa", ".pe", ".pf",
			".pg", ".ph", ".pk", ".pl", ".pm", ".pn", ".pr", ".pt", ".pw",
			".py", ".qa", ".re", ".ro", ".rs", ".ru", ".rw", ".sa", ".sb",
			".sc", ".sd", ".se", ".sg", ".sh", ".si", ".sj", ".sk", ".sl",
			".sm", ".sn", ".so", ".sr", ".st", ".su", ".sy", ".sz", ".tc",
			".td", ".tf", ".tg", ".th", ".tj", ".tk", ".tl", ".tm", ".tn",
			".to", ".tp", ".tr", ".tt", ".tv", ".tw", ".tz", ".ua", ".ug",
			".uk", ".us", ".uy", ".va", ".vc", ".ve", ".vg", ".vn", ".vu",
			".wf", ".ws", ".ye", ".yu", ".za", ".zm", ".zr", ".zw"

	};
	// A C�����л����� COM�������̡����ڵ�רҵ�� EDU������������ GOV���������ţ� NET����
	// �����硢�����������Ϣ���ĺ��������ģ� ORG
	private static final String[] fixupPostfix = new String[] { ".cn", ".bj",
			".id", ".co", ".il", ".co", ".jp", ".co", ".kr", ".co", ".nr",
			".co", ".uk", ".co", ".uz", ".co", ".cn", ".ac", ".cn", ".com",
			".cn", ".edu", ".cn", ".gov", ".cn", ".net", ".cn", ".org", ".cn",
			".sh", ".cn", ".tj", ".cn", ".cq", ".cn", ".he", ".cn", ".sx",
			".cn", ".nm", ".cn", ".ln", ".cn", ".jl", ".cn", ".hl", ".cn",
			".js", ".cn", ".zj", ".cn", ".ah", ".cn", ".fj", ".cn", ".jx",
			".cn", ".sd", ".cn", ".ha", ".cn", ".hb", ".cn", ".hn", ".cn",
			".gd", ".cn", ".gx", ".cn", ".hi", ".cn", ".sc", ".cn", ".gz",
			".cn", ".yn", ".cn", ".xz", ".cn", ".sn", ".cn", ".gs", ".cn",
			".qh", ".cn", ".nx", ".cn", ".xj", ".cn", ".tw", ".cn", ".hk",
			".cn", ".mo", ".ru", ".net", };
	public static HashMap<String,HashMap<String,Object>> urlPostfixMap = new HashMap<String,HashMap<String,Object>>();
	public static HashMap<String,String> regionalUrlPostfixMap = new HashMap<String,String>();
	public static HashMap<String,String> traditionalUrlPostfixMap = new HashMap<String,String>();
	// ���ж��������б�(����ǰ�ߵ�.)
	public static HashMap<String,HashMap<String,Object>> urlPostfixMap_noDot = new HashMap<String,HashMap<String,Object>>();
	// �������ļ���ҵ����صĶ��������б�(����ǰ�ߵ�.)
	public static HashMap<String,String> regionalUrlPostfixMap_noDot = new HashMap<String,String>();
	// ��������Ĺ��Ҽ����������б�(����ǰ�ߵ�.)
	public static HashMap<String,String> traditionalUrlPostfixMap_noDot = new HashMap<String,String>();

	static {
		for (int i = 0; i < traditionalUrlPostfix.length; i++) {
			if (traditionalUrlPostfix[i] != null) {
				String temp = traditionalUrlPostfix[i].trim();
				traditionalUrlPostfixMap.put(temp, null);
				urlPostfixMap.put(temp, null);
				if (temp.length() > 0) {
					temp = temp.substring(1);
					traditionalUrlPostfixMap_noDot.put(temp, null);
					urlPostfixMap_noDot.put(temp, null);
				}
			}
		}
		for (int i = 0; i < regionalUrlPostfix.length; i++) {
			if (regionalUrlPostfix[i] != null) {
				String temp = regionalUrlPostfix[i].trim();
				regionalUrlPostfixMap.put(temp, null);
				urlPostfixMap.put(temp, null);
				HashMap<String, Object> obj = (HashMap<String, Object>) urlPostfixMap.get(temp);
				for( String international : internationalTraditionalUrlPostfix ){
					if( obj == null ){
						obj = new HashMap<String, Object>();
						urlPostfixMap.put(temp, obj);
					}
					obj.put(international, null);
				}
				if (temp.length() > 0) {
					temp = temp.substring(1);
					regionalUrlPostfixMap_noDot.put(temp.substring(1), null);
					urlPostfixMap_noDot.put(temp, null);
				}
			}
		}
		for (int i = 0; i < fixupPostfix.length && i + 1 < fixupPostfix.length; i += 2) {
			String key = fixupPostfix[i];
			String val = fixupPostfix[i + 1];
			{
				HashMap<String,Object> obj = (HashMap<String,Object>) urlPostfixMap.get(key);
				if (obj == null) {
					obj = new HashMap<String,Object>();
					urlPostfixMap.put(key, obj);
				}
				obj.put(val, null);
			}
			if (key.length() > 0 && val.length() > 0) {
				key = key.substring(1);
				val = val.substring(1);
				HashMap<String,Object> obj = (HashMap<String,Object>) urlPostfixMap_noDot.get(key);
				if (obj == null) {
					obj = new HashMap<String,Object>();
					urlPostfixMap_noDot.put(key, obj);
				}
				obj.put(val, null);
			}
		}
	}

	public static final String URL_PATH_SEPERATOR = "/";
	public static final String URL_HTTP_HEAD = "http://";
	public static final String URL_DOMAIN_SEPERATOR = ".";

	/**
	 * �ж�һ���ַ����Ƿ�ΪURL�������ع�һ�����URL�ַ����� ��һ������1.��http://��ͷ; 2.�˿ں�Ϊ80ʱ��Ҫʡ��; 3.
	 * ������ʱ��Ҫ��"/"����β
	 * 
	 * @param query
	 *            ����ַ���
	 * @return �����ΪURLʱ�����ع�һ���Ľ�������򷵻�null��
	 */
	public static final String getLookupUrl(String query) {
		String temp = query.trim();
		String domain;
		String filePath = URL_PATH_SEPERATOR;

		String protocalHead = HTTP_PROTOCOL_HEAD;

		String tempLower = temp.toLowerCase();
		if (tempLower.startsWith(HTTP_PROTOCOL_HEAD)) {
			protocalHead = HTTP_PROTOCOL_HEAD;
			temp = temp.substring(HTTP_PROTOCOL_HEAD_LENGTH);
		} else if (tempLower.startsWith(HTTPS_PROTOCOL_HEAD)) {
			protocalHead = HTTPS_PROTOCOL_HEAD;
			temp = temp.substring(HTTPS_PROTOCOL_HEAD_LENGTH);
		}
		int idxSlash = temp.indexOf('/');
		int idxColon = temp.indexOf(':');

		int port = 80;
		if (idxSlash < 0) { // ������
			if (idxColon > 0) {
				try {
					port = Integer.parseInt(temp.substring(idxColon + 1));
				} catch (NumberFormatException e) {
					return null;
				}
				domain = temp.substring(0, idxColon);
			} else
				domain = temp;
			filePath = URL_PATH_SEPERATOR;
		} else { // ������Ŀ¼
			if (idxColon > 0 && idxColon < idxSlash) {
				try {
					port = Integer.parseInt(temp.substring(idxColon + 1,
							idxSlash));
				} catch (NumberFormatException e) {
					return null;
				}
				domain = temp.substring(0, idxColon);
			} else {
				domain = temp.substring(0, idxSlash);
			}
			filePath = temp.substring(idxSlash);
		}
		// �ж� port �Ƿ��ںϷ���Χ��
		if (port <= 0 || port > 65535) {
			return null;
		}
		// �ж����������Ƿ�Ϸ�

		domain = validateDomain(domain);
		// ȷ��ΪURL
		if (domain != null) {

			String result;
			if (port == 80) {
				result = protocalHead + domain + filePath;
			} else {
				result = protocalHead + domain + ':' + port + filePath;
			}
			return result;
		}
		return null;
	}

	public static final boolean isIP(String domain){
		if( domain == null ) return false;
		
		boolean isValid = false;
		// �ж�һ��xxx.xxx.xxx.xxx��ʽ��IP��ַ
		try {
			StringTokenizer token = new StringTokenizer(domain,
					URL_DOMAIN_SEPERATOR);
			int i;
			for (i = 0; i < 4; i++) {
				int tempInt = Integer.parseInt(token.nextToken());
				if (tempInt < 0 || tempInt > 255)
					break;
			}
			if (i == 4) {
				if (!token.hasMoreTokens()) {
					// ��֤�ɹ�
					isValid = true;
				}
			}
		} catch (NoSuchElementException e) {
		} catch (NumberFormatException e) {
		}
		return isValid;


	}
	/**
	 * �������������Ƿ����RFC�淶
	 * 
	 * @param domain
	 * @return �������null��˵��������������������ͷ���domain����
	 * 
	 */
	public static final String validateDomain(String domain) {
		if (domain == null)
			return null;

		// �ж��㣺�����Ƿ��ַ�
		for (int i = 0; i < domain.length(); i++) {

			char c = domain.charAt(i);

			if (c > 0x7f) {
				return null;
			} else if (!isLetterOrDigit(c)) {
				// �������ܰ�������, �����԰���'.'��'-'��'_',�Ҳ��������������Ŵ�ͷ���β
				if ((c == '.' && i != 0 && i != domain.length() - 1)
						|| ( (c == '-' || c == '_') && i != 0 && i != domain.length() - 1)) {
					continue;
				} else {
					return null;
				}
			}
		}

		boolean isValid = false;
		do{
			if( isIP(domain) ){
				isValid = true;
				break;
			}
			// �����ж��Ƿ�������������ʽ
			{
				isValid = true;
				// �ж϶�.1��xx.xxxx.com��ʽ������(�ж��ַ���ɵĺϷ���)
				StringTokenizer token = new StringTokenizer(domain,
						URL_DOMAIN_SEPERATOR);
				while (token.hasMoreTokens()) {
					String tok = token.nextToken();
					if (tok.length() == 0 || tok.startsWith(".")
							|| tok.endsWith(".") || tok.startsWith("-")
							|| tok.endsWith("-") || tok.startsWith("_")
							|| tok.endsWith("_")) {
						isValid = false;
						break;
					}
				}
				if( isValid && domain.indexOf("..") >= 0 )
					isValid = false;
				// ������������ʽ������
				if (!isValid)
					break; // do .. while(false);
			}
			// �ж϶���xx.xxxx.com��ʽ������(���ݺ�׺�ж�)
			{
				isValid = false;
				domain = domain.toLowerCase();
				int p = domain.lastIndexOf('.');
				try {
					String postfix = domain.substring(p);
					if (urlPostfixMap.containsKey(postfix)) {
						isValid = true;
						// ��֤�ɹ�����������ִ��������ģ���ж�
						break; // do .. while(false);
					}
				} catch (IndexOutOfBoundsException e) {
				}
			}
		}while(false);
		
		// ȷ��ΪURL
		if (isValid) {
			return domain;
		} else {
			return null;
		}
	}

	/**
	 * �ж�URL�ǲ���������ʽ��URL
	 * 
	 * @param url
	 *            url�����Ƿ��ϱ����е�URL��һ�����������URL
	 * @return
	 */
	public static final boolean isDomain(String url) {
		int t = 0;
		if (url.startsWith(HTTP_PROTOCOL_HEAD)) {
			t = HTTP_PROTOCOL_HEAD_LENGTH;
		} else if (url.startsWith(HTTPS_PROTOCOL_HEAD)) {
			t = HTTPS_PROTOCOL_HEAD_LENGTH;
		}
		t = url.indexOf('/', t);
		if (t < 0 || t == url.length() - 1)
			return true;
		return false;
	}

	/**
	 * �ҳ�url��һ������ һ�������ĸ�ʽ: [a-z0-9]([a-z0-9\-]*[a-z0-9])?\.{��������} ��
	 * [a-z0-9]([a-z0-9\-]*[a-z0-9])?\.{�������ṩ������}.{��������}
	 * 
	 * @param url
	 *            url�����Ƿ��ϱ����е�URL��һ�����������URL
	 * @return �����������һ��url����null, ���򷵻ض�Ӧ�Ķ���������,��:
	 *         "http://www.sogou.com.cn/"����ֵ��"sogou.com.cn"
	 */
	@SuppressWarnings("rawtypes")
	public static final String getMainDomain(String url) {

		String domain = getDomainWithoutPort(url);

		if (domain == null)
			return null;

		HashMap map = urlPostfixMap;
		int lastDot = domain.length();
		int last = lastDot;
		do {
			last = domain.lastIndexOf('.', lastDot - 1);

			// ǰ���Ѿ�û��'.'��
			if (last < 0)
				break;
			// �Ѿ�û�е�n+1��������
			if (map == null)
				break;

			String topDomain = domain.substring(last, lastDot);

			if (!map.containsKey(topDomain))
				break;
			else
				map = (HashMap)map.get(topDomain);
			lastDot = last;
		} while (true);
		if (lastDot == domain.length()) {
			return null; // û�ж�������
		} else {
			if (last < 0) { // xxx.com.cn
				return domain;
			} else { // xxx.domain.com.cn
				return domain.substring(last + 1);
			}
		}
	}

	/**
	 * ���url�Ƿ�Ϊ����www��ͷ��һ������ һ�������ĸ�ʽ: [a-z0-9]([a-z0-9\-]*[a-z0-9])?\.{���ⶥ������} ��
	 * [a-z0-9]([a-z0-9\-]*[a-z0-9])?\.{��ͳ��������}.{������������}
	 * 
	 * @param url
	 *            url�����Ƿ��ϱ����е�URL��һ�����������URL
	 * @return
	 */
	public static final boolean isNonWWW(String url) {

		String domain = getDomainWithoutPort(url);

		if (domain == null)
			return false;

		String mainDomain = getMainDomain(domain);
		return (mainDomain != null && mainDomain.equals(domain));

	}

	/**
	 * ��URL���л�ȡQueryString��
	 * 
	 * @param url
	 *            ������URL��������Э��ͷ���������ֵ�
	 * @return null ���url����Ϊnull����url�в���'?'�ַ�
	 *         �������RFC��׼,���ص�һ��'?'�Ժ󣬵�һ��'#'�м�Ĳ�����ΪQueryString��
	 */
	public static final String getQueryString(String url) {
		if (url == null)
			return null;
		int index = url.indexOf('?');
		if (index < 0) {
			return null;
		}
		index++;
		int hash = url.indexOf('#', index);
		if (hash < 0) {
			return url.substring(index);
		} else {
			return url.substring(index, hash);
		}
	}
	
	private static final boolean checkHexChar(byte[] str, int i){
		if( str == null
				|| i >= str.length
				) return false;
		byte ch1 = str[i];
		return (ch1 >= '0' && ch1 <= '9')
			|| (ch1 >='a' && ch1 <='f')
			|| (ch1 >='A' && ch1 <='F');
	}
	private static final boolean checkMultiHexChar(byte[] str, int idx, int n){
		for(int i=0;i<n;i++){
			if(! checkHexChar(str, idx+i) ) return false;
		}
		return true;
	}
	private static final boolean tryPut(byte[]buff, int idx, byte b){
		if( buff == null || idx < 0 || idx >=buff.length ) return false;
		buff[idx] = b;
		return true;
	}
//	private static final boolean tryMultiPut(byte[]buff, int idx, byte[] b){
//		if( buff == null || b == null || idx < 0 || idx + b.length > buff.length ) return false;
//		System.arraycopy(b, 0, buff, idx, b.length);
//		return true;
//	}

	/**
	 * ͨ��url���룬��%xx�ȱ�����ַ���ֱ��ת�ɶ�Ӧ��byte�������Ǿ������
	 * @see genericUrlDecode(String url, byte[]buff, int flag)
	 * @param url  �������url.
	 * @param buff ���ڴ�Ž�������ݵĻ���
	 * @return
	 *   >=0 �����ɹ���buff�����ݵĳ��� 
	 *   -1 ���������⣬urlΪ��
	 *   -2 ���������⣬buff�ռ䲻��
	 *   -3 δ֪����
	 *   -4 ȷ�ϱ���ΪGBK
	 */
	public static final int genericUrlDecode(String url, byte[]buff ){
		return genericUrlDecode(url, buff, 0);
	}
	/**
	 * ͨ��url���룬��%xxֱ��ת�ɶ�Ӧ��byte�������Ǿ������
	 * ֧��%FF��%uFFFF�����ֱ��뷽����ע�⣬����ָ�ı���ͬ"GBK"��ͬ����Ҫ����
	 * @param url  �������url.
	 * @param buff ���ڴ�Ž�������ݵĻ���
	 * @param flag ��ʽ��չ������
	 *   0��ʾ�����url���봦��
	 *   ��һλΪ1����ʾ��Ҫ����apache��ʽ�ı���
	 * @return
	 *   >=0 �����ɹ���buff�����ݵĳ��� 
	 *   -1 ���������⣬urlΪ��
	 *   -2 ���������⣬buff�ռ䲻��
	 *   -3 δ֪����
	 *   -4 ȷ�ϱ���ΪGBK
	 */
	public static final int genericUrlDecode(String url1, byte[]buff, int flag){
		if (url1 == null || buff == null) {
			return -1;
		}
		byte[] data = url1.getBytes();
		byte[] bb = buff;
		int idx = 0;
		for (int i = 0; i < data.length; i++) { // ��֪�����˱�����ַ����ض�����3���ַ�
			if (data[i] == '%') { // ����%FF��%uFFFF�����
				if (checkMultiHexChar(data, i + 1, 2)) { // ����%ff��ʽ
					try {
						int a = Integer.parseInt(new String(data,i + 1, 2),
								16);
						if (!tryPut(bb, idx, (byte) a))
							return -2;
						idx++;
						i += 2;
					} catch (Exception e) {
						return -3;
					}
				} else if (i + 1 < data.length && data[i + 1] == 'u') { // ����%uFFFF��ʽ
					if (checkMultiHexChar(data, i + 2, 4)) {
						// ����ȷ��Ϊgbk����
						return -4;
					}
				}
			} else if ((flag & 1) == 1) {
				// Ϊapache��ʽ����Ԥ������չ
				if (data[i] == '\\' && i + 1 < data.length
						&& data[i + 1] == 'x') { // ����\xFF��ʽ
					if (checkMultiHexChar(data, i + 2, 2)) {
						try {
							int a = Integer.parseInt(new String(data, i + 2, 2), 16);
							if (!tryPut(bb, idx, (byte) a))
								return -2;
							idx++;
							i += 3;
						} catch (Exception e) {
							return -3;
						}
					}
				}
			} else {
				// ���������ַ�
				if (!tryPut(bb, idx, (byte) data[i]))
					return -2;
				idx ++;
			}
		}

		return idx;
	}

	/**
	 * ����һ��byte�����жϿ��ܵ��ַ�����
	 * ������jchardet�����������ṩ��probe�����ж϶�Ӧ�ı������ͣ���һ����������
	 * ���������ǰ���һ��ͳһ�Ĺ���������ģ�����������Խ�࣬�ж�����ȷ��Խ��
	 * @param s ���ж�������
	 * @return ���ܵı����������飬"UTF-8", "GBK", "UTF-16"�ȵ�.
	 *         ������������⣬����ֵΪnull
	 */
	public static final String[] probeAllCharsets(byte[] s) {
		if( s == null || s.length == 0 ) return null;
		return probeAllCharsets(s,s.length);
	}
	public static final String[] probeAllCharsets(byte[] s, int limit) {
		if( s == null || limit <=0 || limit > s.length ) return null;
		nsDetector det = new nsDetector(nsDetector.SIMPLIFIED_CHINESE);
		nsICharsetDetectionObserver c = null;
		det.Init(c);
		int limitPerIteration = 1024;
		if (limit <= limitPerIteration) {
			det.DoIt(s, limit, false);
			det.Done();

			String prob[] = det.getProbableCharsets();
			return prob;
		}
		byte[] bytes = new byte[limitPerIteration];
		int index = 0;
		while ((index + limitPerIteration) < limit) {
			System.arraycopy(s, index, bytes, 0, limitPerIteration);
			if (det.DoIt(bytes, bytes.length, false)) {
				det.Done();
				String prob[] = det.getProbableCharsets();
				return prob;
			}
			index += limitPerIteration;
		}
		System.arraycopy(s, index, bytes, 0, limit - index);
		det.DoIt(bytes, bytes.length, false);
		det.Done();
		String prob[] = det.getProbableCharsets();

		return prob;
	}
	
	/**
	 * �ж��ַ������ܵı�������
	 * @param line ����������ַ�����һ��Ӧ����url����
	 * @return ���ܵı�������"GBK"��"UTF-8"�ȣ�������ִ��󣬷���ֵ��null
	 */
	public static final String probeCharset(String line){
		String dft = null;
		String ret = null;
		int limit = Integer.MAX_VALUE>>2;
		if( line == null || line.length() == 0 || line.length() >= limit ) 
			return dft;
		byte[] buff = new byte[line.length()*2];
		int n = genericUrlDecode(line, buff);
		if( n >= 0 ){
			ret = probeCharset(buff, 0, n);
			try{
				String oldret = probeCharset2(buff, 0, n);
				if (!ret.equals(oldret)){
					StringBuilder sb = new StringBuilder();
					sb.append("charset diff:");
					sb.append(ret);
					sb.append(" ");
					sb.append(oldret);
					sb.append(" ");
					sb.append(line);
					System.out.println(sb.toString());
				}
			}catch(Exception e){
				System.out.println("charset diff:exception "+line);
			}
		} else if( n == -1 ){
			// ����������
			ret = dft;
		} else if( n == -2 ){ // �ռ䲻�㣬����
			buff = new byte[line.length()*4];
			n = genericUrlDecode(line, buff);
			if( n < 0 ){
				ret = dft;
			} else {
				ret = probeCharset(buff, 0, n);
			}
		} else if( n == -4 ){ // n == -4 ��������ȷ��ΪGBK
			ret = "GBK";
		} else {// n == -3 δ֪����
			ret = dft;
		}
		return ret;
		
	}
	
	private static final float count(String str){
		if( str == null || str.length() == 0 ) return 1.0f;
		int total = 0;
		float sum = 0.0f;
		for(int i=0;i<str.length(); i++){
			char ch = str.charAt(i);
			if( ch > 128 ){
				total ++;
				if( !GB2312Charset.has(ch) ){
					if( ch >= '\ufff0' ){
						sum += 2.0f;
					} else {
						sum += 1.0f;
					}
				} else {
					if( ch < 39 * 8 * 64 ){
						sum += 0.05;
					} else if( ch < 58 * 8 * 64 ){
						sum += 0.07;
					} else if( ch > 80 * 8 * 64 ){
						sum += 0.2;
					}
				}
			}
		}
		return total == 0 ? 1 : (sum)/total;
	}
	
	private static final float count2(String str){
		if( str == null || str.length() == 0 ) return 1.0f;
		int total = 0;
		float sum = 0.0f;
		for(int i=0;i<str.length(); i++){
			char ch = str.charAt(i);
			if( ch > 128 ){
				total ++;
				if( !GB2312Charset.has(ch) ){
					if( ch >= '\ufff0' ){
						sum += 2.0f;
					} else {
						sum += 1.0f;
					}
				} else {
					if( ch < 39 * 8 * 64 ){
						sum += 0.05;
					} else if( ch < 58 * 8 * 64 ){
						sum += 0.07;
					} else if( ch > 80 * 8 * 64 ){
						sum += 0.2;
					} else if( !GB2312Charset.has2312(ch) ){
						sum += 0.15;
					}
				}
			}
		}
		return total == 0 ? 1 : (sum)/total;
	}
	/**
	 * �ж�һ��������Ӧ�ı�������
	 * @param buff ԭʼ������������Խ��Խ׼ȷ����������Ҳ��֮�½�
	 * @param start ��ʼλ��
	 * @param limit �����ĳ���
	 * @return
	 *   null �������󣬻����ж�ʧ��
	 */
	public static final String probeCharset(byte[] bb, int start, int limit){

		if (bb == null || start < 0 || limit <= 0 || limit + start > bb.length) {
			return null;
		}
		byte[] buff = new byte[limit];
		System.arraycopy(bb, start, buff, 0, limit);
		String[] css = probeAllCharsets(buff);
		for (String cs : css) {
			if ( cs.equals("GBK") || cs.equals("GB2312")
					|| cs.equals("GB18030"))
				return "GBK";
			else if( cs.equals("UTF-8") ){
				// fixup
				// ����chardet���ԡ�һ�����ֵ�ʶ�������Ե�����
				{
					try{
						String str = new String( bb, start, limit, cs);
						if (str != null && str.contains("�Ї�")){
							return "UTF-8";
						}
						if (str != null && str.contains("�iv")){
							return "GBK";
						}
						if(count(str) > 0.5){
							cs = "GBK";
						}
						if(count(str) == 0.5){
							try{
								str = new String( bb, start, limit, "GBK");
								if (count(str) < 0.5){
									return "GBK";
								}
							}catch(Exception e){
								cs = "UTF-8";
							}
						}
					}catch(Exception e){
						cs = "GBK";
					}
				}
				return cs;
			}
		}
		return null;

	}
	
	public static final String probeCharset2(byte[] bb, int start, int limit){

		if (bb == null || start < 0 || limit <= 0 || limit + start > bb.length) {
			return null;
		}
		byte[] buff = new byte[limit];
		System.arraycopy(bb, start, buff, 0, limit);
		String[] css = probeAllCharsets(buff);
		String maybeSET = null;
		for (String cs : css) {
			if ( cs.equals("GBK") || cs.equals("GB2312")
					|| cs.equals("GB18030"))
				return "GBK";
			else if( cs.equals("UTF-8") ){
				// fixup
				// ����chardet���ԡ�һ�����ֵ�ʶ�������Ե�����
				{
					try{
						String str = new String( bb, start, limit, cs);
						float cnt = count2(CharsetConverter.getInstance().getGBKOfBig5(str));  
						if (cnt > 0.5){
							return "GBK";
						}
						
						try{
							str = new String( bb, start, limit, "GBK");
							float cnt2 = count2(str);  
							if (cnt2+0.1 < cnt){
								//gbk�Ŀ����Ա�utf8�ߣ������chardetû���жϳ�gbk������Ȼ��utf8
								maybeSET = "UTF-8";
								continue;
							}else if (cnt2 == 0.0 && cnt > 0.0){
								maybeSET = "UTF-8";
								continue;
							}
						}catch(Exception e){
							cs = "UTF-8";
						}
						
					}catch(Exception e){
						cs = "GBK";
					}
				}
				return cs;
			}
		}
		return maybeSET;

	}

	/**
	 * ��url���������ߴ�������url�н�ȡһ���ض��Ĳ�����������"#"ê������
	 * @param url ԭʼ�ַ���
	 * @param param ��Ҫ�ҵĲ���
	 * @return �ҵ��Ķ�Ӧ����
	 *    null ���������ڣ����߲����Ƿ�
	 */
	public static final String getParameter(String url, String param){
		if( url == null || param == null ) return null;
		String key = param + "=";
		int right = url.indexOf("/#");
		if (right < 0) {
			right = url.indexOf('#');
		} else {
			right = url.indexOf('#', right + 2);
		}
		if( right < 0 ) right = url.length();
		int left = -1;
		while(true){
			int idx = url.indexOf(key, left + 1);
			if( idx < 0 ) {
				return null;
			} else if( idx == 0 ) {
				left = idx;
				break;
			} else if( url.charAt(idx-1) == '?' || url.charAt(idx-1) == '&' || url.charAt(idx-1) == '#'){
				left = idx;
				break;
			} else{
				left = idx ;
			}
		}
		// δ�ҵ�
		if( left < 0 ) return null;
		left += key.length();
		// ����#ê��
		if( left >= right ) return null;

		int end = url.indexOf('&', left+1);
		if( end > 0 && end < right ) right = end;
		return url.substring(left, right);
	}
}
