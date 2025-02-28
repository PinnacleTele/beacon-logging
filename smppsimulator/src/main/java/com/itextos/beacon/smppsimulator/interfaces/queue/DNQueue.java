package com.itextos.beacon.smppsimulator.interfaces.queue;

import java.util.HashMap;
import java.util.Map;

public class DNQueue {

	final static Map<String,InMemoryQueueWithRepeater> map=new HashMap<>();
	
	public static InMemoryQueueWithRepeater getInstance(String systemId) {
		
		synchronized(map) {
			
			InMemoryQueueWithRepeater result=map.get(systemId);
			
			if(result==null) {
				
				result=new InMemoryQueueWithRepeater(systemId);
				result.startRepeaterProcess();
				
				map.put(systemId, result);
				
			}
			
			return result;
		}
		
	}
	
}
