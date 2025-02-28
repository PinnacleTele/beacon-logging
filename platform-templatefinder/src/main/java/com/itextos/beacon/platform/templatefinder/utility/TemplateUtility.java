package com.itextos.beacon.platform.templatefinder.utility;

import java.util.List;
import java.util.StringJoiner;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.platform.templatefinder.Result;

public final class TemplateUtility
{

    public static final String  CONSTANT_VAR_VARIABLE       = "{#var#}";
    public static final String  CONSTANT_HEX_VAR_VARIABLE   = "007B00230076006100720023007D";

    private static final String NON_MATCHING_TEMPLATES      = "non_match";
    private static final String TO_REPLACE                  = DltTemplateProperties.getInstance().getReplaceChars(false);
    private static final String TO_REPLACE_WITHOUT_SPACE    = DltTemplateProperties.getInstance().getReplaceChars(true);
    private static final String CONSTANT_VAR_VARIABLE_NAME  = "winnunitextos";

    private static int          mNonMatchingTemplateCounter = 0;

    private TemplateUtility()
    {}

    public static String getTemplateKey(
            String aTemplateGroupId,
            String aHeader)
    {
        return CommonUtility.combine(aTemplateGroupId, aHeader);
    }

    public static String basicReplaceCharacters(
            String aString)
    {
        String s = aString.toLowerCase();

        s = StringUtils.replaceChars(s, "\r\n", "");

        return s;
    }

    public static String replaceCharacters(
            String aString,
            boolean aReplaceVarVariables,
            boolean aSkipSpace)
    {
        String s = basicReplaceCharacters(aString);

        if (aReplaceVarVariables)
            s = StringUtils.replace(s, CONSTANT_VAR_VARIABLE, CONSTANT_VAR_VARIABLE_NAME);

        s = StringUtils.replaceChars(s, aSkipSpace ? TO_REPLACE_WITHOUT_SPACE : TO_REPLACE, "");

        if (aReplaceVarVariables)
            s = StringUtils.replace(s, CONSTANT_VAR_VARIABLE_NAME, CONSTANT_VAR_VARIABLE);

        // Remove white spaces in between
        s = StringUtils.normalizeSpace(s);

        return s;
    }

    public static String frameTemplate(
            String aMessage,
            List<Integer> aVarPositions)
    {
        final StringJoiner sj               = new StringJoiner(" ");
        final String[]     messageWordsList = aMessage.split(" ");

        for (int wordIndex = 0, firstMsgWordCount = messageWordsList.length; wordIndex < firstMsgWordCount; wordIndex++)
            if (!aVarPositions.contains(wordIndex))
                sj.add(messageWordsList[wordIndex]);
            else
                sj.add(TemplateUtility.CONSTANT_VAR_VARIABLE);
        return sj.toString();
    }

    public static boolean validateTemplate(
            String aTemplate)
    {
        final String[] lSplit = StringUtils.split(aTemplate, CONSTANT_VAR_VARIABLE);
        return lSplit.length > DltTemplateProperties.getInstance().getNonMatchingMessageCreateTemplateMinWordCount();
    }

    public static String[] splitAllWords(
            String aTemplate)
    {
        return StringUtils.splitByWholeSeparatorPreserveAllTokens(aTemplate, CONSTANT_VAR_VARIABLE);
    }

    public static String[] findFirstAndLastWord(
            String aString)
    {
        final int firstBreak = aString.indexOf(' ');
        final int lastBreak  = aString.lastIndexOf(' ');
        String    firstWord  = null;
        String    lastWord   = null;

        if (firstBreak > 0)
        {
            firstWord = aString.substring(0, firstBreak);

            final int varStartPosition = firstWord.indexOf(CONSTANT_VAR_VARIABLE);

            if ((varStartPosition == 0) || (firstWord.trim().length() == 0))
                firstWord = null;
        }

        if ((lastBreak > 0) && (lastBreak > firstBreak))
        {
            lastWord = aString.substring(lastBreak + 1);

            if (lastWord.endsWith(CONSTANT_VAR_VARIABLE) || (lastWord.trim().length() == 0))
                lastWord = null;
        }

        if ((lastBreak == -1) && (firstBreak == -1))
        {
            firstWord = aString;
            lastWord  = aString;
        }

        return new String[]
        { firstWord, lastWord };
    }

    public static Result isTemplateMatch(
            Pattern pattern,
            String aMessage)
    {
        Result        returnValue  = Result.TEMPLATE_FOUND;
        final boolean patternMatch = pattern.matcher(aMessage).matches();

        if (!patternMatch)
            returnValue = Result.TEMPLATE_NOT_FOUND;
        return returnValue;
    }

    public static String generateNewId(
            String aTemplateGroupId,
            String aHeader)
    {
        mNonMatchingTemplateCounter++;
        return new StringJoiner("_").add(NON_MATCHING_TEMPLATES).add(aTemplateGroupId).add(aHeader).add(mNonMatchingTemplateCounter + "").toString();
    }

}