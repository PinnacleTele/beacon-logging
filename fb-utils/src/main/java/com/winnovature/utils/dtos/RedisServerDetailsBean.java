package com.winnovature.utils.dtos;

import redis.clients.jedis.JedisPool;

public class RedisServerDetailsBean {

	String ipAddress = "";
	String mdb = "";
	String password = "";
	String port = "";
	String maxPool = "";
	String maxWait = "";
	String timeout = "";
	String rid = "";
	JedisPool conPool = null;

	public JedisPool getConPool() {
		return conPool;
	}

	public void setConPool(JedisPool conPool) {
		this.conPool = conPool;
	}

	public String getIpAddress() {
		return ipAddress;
	}

	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}

	public String getMdb() {
		return mdb;
	}

	public void setMdb(String mdb) {
		this.mdb = mdb;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getPort() {
		return port;
	}

	public void setPort(String port) {
		this.port = port;
	}

	public String getMaxPool() {
		return maxPool;
	}

	public void setMaxPool(String maxPool) {
		this.maxPool = maxPool;
	}

	public String getMaxWait() {
		return maxWait;
	}

	public void setMaxWait(String maxWait) {
		this.maxWait = maxWait;
	}

	public String getTimeout() {
		return timeout;
	}

	public void setTimeout(String timeout) {
		this.timeout = timeout;
	}

	public String getRid() {
		return rid;
	}

	public void setRid(String rid) {
		this.rid = rid;
	}

	@Override
	public String toString() {
		return "ipAddress:" + ipAddress + "\t mdb:" + mdb + "\t password:"
				+ password + "\t port:" + port + "\t maxPool:" + maxPool
				+ "\t maxWait:" + maxWait + "\t timeout:" + timeout + "\t rid:"
				+ rid;
	}
}
