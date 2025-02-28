package com.itextos.beacon.platform.templatefinder.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.apache.commons.lang.StringUtils;

import com.itextos.beacon.platform.templatefinder.utility.TemplateUtility;

public class TemplateContents
        implements
        Serializable
{

    private static final long  serialVersionUID = 8059278348914883857L;

    private final String       mTemplateId;
    private final String       mTemplate;
    private final String       mStrippedTemplateContent;
    private boolean            isStartsWithVar  = false;
    private boolean            isEndsWithVar    = false;
    private boolean            isContainsVar    = false;
    private String             mFirstWord       = null;
    private String             mLastWord        = null;
    private final List<String> allWordsOnly     = new ArrayList<>();

    TemplateContents(
            String aTemplateId,
            String aTemplate)
    {
        mTemplateId              = aTemplateId;
        mTemplate                = aTemplate.toLowerCase();
        mStrippedTemplateContent = replaceRegex();
        wordFinder();
    }

    private String replaceRegex()
    {
        final String        replacedTemplate   = TemplateUtility.replaceCharacters(mTemplate, true, false);
        final String[]      finalStringToSplit = StringUtils.splitByWholeSeparatorPreserveAllTokens(replacedTemplate, TemplateUtility.CONSTANT_VAR_VARIABLE);
        int                 continousVarCount  = 1;

        final StringBuilder sb                 = new StringBuilder();
        if (finalStringToSplit.length == 1)
            return finalStringToSplit[0];

        for (int i = 0; i < (finalStringToSplit.length - 1); i++)
        {
            final String word     = finalStringToSplit[i];
            final String nextWord = finalStringToSplit[i + 1];

            {
                final String temp = StringUtils.strip(word);
                sb.append(temp);
                addToWordsList(temp);
            }

            if (StringUtils.isBlank(nextWord))
            {
                if ((finalStringToSplit.length - 2) == i)
                    sb.append(".{0," + (continousVarCount * 30) + "}");
                continousVarCount++;
            }
            else
            {
                sb.append(".{0," + (continousVarCount * 30) + "}");

                if ((finalStringToSplit.length - 2) == i)
                {
                    final String temp = StringUtils.strip(nextWord);
                    sb.append(temp);
                    addToWordsList(temp);
                }
                continousVarCount = 1;
            }
        }
        return sb.toString();
    }

    private void addToWordsList(
            String aTemp)
    {
        if (aTemp.isBlank())
            return;

        allWordsOnly.add(aTemp.trim());
    }

    private void wordFinder()
    {
        final String replacedTemplate = TemplateUtility.replaceCharacters(mTemplate, true, true);
        isContainsVar = (replacedTemplate.indexOf(TemplateUtility.CONSTANT_VAR_VARIABLE) != -1);

        if (isContainsVar)
        {
            isStartsWithVar = replacedTemplate.startsWith(TemplateUtility.CONSTANT_VAR_VARIABLE);
            isEndsWithVar   = replacedTemplate.endsWith(TemplateUtility.CONSTANT_VAR_VARIABLE);
        }
        // TODO Check if ends with comes like this {#var#}.
        final String[] firstAndLastWords = TemplateUtility.findFirstAndLastWord(replacedTemplate);
        if (!isStartsWithVar && (firstAndLastWords[0] != null) && !firstAndLastWords[0].contains(TemplateUtility.CONSTANT_VAR_VARIABLE))
            mFirstWord = firstAndLastWords[0];

        if (!isEndsWithVar && (firstAndLastWords[1] != null) && !firstAndLastWords[1].contains(TemplateUtility.CONSTANT_VAR_VARIABLE))
            mLastWord = firstAndLastWords[1];
    }

    public String getStrippedTemplateContent()
    {
        return mStrippedTemplateContent;
    }

    boolean isStartsWithVar()
    {
        return isStartsWithVar;
    }

    boolean isEndsWithVar()
    {
        return isEndsWithVar;
    }

    boolean isContainsVar()
    {
        return isContainsVar;
    }

    String getFirstWord()
    {
        return mFirstWord;
    }

    String getLastWord()
    {
        return mLastWord;
    }

    public String getTemplate()
    {
        return mTemplate;
    }

    String getTemplateId()
    {
        return mTemplateId;
    }

    public List<String> getAllWordsOnly()
    {
        return allWordsOnly;
    }

    public int getStaticWordsCount()
    {
        return allWordsOnly.size();
    }

    @Override
    public int hashCode()
    {
        final int prime  = 31;
        int       result = 1;
        result = (prime * result) + ((mTemplate == null) ? 0 : mTemplate.hashCode());
        result = (prime * result) + ((mTemplateId == null) ? 0 : mTemplateId.hashCode());
        return result;
    }

    @Override
    public boolean equals(
            Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final TemplateContents other = (TemplateContents) obj;

        if (!Objects.equals(mTemplate, other.mTemplate))
            return false;

        if (!Objects.equals(mTemplateId, other.mTemplateId))
            return false;
        return true;
    }

    @Override
    public String toString()
    {
        return "TemplateContents [mTemplateId=" + mTemplateId + ", mTemplate=" + mTemplate + ", mStrippedTemplateContent=" + mStrippedTemplateContent + ", isStartsWithVar=" + isStartsWithVar
                + ", isEndsWithVar=" + isEndsWithVar + ", isContainsVar=" + isContainsVar + ", mFirstWord=" + mFirstWord + ", mLastWord=" + mLastWord + ", allWordsOnly=" + allWordsOnly + "]";
    }

}