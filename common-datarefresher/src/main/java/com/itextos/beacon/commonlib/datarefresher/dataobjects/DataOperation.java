package com.itextos.beacon.commonlib.datarefresher.dataobjects;

public enum DataOperation
{

    INSERT,
    UPDATE,
    DELETE;

    public static DataOperation getDataOperation(
            int aDataOperation)
    {

        switch (aDataOperation)
        {
            case 1:
                return INSERT;

            case 2:
                return UPDATE;

            case 3:
                return DELETE;

            default:
                break;
        }
        return null;
    }

}
