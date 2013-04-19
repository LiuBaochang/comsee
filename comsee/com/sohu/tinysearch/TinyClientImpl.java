/*
 * Created on 2003-11-23
 *
 */
package com.sohu.tinysearch;

import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.ArrayList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sohu.common.connectionpool.Request;
import com.sohu.common.connectionpool.Result;

/**
 * ����cache�Ŀͻ���
 * 
 * @author Kirby Zhou
 *  
 */
public class TinyClientImpl extends com.sohu.common.connectionpool.QueryClientImpl {

	// �������������
	private static final int MAX_TINY_ITEMS = 10;

	private static Log log = LogFactory.getLog( TinyClientImpl.class );
	
	private static Charset cs = Charset.forName("GBK");
	private static CharsetEncoder ce = cs.newEncoder();
	
	private ByteBuffer obb = ByteBuffer.allocate(128);
//	private ByteBuffer ibb = ByteBuffer.allocate(1024 );
	private CharBuffer ocb = CharBuffer.allocate( 128 );
//	private CharBuffer icb = CharBuffer.allocate(1024);
	
	private Request request ;
	private String parity;

	private StringBuffer sb = null;
	/**
	 * �½�һ���ͻ���
	 */
	TinyClientImpl() {
	}
	
	protected Log getLogger() {
		return log;
	}

	private static int hash = 0;
	private static synchronized int getHash(){
		return hash++;
	}
	private int ObjectId  = getHash();
	public boolean in =false;
	
	public int handleOutput( ) throws IOException {
		this.life --;
		TinySearchRequest qr = (TinySearchRequest) this.request;
		this.parity = "parity:" + ObjectId +' '+ qr.getRequestId();
		sb = new StringBuffer();
		sb.append( parity + " port:"+ socket.getLocalPort() );

		if( log != null && log.isDebugEnabled() ){
			log.debug(parity +" query: begin" );
		}

		ocb.clear();
		
		ocb.put( "cmd:query\n" );
		ocb.put( "query:" );
		
		String query = null;
		try {
			query = qr.getQuery().trim();
		}catch ( NullPointerException e){}
		// �����ѯ��Ϊһ���ո�����
		
		if( query == null || query.length() == 0 ) return -1;
		
		ocb.put( query );
		ocb.put('\n');
		
		// ����Կ��н���
		ocb.put('\n');
		
		ocb.flip();
		
		int charNum = ocb.limit();
		
		obb.clear();
		ce.encode( ocb, obb, true );
		obb.flip();
		
		int byteNum = obb.limit();
		if( byteNum <charNum ){
			if( logger != null && logger.isDebugEnabled() ){
				logger.debug("suspicious invalid gbk code:"+query);
			}
			return -2;
		}
		
		os.write( obb.array(),0, obb.limit() );
		os.flush();
		
		if ( logger != null && logger.isDebugEnabled() ) {
			logger.debug(parity + '\u0020' + new String(ocb.array(),0, ocb.position()));
		}

		return obb.limit();
	}

