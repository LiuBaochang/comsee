package com.sohu.tinysearch.async;

import java.io.BufferedReader;
import java.io.CharArrayReader;
import java.io.IOException;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;
import java.util.ArrayList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sohu.common.connectionpool.async.AsyncGenericQueryClient;
import com.sohu.common.connectionpool.async.AsyncRequest;
import com.sohu.tinysearch.TinyClientImpl;
import com.sohu.tinysearch.TinySearchRequest;
import com.sohu.tinysearch.TinySearchResult;
import com.sohu.tinysearch.TinySearchResultItem;

public class AsyncTinySearchClient extends AsyncGenericQueryClient {

	// �������������
	private static final int MAX_TINY_ITEMS = 10;

	private static Log log = LogFactory.getLog( TinyClientImpl.class );
	
	private static Charset cs = Charset.forName("GBK");
	private static CharsetEncoder ce = cs.newEncoder();
	private static CharsetDecoder cd = cs.newDecoder();
	
	private ByteBuffer obb = ByteBuffer.allocate( 65536 );
	private ByteBuffer ibb = ByteBuffer.allocate( 65536 );
	private CharBuffer ocb = CharBuffer.allocate( 65536 );
	private CharBuffer icb = CharBuffer.allocate( 65536 );
	
	private String parity;

	protected Log getLogger() {
		return log;
	}

	private static int hash = 0;
	private static synchronized int getHash(){
		return hash++;
	}
	private int ObjectId  = getHash();
	public boolean in =false;
	
	public int sendRequest( ) throws IOException {
		this.life --;
		
		SocketChannel channel = getChannel();
		if( channel == null ) return 0;
		
		TinySearchRequest qr = (TinySearchRequest) this.getRequest();
		this.parity = "parity:" + ObjectId +' '+ qr.getRequestId();

		if( log.isDebugEnabled() ){
			log.debug(parity +" query: begin" );
		}

		ocb.clear();
		
		try{
			ocb.put( "cmd:query\n" );
			ocb.put( "query:" );
			
			String query = null;
			try {
				query = qr.getQuery().trim();
			}catch ( NullPointerException e){}
			// �����ѯ��Ϊһ���ո�����
			
			if( query == null || query.length() == 0 ) return -1;
			
			ocb.put( query );
			
			String reqType = qr.getReqType();
			if( reqType != null ){
				ocb.put("\nreq_type:");
				ocb.put(reqType);
			}
			
			
			//�ж��Ƿ��������뷨������������뷨���������ֵĻ�������Ҫ��ȡ��ѯ����Ϣ
			if ( qr.isIME() ) {
				ocb.put( "\ntinyengine:1");
			}
			
			
			String type = qr.getType();
			if( type != null ){
				ocb.put("\ntype:");
				ocb.put(type);
			}
			
			int pornlevel = qr.getPornlevel();
			
			ocb.put("\npornlevel:");
			ocb.put( String.valueOf(pornlevel) );
			
			String uuid=qr.getUUID();
			if(uuid!=null){
				ocb.put("\nuuid:");
				ocb.put(uuid);
			}
			
			ocb.flip();
			
			obb.clear();
			CoderResult cr = ce.encode( ocb, obb, true );
			
			if( cr.isOverflow() 
					|| cr.isError()	){
				if( log != null && log.isDebugEnabled() ){
					log.debug("suspicious invalid gbk code:"+query);
				}
				return -2;
			}
			obb.put((byte)'\n');
			obb.put((byte)'\n');
	
			obb.flip();
	
			// ��������
			int total_len = obb.remaining();
			// write����0ʧ�ܵĴ���
			int retry = 0;
			try{
				while( obb.remaining() > 0 ){
					long n = channel.write( obb );
					if( n == 0 ) retry ++;
					if( retry > 6 ) {
						// jvm������bug������⵽ĳ��channelʼ�շ��Ͳ���ȥ��
						String msg = "Can't Send Data thru SChannel! Bug maybe... retry " + retry + " times.";
						if( log != null && log.isErrorEnabled() ){
							log.error("luke:" + msg);
						}
						throw new IOException(msg);
					}
				}
			}catch( IllegalArgumentException e){
				// ����jdk��bug, ��bug��1.6.0_u1���޸�.
				throw (IOException)new IOException().initCause(e);
			}
	
			return total_len;
		}catch( BufferOverflowException  e){
			if( log != null && log.isTraceEnabled() ){
				log.trace("TinyBufferOverflow:",e);
			}
		}
		return 0;
	}

