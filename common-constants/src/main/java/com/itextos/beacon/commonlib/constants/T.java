package com.itextos.beacon.commonlib.constants;

public class T {

	public static void main(String[] args) {


		 try
	        {
	            final DCS dcs = DCS.getDcs(-16);
	            dcs.getKey();
	            System.out.println(dcs.getKey());
	        }
	        catch (final Exception e)
	        {
	          e.printStackTrace();
	        }
	}

}
