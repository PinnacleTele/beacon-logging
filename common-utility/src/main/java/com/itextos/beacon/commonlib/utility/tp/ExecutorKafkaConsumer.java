package com.itextos.beacon.commonlib.utility.tp;

public class ExecutorKafkaConsumer {
    // Public method to get the singleton instance
    public static ExecutorKafkaConsumer getInstance() {
            return  new ExecutorKafkaConsumer();
    }

    // Method to add tasks to the list of tasks
    public void addTask(Runnable task,String threadName) {
    	
    	Thread.ofVirtual().start(task);
    }

    
}
