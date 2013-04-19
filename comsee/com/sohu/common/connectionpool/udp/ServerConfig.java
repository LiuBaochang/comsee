package com.sohu.common.connectionpool.udp;

public class ServerConfig {


/// �������ٴδ����,����sleep����
protected int maxErrorsBeforeSleep = 4;
///	��������󣬶೤ʱ���ڲ������³���
protected int sleepMillisecondsAfterTimeOutError = 6000;
protected int maxConnectionsPerServer = 1;
protected long connectTimeout = 70;
protected long socketTimeout = 100l;
protected long queueTimeout = 1000l;
protected long robinTime = 500;
protected int maxQueueSize = 10000;
protected long minProbeTime = 1000l;
String servers;
String name = "Pool";

public String getServers() {
	return servers;
}

public void setServers(String servers) {
	this.servers = servers;
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

public long getSocketTimeout() {
	return socketTimeout;
}

public void setSocketTimeout(long socketTimeout) {
	this.socketTimeout = socketTimeout;
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

/**
 * @return the minProbeTime
 */
public long getMinProbeTime() {
	return minProbeTime;
}

/**
 * @param minProbeTime the minProbeTime to set
 */
public void setMinProbeTime(long minProbeTime) {
	this.minProbeTime = minProbeTime;
}

}