	/**
	 * ��cache�������ύ��ѯ
	 * 
	 * @param request
	 * @return
	 */
	public Result query(Request objRequest) throws IOException {
		
		if( objRequest == null ) return null;
		
		long start = System.currentTimeMillis();
		
		// ����ֵ��1
		this.life --;
		
		this.request = objRequest;
		// ����ѯID��Ϊparity
		long requestId = request.getRequestId();
		this.parity = "parity:" + requestId;

		// ���������ҳ�ź�ҳ��, ����ֵ�ĺϷ��Խ��м��

		if( logger != null && logger.isDebugEnabled() ) {
			logger.debug(parity + " query: begin");
		}

		try {
			int sendStatus = handleOutput( );

			Result result = null;
			if( sendStatus > 0 ){
				result = handleInput( );
			}
			
			return result;
			
		}finally{
			if( logger.isDebugEnabled() ){
				logger.debug(parity + " query: end");
			}
			long time = System.currentTimeMillis() - start;
			request.setTime( time );
		}

	}
	/**
	 * �ӷ����������ղ�ѯ���.
	 * @return
	 */
	public Result handleInput() throws IOException
	{
		try{
			TinySearchResult result = new TinySearchResult();

			//////////////////////////////////////////
			// parity code
			boolean matched = false;
			matched = true;

			int itemsonpage = 0;
//			int ad_count = 0;

			/////////////////////////////////////////////////////////////
			//
			// 1. ����ͷ����Ϣ
			//
			String line = null;
			
			line = reader.readLine();

			while (line.length() != 0 || matched==false) { //���������е�ʱ�����
				int p = line.indexOf(':');
				String value = "";
				if (p >= 0)
					value = line.substring(p + 1);
				try {
					if ( line.startsWith("qc:") 
							|| line.startsWith("qr:") ) {
                        result.setQrHeader( line );
                        int q = line.indexOf(':',p+1);
                        result.setQrNum( Integer.parseInt( line.substring(p+1, q) ) );
                        p = q;
                        q = line.indexOf(':', q+1); 
                        result.setQrType( Integer.parseInt( line.substring(p+1, q) ) );
                        result.setQr( line.substring(q + 1) );
                    } else if ( line.startsWith("hint:")) {
                        result.setHintHeader( line );
                        int q = line.indexOf(':',p+1);
                        result.setHintNum( Integer.parseInt( line.substring(p+1, q) ) );
                        result.setHint( line.substring(q + 1) );
					} else if ( line.startsWith("tiny_count:")){
						itemsonpage = Integer.parseInt(value );
					}

					//////////////////////////////////////////
					// parity code
					else if (line.startsWith("parity:")) {
						if( parity.equals(line)){
							matched = true;
						}else{
							matched = false;
							if( logger != null && logger.isFatalEnabled() ){
								logger.fatal(parity +" ##NON-Matched parity#################################");
							}
						}
					}
				} catch (NumberFormatException e) {
					// ����
				} catch (IndexOutOfBoundsException e){
					// ����
				}
				line = reader.readLine();
			}

			int validItemNum = 0;
			int beginIndex;
			
			if( itemsonpage > 0 ){
				if( itemsonpage > MAX_TINY_ITEMS )
					validItemNum = MAX_TINY_ITEMS;
				else
					validItemNum = itemsonpage;
			}
			beginIndex = 0;

			ArrayList items = null;
			
			if( validItemNum <= 0 )
				items = null;
			else
				items = (new ArrayList( validItemNum ));

			result.setItems(items);

			int itemindex = 0;
			int resultIndex  = 0;

			///////////////////////////////////////////////////////////////
			//
			// 2. �������tinysearch item
			//
			line = reader.readLine(); //�����к��һ��
			while (line.length() != 0
					&& resultIndex < itemsonpage
					) { //�������������е�ʱ�����

				if (resultIndex >= beginIndex  
						&& items != null && items.size() < validItemNum ) 
				{ //�����Ϊ��Ҫ�Ľ�����һ��д洢�ռ��ʱ��洢

					TinySearchResultItem item = new TinySearchResultItem();
					items.add( item );
					while (line.length() != 0) { //���������е�ʱ�����
						int p = line.indexOf(':');
						String value = "";
						if (p >= 0)
							value = line.substring(p + 1);
							if (line.startsWith("type:")){
								try {
									item.setType(Integer.parseInt(value));
								} catch (NumberFormatException e) {
									// ����
								}
        					} else if ( line.startsWith("content:")){
        						item.setValue(value);
        					}
						line = reader.readLine();
					}
					itemindex ++;
				} else {
					while (line.length() != 0) { //���������е�ʱ�����
						line = reader.readLine();
					}
				}
				++resultIndex;
				line = reader.readLine(); //�����к��һ��
			}
			
			// ������������, ����.
			if( line.length()==0 ) return result;
			
			//////////////////////////////////////////////////////////////////////////
			//
			// 3. ����ad��header
			//
			while (line.length() != 0) { //�������������е�ʱ�����

				int p = line.indexOf(':');
				if( p>0 ){
//					String value = line.substring(p+1);
					if( line.startsWith("ad_count:")){
						
//						try{
//							ad_count = Integer.parseInt( value );
//						}catch( NumberFormatException e){
//							
//						}
					}
				}
				line = reader.readLine(); //�����к��һ��
			}

			//////////////////////////////////////////////////////////////////////////
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
				result.setAdItem( item.getType(), item);
				
				line = reader.readLine(); // �����к��һ��
			}

			//////////////////////////////////////////////////////////////////////////
			// 
			//  ����
			//
//			// ��ս��ջ�����
//			while(reader.ready()){
//				line = reader.readLine();
//					logger.fatal(parity+" From Hell:"+line);
//			}
			/////////////////////// test lingyi ///////////////////////

			return result;
		} catch (NullPointerException e) {
			logger.fatal(parity,e);
			close();
			throw (EOFException) ((new EOFException(
					"unexpected socket EOF!")).initCause(e));
		} catch (IOException e) {
			logger.debug(parity,e);
			close();
			throw e;
//		} catch (Exception e) {
//			logger.debug(parity,e);
//			close();
//			throw (IOException) ((new IOException(
//					"Reached a unexpected exception")).initCause(e));
		}finally{
			logger.fatal(parity+" query: end");
		}
	}
	/**
	 * �ر�����
	 */
	public synchronized void close() {
		try {
			if (os != null){
				try{
					os.close();
				}catch(Exception e){
					logger.debug(this,e);
				}
			}
			if (reader != null){
				try{
					reader.close();
				}catch(Exception e){
					logger.debug(this,e);
				}
			}
			
			if (socket != null) {
				logger.debug( "TinySocketClose:"+socket.getLocalPort() );
				try {
					socket.close();
				} catch (Exception e) {
					logger.debug(this, e);
				}
			}
			if( sb!= null ){
				sb.append(" tiny client to be closed :");
				logger.debug( sb.toString() );
				sb = null;
			}
		} finally {
			if (socket != null) {
				if (!socket.isClosed())
					logger.debug("EERROR:socket: NOT isClosed");
				if (socket.isConnected())
					logger.debug("EERROR:socket:isConnected");
			} else {
				logger.debug("EERROR:socket:==NULL");
			}
			reader = null;
			os = null;
			socket = null;
		}
	}


}

