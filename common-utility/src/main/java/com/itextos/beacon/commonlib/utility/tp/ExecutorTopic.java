package com.itextos.beacon.commonlib.utility.tp;

public class ExecutorTopic {


    public static ExecutorTopic getInstance() {
    
        return  new ExecutorTopic();
    }

    // Method to add tasks to the list of tasks
    public void addTask(Runnable task,String threadName) {
        
    	Thread.ofVirtual().start(task);
    	//new Thread(task).start();
    }

    
}
