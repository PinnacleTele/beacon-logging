package com.itextos.beacon.httpclienthandover.validator;

public class NumberDataValidator
        implements
        IDataValidator
{

    private final String[] values;

    public NumberDataValidator(
            String[] aValues)
    {
        values = aValues;
    }

    @Override
    public boolean validate(
            String aValue)
    {

        try
        {
            final double doubleValue = Double.parseDouble(aValue);

            for (final String temp : values)
                if (temp.startsWith(DataValidatorImpl.OPEN_PARAN) && temp.endsWith(DataValidatorImpl.END_PARAN))
                {
                    final String t            = temp.substring(1, temp.length() - 1);

                    final String operator     = t.split("~")[0];
                    final String compareValue = t.split("~")[1];

                    // TODO Karthik add tilde separated logic
                    if (getValidateData(doubleValue, Double.parseDouble(compareValue), Operator.getEnum(operator)))
                        return true;
                }
        }
        catch (final Exception e)
        {
            e.printStackTrace();
        }
        return false;
    }

    private static boolean getValidateData(
            double aDoubleValue,
            double matchingValue,
            Operator operator)
    {

        switch (operator)
        {
            case EQUAL_TO:
                if (aDoubleValue == matchingValue)
                    return true;
                return false;

            case NOT_EQUAL_TO:
                if (aDoubleValue != matchingValue)
                    return true;
                return false;

            case GREATER_THAN:
                if (aDoubleValue > matchingValue)
                    return true;
                return false;

            case LESS_THAN_EQUAL_TO:
                if (aDoubleValue <= matchingValue)
                    return true;
                return false;

            case GREATER_THAN_EQUAL_TO:
                if (aDoubleValue >= matchingValue)
                    return true;
                return false;

            case LESS_THAN:
                if (aDoubleValue < matchingValue)
                    return true;
                return false;

            default:
                return false;
        }
    }

}
