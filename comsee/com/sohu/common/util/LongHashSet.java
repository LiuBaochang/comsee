package com.sohu.common.util;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * ��������ʵ��һ����long������Ϊ��ֵ��hash��. 
 * �����㷨Ϊ��ɢ��, ����̽�鷨�����ͻ, ���ṩ�˴��ļ���ȡ�ͱ��浽�ļ��Ĺ���. 
 * @author liumingzhu
 * @version 1.0
 *
 */
public class LongHashSet implements Serializable{

	/**
	 *  magic num
	 */
	private static final long serialVersionUID = 8965073072733792746L;

	private static final int []pows = {
		0,1,4,9,16,25,36,49,64,81,100,121,144,169,196,225,256,
		289,324,361,400,441,484,529,576,625,676,729,784,841,900,961,1024,
		1089,1156,1225,1296,1369,1444,1521,1600,1681,1764,1849,1936,2025,2116,2209,2304,
		2401,2500,2601,2704,2809,2916,3025,3136,3249,3364,3481,3600,3721,3844,3969,4096,
		4225,4356,4489,4624,4761,4900,5041,5184,5329,5476,5625,5776,5929,6084,6241,6400,
		6561,6724,6889,7056,7225,7396,7569,7744,7921,8100,8281,8464,8649,8836,9025,9216,
		9409,9604,9801,10000,
	};
	
	long[] array;
	int capacity;
	int count;
	
	ReentrantReadWriteLock rwlock = new ReentrantReadWriteLock();

	/**
	 * ���캯��. capacityӦ��Ϊһ���Ƚϴ������.
	 * @param capacity
	 */
	public LongHashSet(int capacity){
		array = new long[capacity];
		this.capacity = capacity;
		this.count = 0;
	}
	public LongHashSet( String filename ) throws IOException{
		 readFromFile( filename );
	}
	public int size(){
		return count;
	}
	public int capacity(){
		return capacity;
	}
	/**
	 * ��Hash���浽�ļ���.
	 * @param file
	 * @throws IOException
	 */
	public void writeToFile(String file)throws IOException{
		DataOutputStream os = null;
		rwlock.writeLock().lock();
		try{
			FileOutputStream fis = new FileOutputStream( file );
			os = new DataOutputStream(fis);
			os.writeLong(serialVersionUID);
			os.writeInt( capacity );
			os.writeInt( count );
			
			for( int i=0; i<capacity; i++){
				os.writeLong( array[i] );
			}
			os.writeLong( serialVersionUID );
			
			os.close();
			os = null;
		}finally{
			rwlock.writeLock().unlock();
			if( os != null ){
				try{
					os.close();
				}catch(IOException e){}
				os = null;
			}
		}
	}

	/**
	 * ���ļ��ж�ȡhash��
	 * @param file
	 * @throws IOException
	 */
	public void readFromFile(String file)throws IOException{
		DataInputStream is = null;
		rwlock.writeLock().lock();
		try{
			File f = new File( file );
			long fileLength = f.length();
			if( fileLength > Integer.MAX_VALUE ){
				throw new IOException("Illegal Blacklist-File Format! File Too Large");
			}
			int fileLen = (int)fileLength;
			is = new DataInputStream(
					new FileInputStream( file ) );
			long magic = is.readLong();
			if( magic != serialVersionUID ){
				throw new IOException("Illegal Blacklist-File Format!");
			}
			int capacity = is.readInt();
			int count = is.readInt();
			
			if( ( capacity << 3 ) +24 != fileLen 
					|| count <0 
					|| count > capacity){
				throw new IOException("Illegal Blacklist-File Format! capacity doesn't match file length");
			}
			
			byte [] array = new byte[fileLen - 16]; 
			long[] data = new long[capacity];
			int len = is.read(array);
			if( len != array.length ){
				throw new IOException("ERROR mapping Blacklist-File data! ");
			}
			DataInputStream is2 = new DataInputStream( new ByteArrayInputStream(array, 0, array.length) );
			for( int i=0; i<capacity; i++){
				data[i] = is2.readLong();
			}
			magic = is2.readLong();
			if( magic != serialVersionUID ){
				throw new IOException("Illegal Blacklist-File Format!Bad FileEnd");
			}
			synchronized(this){
				this.array = data;
				this.capacity = capacity;
				this.count = count;
			}
			is2.close();
			is2 = null;
			array = null;
			is.close();
			is = null;
			
		}finally{
			rwlock.writeLock().unlock();

			if( is != null ){
				try{
					is.close();
				}catch( IOException e){}
				is = null;
			}
		}
	}
	
