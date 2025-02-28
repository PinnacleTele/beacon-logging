package com.itextos.beacon.commonlib.dnddataloader.compare;

public class Test
{

    public static void main(
            String[] args)
    {
        final String dest     = "919884227203";
        final String outerKey = dest.substring(2, 7);
        final String innerKey = dest.substring(7);

        System.out.println(new java.util.Date() + " - Test main dest : '" + dest + "' outer : '" + outerKey + "' inner : '" + innerKey + "'");
    }

}
