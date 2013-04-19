package com.sohu.common.connectionpool.async;

public class ServerConfig {


/// �������ٴδ����,����sleep����
protected int maxErrorsBeforeSleep = 4;
///	��������󣬶೤ʱ���ڲ������³���
protected int sleepMillisecondsAfterTimeOutError = 30000;
protected int maxConnectionsPerServer = 8;
protected long connectTimeout = 70;
protected long socketTimeout = 10000l;
//socketFailTimeoutֻ�д��ڴ�ֵʱ��������Ż�����ʧ�ܣ��Ӷ�����null
//һ�������socketFailTimeout = socketTimeout
//����ֻҪ��ʱ��fail
protected int maxClonedRequest = 2;

protected long socketFailTimeout = 0l; 

//�Ŷ�ת��ʱ��
protected long queueShortTimeout = 600l;
//�Ŷӳ�ʱʱ��
protected long queueTimeout = 3000l;
protected long robinTime = 500;
protected int maxQueueSize = 10000;

//ƽ����Ӧʱ�����
protected long maxResponseTime = 0l;
protected int maxResponseRadio = 5;

//Ӱ����������
protected long shortRetryTime = 0l;

public long getShortRetryTime() {
	return shortRetryTime;
}

public void setShortRetryTime(long shortRetryTime) {
	this.shortRetryTime = shortRetryTime;
}

public long getMaxResponseTime() {
	return maxResponseTime;
}

public void setMaxResponseTime(long maxResponseTime) {
	this.maxResponseTime = maxResponseTime;
}

public int getMaxResponseRadio() {
	return maxResponseRadio;
}

public void setMaxResponseRadio(int maxResponseRadio) {
	this.maxResponseRadio = maxResponseRadio;
}




String servers;
String name = "Pool";

public String getServers() {
	return servers;
}

public void setServers(String servers) {
	this.servers = servers;
}

public int getMaxClonedRequest() {
	return maxClonedRequest;
}

public void setMaxClonedRequest(int maxClonedRequest) {
	this.maxClonedRequest = maxClonedRequest;
}

public int getMaxErrorsBeforeSleep() {
	return maxErrorsBeforeSleep;
}

public void setMaxErrorsBeforeSleep(int maxErrorsBeforeSleep) {
	this.maxErrorsBeforeSleep = maxErrorsBeforeSleep;
}

public int getSleepMillisecondsAfterTimeOutError() {
	return sleepMillisecondsAfterTimeOutError;
}

public synchronized void setSleepMillisecondsAfterTimeOutError(
		int sleepMillisecondsAfterTimeOutError) {
	this.sleepMillisecondsAfterTimeOutError = sleepMillisecondsAfterTimeOutError;
}

public int getMaxConnectionsPerServer() {
	return maxConnectionsPerServer;
}

public void setMaxConnectionsPerServer(int maxConnectionsPerServer) {
	this.maxConnectionsPerServer = maxConnectionsPerServer;
}

public long getConnectTimeout() {
	return connectTimeout;
}

public void setConnectTimeout(long connectTimeout) {
	this.connectTimeout = connectTimeout;
}

public long getQueueShortTimeout() {
	if (queueShortTimeout == 0l){
		return getQueueTimeout();
	}
	return queueShortTimeout;
}

public void setQueueShortTimeout(long queueShortTimeout) {
	this.queueShortTimeout = queueShortTimeout;
}

public long getSocketTimeout() {
	return socketTimeout;
}

public void setSocketTimeout(long socketTimeout) {
	this.socketTimeout = socketTimeout;
}

public long getSocketFailTimeout() {
	if (socketFailTimeout == 0l){
		return getSocketTimeout();
	}
	return socketFailTimeout;
}

public void setSocketFailTimeout(long socketFailTimeout) {
	this.socketFailTimeout = socketFailTimeout;
}

public String getName() {
	return name;
}

public void setName(String name) {
	this.name = name;
}

public long getQueueTimeout() {
	return queueTimeout;
}

public void setQueueTimeout(long queueTimeout) {
	this.queueTimeout = queueTimeout;
}

public long getRobinTime() {
	return robinTime;
}

public void setRobinTime(long robinTime) {
	this.robinTime = robinTime;
}

/**
 * @return the maxQueueSize
 */
public int getMaxQueueSize() {
	return maxQueueSize;
}

/**
 * @param maxQueueSize the maxQueueSize to set
 */
public void setMaxQueueSize(int maxQueueSize) {
	this.maxQueueSize = maxQueueSize;
}

}
