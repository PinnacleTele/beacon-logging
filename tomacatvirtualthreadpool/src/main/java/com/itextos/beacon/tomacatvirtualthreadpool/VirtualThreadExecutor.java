package com.itextos.beacon.tomacatvirtualthreadpool;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.catalina.LifecycleException;
import org.apache.catalina.core.StandardThreadExecutor;

public class VirtualThreadExecutor extends StandardThreadExecutor {
    private ExecutorService executorService;

    @Override
    protected void startInternal() throws LifecycleException {
        this.executorService = Executors.newVirtualThreadPerTaskExecutor();
        super.startInternal();
    }

    @Override
    public void execute(Runnable command) {
        executorService.submit(command);
    }

    @Override
    protected void stopInternal() throws LifecycleException {
        executorService.shutdown();
        super.stopInternal();
    }
}
