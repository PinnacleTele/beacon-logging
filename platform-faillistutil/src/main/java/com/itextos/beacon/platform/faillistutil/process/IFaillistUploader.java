package com.itextos.beacon.platform.faillistutil.process;

/**
 * An interface to represent the common methods for the
 * <code>International</code> / <code>Domestic</code> process.
 */
public interface IFaillistUploader
{

    /**
     * A method to be invoked to read the records in a CSV file and push it to
     * Redis.
     */
    void process();

    /**
     * A method to return the process type.
     *
     * @return <code>International Process</code> or <code>Domestic Process</code>
     *         based on the process type.
     */
    String getProcessType();

}
