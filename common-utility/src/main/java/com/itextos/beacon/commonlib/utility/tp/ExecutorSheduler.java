package com.itextos.beacon.commonlib.utility.tp;

public class ExecutorSheduler {

	
    // Public method to get the singleton instance
    public static  ExecutorSheduler getInstance() {
       
        return new ExecutorSheduler();
    }

    // Method to add tasks to the list of tasks
    public void addTask(Runnable task,String threadName) {
    
       Thread.ofVirtual().start(task);
    }

    
}