	/**
	 * ��hash�������Ԫ��
	 * @param key
	 * @return <0 ������, �޷����Ԫ��
	 *          0 ��ӳɹ�, ��ԭ����ϣ�����޴���
	 *          1 �ظ����, ԭ����ϣ�������д���
	 */
	public int add( long key ){
		if( key < 0 ) key = -key;
		
		rwlock.writeLock().lock();

		int index = hash(key);
		int ret;
		if( index < 0 ) {
			ret = index;
		} else if( array[index] == 0 ){
			array[index] = key;
			count ++;
			ret = 0;
		} else {
			ret = 1;
		}
		rwlock.writeLock().unlock();
		
		return ret;
	}
	
	/**
	 * ��hash����ɾ��Ԫ��
	 * @param key
	 * @return <0 ������, ��δ�ҵ�Ԫ��
	 *          0 ��δ��, ��δ�ҵ�Ԫ��
	 *          1 �ҵ�Ԫ��, �ҳɹ�ɾ��
	 */
	public synchronized int delete( long key ){
		if( key < 0 ) key = -key;
		
		rwlock.writeLock().lock();

		int index = hash(key);
		int ret;
		if( index < 0 ){
			ret = index;
		} else if( array[index] == 0 ){
			ret = 0;
		} else {
			array[index] = 0;
			ret = 1;
		}
		rwlock.writeLock().unlock();
		return ret;
	}
	
	/**
	 * ���hash�����Ƿ����ĳԪ��
	 * @param key
	 * @return <0 ������, ��δ�ҵ�Ԫ��
	 *          0 ��δ��, ��δ�ҵ�Ԫ��
	 *          1 �ҵ�Ԫ�� 
	 */
	public synchronized int contains( long key ){
		
		if( key < 0 ) key = -key;
		
		rwlock.readLock().lock();

		int index = hash(key);
		int ret;
		if( index < 0 ){
			ret = index;
		} else if( array[index] == 0 ){
			ret = 0;
		} else {
			ret = 1;
		}
		rwlock.readLock().unlock();
		return ret;
	}
	
	/**
	 * hash����
	 * @param key
	 * @return
	 */
	private int hash( long key ){
		if( capacity <= 0 ) return -2;
		
		int index = (int)(key % capacity);
		int offset = 0;
		int loops = capacity >> 1;
		
		for(int delta = 0; delta <= loops ; delta ++ ){
			
			int ret;
			
			if( delta > 100 ){
				offset += (delta << 1) + 1;
			} else {
				offset = pows[delta];
			}
			
			ret = mod(index + offset, capacity);
			
			if( 1520130 == ret ){
				
			}

			if( array[ret] == 0 ){
				return ret;
			} else if( array[ret] == key ){
				return ret;
			}
			
			ret = mod(index - offset, capacity);
			
			if( array[ret] == 0 ){
				return ret;
			} else if( array[ret] == key ){
				return ret;
			}
		}
		return -1;
	}
	private int mod(int val, int capacity){
		int ret = val;
		if( ret < 0 ){
			if( ret == Integer.MIN_VALUE ){
				ret = Integer.MAX_VALUE % capacity;
			} else {
				ret = (-ret) % capacity;
			}
			if( ret != 0 ){
				ret = capacity - ret;
			}
		} else {
			ret %= capacity;
		}
		return ret;
	}
}
