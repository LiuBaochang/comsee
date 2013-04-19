/**
 * 
 */
package com.sohu.leadsearch.async;

import java.io.IOException;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.CharBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sohu.common.connectionpool.udp.AsyncGenericQueryClient;
import com.sohu.leadsearch.LeadRequest;
import com.sohu.leadsearch.LeadResult;

/**
 * @author liumingzhu
 *
 */
public class AsyncLeadClient extends AsyncGenericQueryClient {
	
	private static Log logger = LogFactory.getLog(AsyncLeadClient.class);

	/// �������ݱ��������ݲ��ֵ���󳤶� 
	private static final int MAX_OUTPUT_DATA_LENGTH = 0x5000;
	/// ��������ݱ��������ݲ��ֵ���󳤶� 
	private static final int MAX_INPUT_DATA_LENGTH = 0x10000;

	private Charset charset = Charset.forName("GBK");
	private CharsetDecoder dec = charset.newDecoder();
	private CharsetEncoder enc = charset.newEncoder();
	
	private CharBuffer ocb = null;
	private CharBuffer icb = null;
	private ByteBuffer ibbBody = null;
	private ByteBuffer ibbHeader = null; 
	private ByteBuffer[] iBuffers = null;

	private ByteBuffer obbBody = null;
	private ByteBuffer obbHeader = null; 
	private ByteBuffer[] oBuffers = null;
	
	public AsyncLeadClient(){
		initBuffers();
	}
	private void initBuffers(){
		ocb = CharBuffer.allocate( MAX_OUTPUT_DATA_LENGTH);
		icb = CharBuffer.allocate( MAX_INPUT_DATA_LENGTH );
		
		ibbBody = ByteBuffer.allocate(  MAX_INPUT_DATA_LENGTH * 2 );
		ibbBody.order(ByteOrder.BIG_ENDIAN);
		ibbHeader = ByteBuffer.allocate( 11 ); 
		ibbHeader.order( ByteOrder.BIG_ENDIAN);
		
		iBuffers = new ByteBuffer[2];
		iBuffers[0] = ibbHeader;
		iBuffers[1] = ibbBody;
		
		obbBody = ByteBuffer.allocate( MAX_OUTPUT_DATA_LENGTH * 2 );
		obbBody.order(ByteOrder.BIG_ENDIAN);
		obbHeader = ByteBuffer.allocate( 11 ); 
		obbBody.order( ByteOrder.BIG_ENDIAN);
		
		oBuffers = new ByteBuffer[2];
		oBuffers[0] = obbHeader;
		oBuffers[1] = obbBody;
	
	}