	/**
	 * �ӷ����������ղ�ѯ���.
	 * @return
	 */
	public int handleInput() throws IOException
	{
		int n=0;
		try{
			SocketChannel channel = getChannel();
			if( channel != null ){
				n = channel.read(ibb);
			}
		}catch( BufferOverflowException e){
			throw (IOException)new IOException().initCause(e);
		}
		return n;
	}
	public boolean finishResponse() throws IOException{
		
		byte[] b = ibb.array();
		int len = ibb.position();
		boolean finished = false;
		
		if( len > 2 ){
			if( b[len-1]==(byte)'\n'
					&& b[len-2]==(byte)'\n'
					&& b[len-3]==(byte)'\n'){
				finished = true;
			}
		}else {
			if( b[0]==(byte)'\n'
					&& b[1]==(byte)'\n'){
				finished = true;
			}
		}
		
		if( ! finished){
			return false;
		}
		AsyncRequest request = this.getRequest();
		if( request != null )
			request.timeIoend();
		else {
			return true;
		}
		ibb.flip();
		icb.clear();
		CoderResult cr = cd.decode(ibb, icb, true);
		
		while( cr.isError()
				&& ibb.remaining() > 0 
				&& !cr.isOverflow() ){
			ibb.get();
			cr = cd.decode(ibb, icb, true );
		}
		if( cr.isOverflow() 
				|| cr.isError() ){
			request.decodeFailed();
			return true;
		}
		
		icb.flip();

		TinySearchResult result = new TinySearchResult();

		// ////////////////////////////////////////
		// parity code
		boolean matched = false;
		matched = true;

		int itemsonpage = 0;
		// int ad_count = 0;

		// ///////////////////////////////////////////////////////////
		//
		// 1. ����ͷ����Ϣ
		//
		String line = null;

		BufferedReader reader = new BufferedReader(new CharArrayReader(icb
				.array(), 0, icb.remaining() ));

		line = reader.readLine();
		//line = "query_category:0x10:0x01:����	�Ϻ�";

		while (line != null && (line.length() != 0 || matched == false) ) { // ���������е�ʱ�����
			int p = line.indexOf(':');
			String value = "";
			if (p >= 0)
				value = line.substring(p + 1);
			try {
				if (line.startsWith("qc:") || line.startsWith("qr:")) {
					result.setQrHeader(line);
					int q = line.indexOf(':', p + 1);
					result.setQrNum(Integer.parseInt(line.substring(p + 1, q)));
					p = q;
					q = line.indexOf(':', q + 1);
					result
							.setQrType(Integer.parseInt(line
									.substring(p + 1, q)));
					result.setQr(line.substring(q + 1));
				} else if (line.startsWith("hint:")) {
					result.setHintHeader(line);
					int q = line.indexOf(':', p + 1);
					result.setHintNum(Integer
							.parseInt(line.substring(p + 1, q)));
					result.setHint(line.substring(q + 1));
				} else if (line.startsWith("tiny_count:")) {
					itemsonpage = Integer.parseInt(value);
				} else if (line.startsWith("query_category:")){
					/**
					 * 	tinyEngine
					 *	��ʽΪquery_category:0x00a0:0x00020:����\t�Ϻ�
					 *	��һ��Ϊ64bit ������,ÿλΪһ������Ժ���ܻ��ж�����JAVA��Ϊ�з�����������Ҫע�⴦�����
					 *	�ڶ���Ϊ32bit ������ÿλΪһ����־�������ж��С��־��ͬע�⴦�����
					 *	��������Ϊqc���صĲ�ѯ��,��\t�ָ�������2���ؼ��֣���EngineTab������д���
					 */
					result.setQaHeader(line);
					int q = line.indexOf(':', p+1);
//					���ô����
					//��Ϊ0x10000000000..��ʱ�򣬱�ʾ��ѯ��Ϊ�ʴ���
					if (line.substring(p+3,p+4).equalsIgnoreCase("1")) {
						result.setQaCategory((long)-1);
					} else {
						//�����ж�
						result.setQaCategory(Long.decode(line.substring(p+1, q)));
					}
					p = q;
//					���ùؼ�����
					q = line.indexOf(':', p+1);
					
					//������0xFFFFFFFF�������������С��
					Long sign = Long.decode(line.substring(p+1, q));
					if (sign > Integer.MAX_VALUE) {
						result.setQaSignid(-1);
					} else {
						result.setQaSignid(Integer.decode(line.substring(p+1, q)));
					}
					p = q;
//					��ȡ�ؼ���
					result.setQaWord(line.substring(p+1));
				}

				// ////////////////////////////////////////
				// parity code
				else if (line.startsWith("parity:")) {
					if (parity.equals(line)) {
						matched = true;
					} else {
						matched = false;
						if( log != null && log.isFatalEnabled() ){
							log.fatal(parity	+ " ##NON-Matched parity####");
						}
					}
				}
			} catch (NumberFormatException e) {
				// ����
			} catch (IndexOutOfBoundsException e) {
				// ����
			}
			line = reader.readLine();
		}

		int validItemNum = 0;
		int beginIndex;

		if (itemsonpage > 0) {
			if (itemsonpage > MAX_TINY_ITEMS)
				validItemNum = MAX_TINY_ITEMS;
			else
				validItemNum = itemsonpage;
		}
		beginIndex = 0;

		ArrayList items = null;

		if (validItemNum <= 0)
			items = null;
		else
			items = (new ArrayList(validItemNum));

		result.setItems(items);

		int itemindex = 0;
		int resultIndex = 0;

		// /////////////////////////////////////////////////////////////
		//
		// 2. �������tinysearch item
		//
		line = reader.readLine(); // �����к��һ��
		while (line != null && (line.length() != 0 && resultIndex < itemsonpage) ) { // �������������е�ʱ�����

			if (resultIndex >= beginIndex && items != null
					&& items.size() < validItemNum) { // �����Ϊ��Ҫ�Ľ�����һ��д洢�ռ��ʱ��洢

				TinySearchResultItem item = new TinySearchResultItem();
				items.add(item);
				while (line != null && line.length() != 0) { // ���������е�ʱ�����
					int p = line.indexOf(':');
					String value = "";
					if (p >= 0)
						value = line.substring(p + 1);
					if (line.startsWith("type:")) {
						try {
							item.setType(Integer.parseInt(value));
						} catch (NumberFormatException e) {
							// ����
						}
					} else if (line.startsWith("content:")) {
						item.setValue(value);
					}
					line = reader.readLine();
				}
				itemindex++;
			} else {
				while (line != null && line.length() != 0) { // ���������е�ʱ�����
					line = reader.readLine();
				}
			}
			++resultIndex;
			line = reader.readLine(); // �����к��һ��
		}

		// ������������, ����.
		if (line != null && line.length() == 0) {
			request.setResult(result);
			return true;
		}

		// ////////////////////////////////////////////////////////////////////////
		//
		// 3. ����ad��header
		//
		while (line != null && line.length() != 0) { // �������������е�ʱ�����

			int p = line.indexOf(':');
			if (p > 0) {
				// String value = line.substring(p+1);
				if (line.startsWith("ad_count:")) {

					// try{
					// ad_count = Integer.parseInt( value );
					// }catch( NumberFormatException e){
					//							
					// }
				}
			}
			line = reader.readLine(); // �����к��һ��
		}

		// ////////////////////////////////////////////////////////////////////////
		//
		// 4. ����AD����
		//
		line = reader.readLine();
		// ����ad����������ȡ����
		resultIndex = 0;
		while (line != null && line.length() != 0) {
			TinySearchResultItem item = new TinySearchResultItem();
			while (line != null && line.length() != 0) { // ���������е�ʱ�����
				int p = line.indexOf(':');
				String value = "";
				if (p >= 0)
					value = line.substring(p + 1);
				if (line.startsWith("type:")) {
					try {
						item.setType(Integer.parseInt(value));
					} catch (NumberFormatException e) {
						// ����
					}
				} else if (line.startsWith("content:")) {
					item.setValue(value);
				}
				line = reader.readLine();
			}
			result.setAdItem(item.getType(), item);

			line = reader.readLine(); // �����к��һ��
		}

		// ////////////////////////////////////////////////////////////////////////
		// 
		// ����
		//
		// // ��ս��ջ�����
		// while(reader.ready()){
		// line = reader.readLine();
		// log.fatal(parity+" From Hell:"+line);
		// }
		// ///////////////////// test lingyi ///////////////////////

		request.setResult(result);
		return true;
	}


	public void reset() throws IOException {
		this.ibb.clear();
		this.obb.clear();
		setRequest( null );
	}


}
