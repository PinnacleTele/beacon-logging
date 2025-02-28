package com.itextos.beacon.commonlib.stringprocessor.validator;

class StringDataValidator
        implements
        IDataValidator
{

    private final String[] values;

    StringDataValidator(
            String[] aValues)
    {
        values = aValues;
    }

    @Override
    public boolean validate(
            String aValue)
    {
        boolean isValidated = false;

        for (final String temp : values)
        {
            if (DataValidatorImpl.NULL.equals(temp))
                isValidated = (aValue == null);

            if (DataValidatorImpl.EMPTY_STRING.equals(temp))
                isValidated = (aValue != null) && aValue.trim().equals("");

            if ((aValue != null) && temp.startsWith(DataValidatorImpl.OPEN_PARAN) && temp.endsWith(DataValidatorImpl.END_PARAN))
            {
                String t = temp.substring(1);
                t = t.substring(0, t.length() - 1);

                if (aValue.contains(t))
                    isValidated = true;
            }

            if (temp.equalsIgnoreCase(aValue))
                isValidated = true;

            if (isValidated)
                return isValidated;
        }
        return false;
    }

}