	/**
	 * �ص�����.
	 * ��Selector�̵߳���.
	 * ע��: һ�����ܲ���Exception
	 */
	@Override
	public int handleInput( ) throws IOException{
		DatagramChannel channel = getChannel();
		if( channel == null ) return -1;

		try{
			long n = channel.read( iBuffers );
			return (int)n;
		}catch( IOException e){
//			if( logger != null && logger.isDebugEnabled() ){
//				logger.debug("lead IOE while read :", e );
//			}
			throw e;
		} finally {
		}
	}
	/**
	 * �ص�����
	 * ��Receiver�̵߳���, ���ڼ���������ȷ��
	 */
	@Override
	protected boolean finishResponse() {
		//	struct ad_response {
		//	DWORD type;         // ������ͣ�ʼ�յ���0x534F5250 (��"SORP")
		//	BYTE  version;      // Э��汾�ţ�Ŀǰ����0x1
		//	DWORD sequence;     // ������кţ���Ҫ���Ӧ������������е����к�һ��
		//	SHORT result_len;   // ���۹��������
		//	CHAR  *result;      // ���۹����
		//	}
		LeadRequest request = (LeadRequest) getRequest();
		if( request == null ){
			return true;
		}
		// ��ʱ
		request.timeIoend();
		int requestSeq = (int)request.getRequestId();
		
		ibbHeader.flip();
		ibbBody.flip();
		
		try {
			int headLen = ibbHeader.remaining();
			// ������������Ƿ���ȷ.
			do {
				if (headLen < 11) {
					if( logger != null && logger.isDebugEnabled() ){
						logger.debug("lead_error: small packet");
					}
					break;
				}
				if (0x534F5250 != ibbHeader.getInt(0)){
					if( logger != null && logger.isDebugEnabled() ){
						logger.debug("lead_error: invalid HEADER");
					}
					break;
				}
				if (0x01 != ibbHeader.get(4)){
					if( logger != null && logger.isDebugEnabled() ){
						logger.debug("lead_error: invalid VERSION");
					}
					break;
				}

				int replyseq = ibbHeader.getInt(5);
				if (replyseq != requestSeq){
					if( logger != null && logger.isDebugEnabled() ){
						logger.debug("lead_error: invalid SEQUENCE: sent:" + requestSeq + ",reply:" + replyseq );
					}
					break;
				}

				int datalen = ibbHeader.getShort(9);
				if( datalen < 0 ) datalen += 1 << 16;
				if (datalen != ibbBody.remaining()){
					if( logger != null && logger.isDebugEnabled() ){
						logger.debug("lead_error: invalid DATALEN");
					}
					break;
				}

				// ���ҽ���һ�������UDP�����õ���Ż��log.
				if( logger != null && logger.isDebugEnabled() ){
					logger.debug("adReply:" + requestSeq + ", time:" + System.currentTimeMillis());
				}
				
				LeadResult lrt;
				if( datalen > 0 ){
					icb.clear();
					CoderResult cr = dec.decode(ibbBody, icb, true);
					while( ibbBody.remaining() > 0 && cr.isError() ){
						ibbBody.get();
						cr = dec.decode(ibbBody, icb, true);
					}
					icb.flip();
					String result = icb.toString();
					lrt = new LeadResult( result );
				} else {
					lrt = new LeadResult( "" );
				}
				request.setResult(lrt);
				return true;
			} while (false);
			return false;
			// ��һ�������Ľṹ�Ƿ�Ϸ�
		} finally {
			obbHeader.flip();
			obbBody.flip();
		}
	}
	/**
	 * ��������.
	 * @param querystr
	 * @return �������ݱ��ı�� >=0 Ϊ�Ϸ�ֵ
	 *         ������Ͳ��ɹ�, �ͻ᷵�� -1.
	 *         -2 request Ϊ��
	 *         -4 Channel��ʱ������
	 *         
	 */
	@Override
	public int sendRequest( ) throws IOException{
		LeadRequest req = (LeadRequest)getRequest();
		
		if( req == null ){
			return -1;
		}
		
		//����ѯ���ǲ���Ϊ��.
		String query = req.getQuery();
		if( query == null 
				|| query.length() ==0){
			return -2;
		}

		int tmpleadId = (int)req.getRequestId();

		/* typedef struct ad_query {
		    DWORD type;      // ������ͣ�ʼ�յ���0x534F5152 (��"SOQR")
		    BYTE version;    // Э��汾�ţ�Ŀǰ����0x1
		    DWORD sequence;  // ������к�
		    SHORT url_len;   // ����URL����
		    CHAR  *url;      // ����URL
		} AD_Q; */
		obbHeader.clear();
		obbBody.clear();
		
		try{
			//��Ϣͷ��
			obbHeader.putInt( 0x534f5152 );// �������
			obbHeader.put((byte)0x01); // Э��汾��
			
			ocb.clear();

			String uri = req.getUri();
			if( uri == null ){
				uri = "ad";
			}
			ocb.put( '/' );
			ocb.put( uri );
			ocb.put( '?' );
			addFatalEncodedParam("&key=", query.trim() );
			ocb.put("&area_code=");
			ocb.put(Integer.toString( req.getArea_code() ));
			addParam( "&pid=", req.getPid(), 32 );
			addParam( "&p=", req.getP(), 32 );
			addParam( "&w=", req.getW(), 32 );
			addParam( "&ip=", req.getIp(), 16 );
			addParam ("&sig=",req.getSig(),128);
			addFatalEncodedParam("&cookie=", req.getLeadcookie() );
			addUnsignedIntParam( "&start=", req.getStart() );
			addUnsignedIntParam( "&page=", req.getPage() );
			addUnsignedIntParam( "&num=", req.getNumber() );
			addEncodedParam("&useragent=", req.getUserAgent() );
			if( req.isDebug() ){
				ocb.put( "&debug=on" );	
			}
			if( req.isInitiate() ){
				ocb.put( "&initiate=1" );
			}
			if( req.getReferer() != null ){
				ocb.put("&refer=" + req.getReferer() );
			}
			if( req.getSuidcookie() != null ){
				ocb.put("&suid="+req.getSuidcookie());
			}
			if( req.getUuid() != null ){
				ocb.put("&uuid="+req.getUuid());
			}
			if( req.getExtra() != null ){
				ocb.put(req.getExtra() );
			}
			ocb.put("&ver=" + req.getVer());
			ocb.put("&policyno=" + req.getPolicyno());
			ocb.put("&qt=" + req.getQt());
			if( req.getXforwardfor() != null)
				addParam( "&forward=", req.getXforwardfor(), 100 );
			ocb.flip();
			
			obbBody.clear();
			CoderResult cr = enc.encode( ocb, obbBody, true );
			while( ocb.remaining() > 0 && cr.isError() ){
				ocb.get();
				cr = enc.encode( ocb, obbBody, true );
			}
			obbBody.flip();
			
			int datalen = obbBody.remaining();
			short len = (short) datalen;

			obbHeader.putInt( tmpleadId );// ������к�
			obbHeader.putShort( len ); // ����URL����
			
			obbHeader.flip();
			
			int totalLen = obbHeader.remaining() + datalen;
			DatagramChannel channel = getChannel();
			if( channel != null ){
				try{
					long remaining = (long) totalLen;
					int retry = 0;
					do {
						long n = channel.write( oBuffers );
						if( n == 0 ) retry ++;
						if( retry > 6 ) {
							// jvm������bug������⵽ĳ��channelʼ�շ��Ͳ���ȥ��
							String msg = "Can't Send Data thru SChannel! Bug maybe... retry " + retry + " times.";
							if( logger != null && logger.isErrorEnabled() ){
								logger.error("luke:" + msg);
							}
							throw new IOException(msg);
						}
						remaining -= n;
					}while( remaining > 0 );
				}catch( IOException e ){
					if( logger != null && logger.isDebugEnabled() ){
						logger.debug("lead IOE while write :" , e );
					}
					throw e;
				}catch( IllegalArgumentException e){
					// ����jdk��bug, ��bug��1.6.0_u1���޸�.
					throw (IOException)new IOException().initCause(e);
				}
			} else {
				return -4;
			}
			if( logger != null && logger.isDebugEnabled() ){
				logger.debug("adsendRequest:"+ tmpleadId );
			}
			return totalLen;
		}catch(NullPointerException e){
		}catch(BufferOverflowException e){
			
		}
		return -1;
	}
	private void addFatalEncodedParam(String prefix, String temp) {
		ocb.put(prefix);
		if (temp != null && temp.length() > 0) {
			try {
				ocb.put(java.net.URLEncoder.encode(temp, "GBK"));
			} catch (Exception e) {
			}
		}
	}
	private void addEncodedParam(String prefix, String temp) {
		if (temp != null && temp.length() > 0) {
			ocb.put(prefix);
			try {
				ocb.put(java.net.URLEncoder.encode(temp, "GBK"));
			} catch (Exception e) {
			}
		}
	}

	private void addParam(String prefix, String temp,int maxLength ) {
		if (temp != null && temp.length() > 0) {
			ocb.put(prefix);
			if( maxLength >= 0 && temp.length() > maxLength ){
				temp = temp.substring(0, maxLength);
			}
			try {
				temp = (java.net.URLEncoder.encode(temp, "GBK"));
			} catch (Exception e) {
				temp = "-";
			}
			ocb.put( temp );
		}
	}
	private void addUnsignedIntParam(String prefix, int temp){
		ocb.put( prefix );
		if( temp <= 0 ){
			ocb.put('0' );
		} else {
			ocb.put( String.valueOf(temp) );
		}
	}

	public void close() {
		DatagramChannel channel = getChannel();
		if( channel != null ){
			try{
				channel.close();
			}catch( IOException e){
				if( logger.isTraceEnabled() ){
					logger.trace("IOE while Close", e);
				}
			}
		}
	}
	public void finalize(){
		close();
	}
	@Override
	protected Log getLogger() {
		return logger;
	}
	@Override
	public void reset() throws IOException {
		ibbHeader.clear();
		ibbBody.clear();
	}

}
