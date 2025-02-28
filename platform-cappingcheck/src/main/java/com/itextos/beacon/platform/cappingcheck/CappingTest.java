package com.itextos.beacon.platform.cappingcheck;

public class CappingTest
{

    public static void main(
            String[] args)
    {

        for (int i = 1; i <= 10; i++)
        {
            final boolean canMsgProcess = CappingMessageChecker.doCappingCheck("6000000200000000", 9, 2);

            System.out.println("Can ProcessMsg :" + canMsgProcess);

            // TODO Auto-generated method stub
            final boolean canProcess = CappingMessageChecker.increaseMsgCounter("6000000200000000", CappingIntervalType.HOUR, 5, 9, 2);

            System.out.println("Can Process:" + canProcess);
        }
    }

}
