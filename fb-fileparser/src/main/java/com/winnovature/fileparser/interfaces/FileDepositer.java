package com.winnovature.fileparser.interfaces;


public interface FileDepositer {
	public String start() throws Exception;

	public void write() throws Exception;

	public void end() throws Exception;
}
