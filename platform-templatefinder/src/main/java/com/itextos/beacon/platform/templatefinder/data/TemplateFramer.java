package com.itextos.beacon.platform.templatefinder.data;

import java.util.ArrayList;
import java.util.List;

import com.itextos.beacon.platform.templatefinder.utility.TemplateUtility;

class TemplateFramer
{

    private final List<String> mNonMatchingTemplates;
    private String             mFramedTemplate = null;

    TemplateFramer(
            List<String> aNonMatchingTemplates)
    {
        mNonMatchingTemplates = aNonMatchingTemplates;
    }

    /**
     * Template generation is purely based on the space separation.
     *
     * @return
     */
    boolean frameTemplate()
    {
        if ((mNonMatchingTemplates == null) || (mNonMatchingTemplates.size() <= 1))
            return false;

        String firstMessage = mNonMatchingTemplates.get(0);
        firstMessage = TemplateUtility.basicReplaceCharacters(firstMessage);

        String lastMessage = mNonMatchingTemplates.get(mNonMatchingTemplates.size() - 1).toLowerCase();
        lastMessage = TemplateUtility.basicReplaceCharacters(lastMessage);

        final String[] firstMessageWordsList = firstMessage.split(" ");
        final String[] lastMessageWordsList  = lastMessage.split(" ");

        if (firstMessageWordsList.length != lastMessageWordsList.length)
            return false;

        final List<Integer> varPositions = new ArrayList<>();

        for (int wordIndex = 0, firstMsgWordCount = firstMessageWordsList.length; wordIndex < firstMsgWordCount; wordIndex++)
            if (!firstMessageWordsList[wordIndex].contentEquals(lastMessageWordsList[wordIndex]))
                varPositions.add(wordIndex);

        if (varPositions.isEmpty())
            return false;

        for (int index = 1, listSize = mNonMatchingTemplates.size(); index < (listSize - 1); index++)
        {
            final String   otherMessage          = mNonMatchingTemplates.get(index);
            final String[] otherMessageWordsList = otherMessage.split(" ");

            if (firstMessageWordsList.length != otherMessageWordsList.length)
                return false;

            for (int wordIndex = 0, firstMsgWordCount = firstMessageWordsList.length; wordIndex < firstMsgWordCount; wordIndex++)

                if (!varPositions.contains(wordIndex) && !firstMessageWordsList[wordIndex].contentEquals(otherMessageWordsList[wordIndex]))
                    return false;
        }

        final String  tempTemplate      = TemplateUtility.frameTemplate(firstMessage, varPositions);
        final boolean lValidateTemplate = TemplateUtility.validateTemplate(tempTemplate);

        if (!lValidateTemplate)
            return false;

        mFramedTemplate = tempTemplate;

        return true;
    }

    String getFramedTemplate()
    {
        return mFramedTemplate;
    }

}