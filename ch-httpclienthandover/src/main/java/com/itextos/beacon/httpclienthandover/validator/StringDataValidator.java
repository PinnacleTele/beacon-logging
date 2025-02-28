package com.itextos.beacon.httpclienthandover.validator;

public class StringDataValidator
        implements
        IDataValidator
{

    private final String[] values;

    public StringDataValidator(
            String[] aValues)
    {
        values = aValues;
    }

    @Override
    public boolean validate(
            String aValue)
    {

        for (final String temp : values)
        {
            if (DataValidatorImpl.EMPTY_STRING.equals(temp))
                return (aValue != null) && aValue.trim().equals("");

            if (DataValidatorImpl.NULL.equals(temp))
                return aValue == null;

            if ((aValue != null) && temp.startsWith(DataValidatorImpl.OPEN_PARAN) && temp.endsWith(DataValidatorImpl.END_PARAN))
            {
                String t = temp.substring(1);
                t = t.substring(0, t.length() - 1);

                if (aValue.contains(t))
                    return true;
            }

            if (temp.equalsIgnoreCase(aValue))
                return true;
        }
        return false;
    }

}
