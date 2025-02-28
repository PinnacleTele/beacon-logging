package com.itextos.beacon.commonlib.utility.tp;

public class ExecutorKafkaProducer {

	

    // Public method to get the singleton instance
    public static  ExecutorKafkaProducer getInstance() {
      
        return new ExecutorKafkaProducer();
    }

    // Method to add tasks to the list of tasks
    public void addTask(Runnable task,String threadName) {
        
    	Thread.ofVirtual().start(task);
    }


}
