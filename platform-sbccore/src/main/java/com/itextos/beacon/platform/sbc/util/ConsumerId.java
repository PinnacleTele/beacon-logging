package com.itextos.beacon.platform.sbc.util;

import java.util.ArrayList;
import java.util.List;

public class ConsumerId {

	private static ConsumerId obj=new ConsumerId();
	
	private static List<String> COUNSUMERIDLIST=new ArrayList<String>();
	
	
	private int INDEX=0;
	
	static {
		
		for(int i=1;i<6;i++) {
			
			COUNSUMERIDLIST.add(i+"");
		}
	}
	
	private ConsumerId() {
		
	}
	
	public static ConsumerId getInstance() {
		
		if(obj==null) {
			
			 obj=new ConsumerId();
		}
		
		return obj;
	}
	
	public synchronized String getConsumerId() {
		
		if(INDEX<COUNSUMERIDLIST.size()) {		
			String result= COUNSUMERIDLIST.get(INDEX);
			INDEX++;
			return result;
		}else {
			INDEX=0;
			
			return COUNSUMERIDLIST.get(INDEX);

		}
		

	}
}
