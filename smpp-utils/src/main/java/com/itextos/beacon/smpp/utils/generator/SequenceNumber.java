package com.itextos.beacon.smpp.utils.generator;

public class SequenceNumber
{

    private static final int DEFAULT_VALUE = 1;
    private static final int MAX_VALUE     = Integer.MAX_VALUE;

    private static class SingletonHolder
    {

        static final SequenceNumber INSTANCE = new SequenceNumber();

    }

    public static SequenceNumber getInstance()
    {
        return SingletonHolder.INSTANCE;
    }

    private int value;

    SequenceNumber()
    {
        this.value = 1;
    }

    public synchronized int getNextId()
    {
        final int nextValue = this.value;

        if (this.value == MAX_VALUE)
            this.value = DEFAULT_VALUE;
        else
            ++this.value;

        return nextValue;
    }

}