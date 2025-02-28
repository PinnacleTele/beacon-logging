package com.itextos.beacon.commonlib.utility.timer;

public interface ITimedProcess
{

    boolean canContinue();

    /**
     * This will be called every timeout of the program
     *
     * @return <code>false</code> if needs to sleep in between the next execution.
     *         <code>true</code> continue the process.
     */
    boolean processNow();

    void stopMe();

}
