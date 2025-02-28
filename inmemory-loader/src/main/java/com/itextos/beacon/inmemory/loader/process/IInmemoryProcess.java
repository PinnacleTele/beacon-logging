package com.itextos.beacon.inmemory.loader.process;

public interface IInmemoryProcess
{

    void getDataFromDB();

    void refreshInmemoryData();

	void getDataFromEJBServer();

}