package com.itextos.beacon.commonlib.utility.tp;

public class ExecutorSheduler2 {

	
    // Public method to get the singleton instance
    public static  ExecutorSheduler2 getInstance() {
       
        return new ExecutorSheduler2();
    }

    // Method to add tasks to the list of tasks
    public void addTask(Runnable task,String threadName) {
    
       Thread.ofVirtual().start(task);
    }

    
}
