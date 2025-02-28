package com.itextos.beacon.commonlib.stringprocessor.validator;

import java.util.Date;

import com.itextos.beacon.commonlib.utility.DateTimeUtility;

class DateValidator
        implements
        IDataValidator
{

    private final String[] values;
    private final String   dateFormat;

    DateValidator(
            String[] aValues,
            String aDateFormat)
    {
        values     = aValues;
        dateFormat = aDateFormat;
    }

    @Override
    public boolean validate(
            String aValue)
    {

        try
        {

            for (final String temp : values)
            {
                if (DataValidatorImpl.NULL.equalsIgnoreCase(temp))
                    return aValue == null;

                if (temp.startsWith(DataValidatorImpl.OPEN_PARAN) && temp.endsWith(DataValidatorImpl.END_PARAN))
                {
                    if (aValue == null)
                        continue;
                    final String t             = temp.substring(1, temp.length() - 1);

                    final String operator      = t.split("~")[0];
                    final String compareValue  = t.split("~")[1];
                    final Date   value         = DateTimeUtility.getDateFromString(aValue, dateFormat);
                    final Date   matchingValue = DateTimeUtility.getDateFromString(compareValue, dateFormat);

                    if (getValidateData(value, matchingValue, Operator.getEnum(operator)))
                        return true;
                }
            }
        }
        catch (final Exception e)
        {
            e.printStackTrace();
        }
        return false;
    }

    private static boolean getValidateData(
            Date value,
            Date matchingValue,
            Operator operator)
    {

        switch (operator)
        {
            case EQUAL_TO:
                return value.equals(matchingValue);

            case NOT_EQUAL_TO:
                return !value.equals(matchingValue);

            case GREATER_THAN:
                return value.after(matchingValue);

            case LESS_THAN:
                return value.before(matchingValue);

            case GREATER_THAN_EQUAL_TO:
                return value.equals(matchingValue) || value.after(matchingValue);

            case LESS_THAN_EQUAL_TO:
                return value.equals(matchingValue) || value.before(matchingValue);

            default:
                return false;
        }
    }

}
