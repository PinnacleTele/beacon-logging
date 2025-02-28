package com.itextos.beacon.platform.templatefinder.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.prometheusmetricsutil.PrometheusMetrics;
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.platform.templatefinder.Result;
import com.itextos.beacon.platform.templatefinder.TemplateResult;
import com.itextos.beacon.platform.templatefinder.TemplateScrubber;
import com.itextos.beacon.platform.templatefinder.utility.TemplateUtility;

class TemplateInfo
        implements
        Serializable

{

    private static final long               serialVersionUID                    = 2688745499000581570L;

    private static final Log                log                                 = LogFactory.getLog(TemplateInfo.class);

    private final Map<String, Pattern>      mTemplateContents                   = new HashMap<>();
    private final Map<String, List<String>> mTemplateWordsList                  = new HashMap<>();
    private final Map<String, List<String>> mWordStartsAndEndsWith              = new HashMap<>();
    private final Map<String, List<String>> mWordStarts                         = new HashMap<>();
    private final Map<String, List<String>> mWordEnds                           = new HashMap<>();
    private final List<String>              mStartsAndEndsWithVar               = new ArrayList<>();
    private final List<String>              mStartsWithVar                      = new ArrayList<>();
    private final List<String>              mEndsWithVar                        = new ArrayList<>();
    private final List<String>              mRemainingTemplates                 = new ArrayList<>();
    private final Map<String, String>       mNoVarTemplates                     = new HashMap<>();
    private final Map<String, Integer>      mMostUsedTemplates                  = new ConcurrentHashMap<>();
    private final BlockingQueue<String>     mNonMatchingTemplates               = new LinkedBlockingQueue<>();
    private final RecentlyUsedTemplates     mRecentlyUsedTemplates              = new RecentlyUsedTemplates();
    private final NonMatchingMessage        mNonMatchingMessages                = new NonMatchingMessage();

    private final String                    mTemplateGroupId;
    private final String                    mHeader;

    private boolean                         isInUseNonMatchingTemplateList      = false;
    private boolean                         markForClearNonMatchingTemplateList = false;

    TemplateInfo(
            String aTemplateGroupId,
            String aHeader)
    {
        mTemplateGroupId = aTemplateGroupId;
        mHeader          = aHeader;
    }

    synchronized void addTemplateContentInfo(
            TemplateContents aTemplateContents)
    {
        final String templateId = aTemplateContents.getTemplateId();
        mTemplateContents.put(templateId, Pattern.compile(aTemplateContents.getStrippedTemplateContent(), Pattern.CASE_INSENSITIVE));
        mTemplateWordsList.put(templateId, aTemplateContents.getAllWordsOnly());

        addToWordBasedMaps(aTemplateContents);

        if (aTemplateContents.isStartsWithVar() && aTemplateContents.isEndsWithVar() && !mStartsAndEndsWithVar.contains(templateId))
            mStartsAndEndsWithVar.add(templateId);

        if (aTemplateContents.isStartsWithVar() && !mStartsWithVar.contains(templateId))
            mStartsWithVar.add(templateId);

        if (aTemplateContents.isEndsWithVar() && !mEndsWithVar.contains(templateId))
            mEndsWithVar.add(templateId);

        if (!aTemplateContents.isStartsWithVar() && !aTemplateContents.isEndsWithVar() && aTemplateContents.isContainsVar() && !mRemainingTemplates.contains(templateId))
            mRemainingTemplates.add(templateId);

        if (!aTemplateContents.isContainsVar())
            mNoVarTemplates.put(aTemplateContents.getStrippedTemplateContent(), templateId);
    }

    private void addToWordBasedMaps(
            TemplateContents aTemplateContents)
    {
        final String templateId = aTemplateContents.getTemplateId();

        if ((aTemplateContents.getFirstWord() != null) && (aTemplateContents.getLastWord() != null))
        {
            final String       firstLastWord = CommonUtility.combine(aTemplateContents.getFirstWord(), aTemplateContents.getLastWord());
            final List<String> list          = mWordStartsAndEndsWith.computeIfAbsent(firstLastWord, k -> new ArrayList<>());
            if (!list.contains(templateId))
                list.add(templateId);
        }

        if (aTemplateContents.getFirstWord() != null)
        {
            final List<String> list = mWordStarts.computeIfAbsent(aTemplateContents.getFirstWord(), k -> new ArrayList<>());
            if (!list.contains(templateId))
                list.add(templateId);
        }

        if (aTemplateContents.getLastWord() != null)
        {
            final List<String> list = mWordEnds.computeIfAbsent(aTemplateContents.getLastWord(), k -> new ArrayList<>());
            if (!list.contains(templateId))
                list.add(templateId);
        }
    }

    private void addNonMatchingTemplate(
            String aMessage)
    {
        final String lNonMatchingTemplate = mNonMatchingMessages.addNonMatchingTemplate(aMessage);

        if (lNonMatchingTemplate != null)
        {
            final String           newNonTemplateId  = TemplateUtility.generateNewId(mTemplateGroupId, mHeader);
            final TemplateContents lTemplateContents = new TemplateContents(newNonTemplateId, lNonMatchingTemplate);
            addTemplateContentInfo(lTemplateContents);
            mNonMatchingTemplates.add(newNonTemplateId);
        }
    }

    TemplateResult checkTemplates(
            String aMessage,
            TemplateResult aTemplateResult)
    {

        try
        {
            final List<String> processedTemplateIds = new ArrayList<>();
            aMessage = aMessage.toLowerCase();
            final String strippedMessage = TemplateUtility.replaceCharacters(aMessage, false, false);

            // Check for recently used templates
            checkForRecentlyUsedTemplates(strippedMessage, aTemplateResult, processedTemplateIds);

            if (aTemplateResult.getResult() != null)
            {
                updateResults(aTemplateResult);
                return aTemplateResult;
            }

            // Check for non matching Messages
            checkWithNonMatchingMessages(strippedMessage, aTemplateResult);

            if (aTemplateResult.getResult() != null)
            {
                updateResults(aTemplateResult);
                return aTemplateResult;
            }

            // Check for no variable Messages
            checkForNoVariableMessages(strippedMessage, aTemplateResult);

            if (aTemplateResult.getResult() != null)
            {
                updateResults(aTemplateResult);
                return aTemplateResult;
            }

            // Check for non matching templates
            checkForNonMatchingTemplates(strippedMessage, aTemplateResult, processedTemplateIds);

            if (aTemplateResult.getResult() != null)
            {
                // In case of of Template found, in the case of non matching it should be
                // template not found.
                if (aTemplateResult.getResult() == Result.TEMPLATE_FOUND)
                    aTemplateResult.setResult(Result.TEMPLATE_NOT_FOUND);

                updateResults(aTemplateResult);
                return aTemplateResult;
            }

            // Check for Starts and Ends with Words Templates

            final String[] lFindFirstAndLastWord = TemplateUtility.findFirstAndLastWord(TemplateUtility.replaceCharacters(aMessage, false, true));
            final String   firstWord             = lFindFirstAndLastWord[0];
            final String   lastWord              = lFindFirstAndLastWord[1];

            if ((firstWord != null) && (lastWord != null))
            {
                final String       key   = CommonUtility.combine(firstWord, lastWord);
                final List<String> lList = mWordStartsAndEndsWith.get(key);
                checkForTemplates(lList, strippedMessage, aTemplateResult, processedTemplateIds);
            }

            if (aTemplateResult.getResult() != null)
            {
                updateResults(aTemplateResult);
                return aTemplateResult;
            }

            // Check for Starts with word Templates
            if ((firstWord != null))
            {
                final List<String> lList = mWordStarts.get(firstWord);
                checkForTemplates(lList, strippedMessage, aTemplateResult, processedTemplateIds);
            }

            if (aTemplateResult.getResult() != null)
            {
                updateResults(aTemplateResult);
                return aTemplateResult;
            }

            // Check for Ends with word Templates
            if ((lastWord != null))
            {
                final List<String> lList = mWordEnds.get(lastWord);
                checkForTemplates(lList, strippedMessage, aTemplateResult, processedTemplateIds);
            }

            if (aTemplateResult.getResult() != null)
            {
                updateResults(aTemplateResult);
                return aTemplateResult;
            }

            checkForTemplates(mStartsAndEndsWithVar, strippedMessage, aTemplateResult, processedTemplateIds);

            if (aTemplateResult.getResult() != null)
            {
                updateResults(aTemplateResult);
                return aTemplateResult;
            }

            checkForTemplates(mStartsWithVar, strippedMessage, aTemplateResult, processedTemplateIds);

            if (aTemplateResult.getResult() != null)
            {
                updateResults(aTemplateResult);
                return aTemplateResult;
            }

            checkForTemplates(mEndsWithVar, strippedMessage, aTemplateResult, processedTemplateIds);

            if (aTemplateResult.getResult() != null)
            {
                updateResults(aTemplateResult);
                return aTemplateResult;
            }

            // Check for remaining templates.
            checkForTemplates(mRemainingTemplates, strippedMessage, aTemplateResult, processedTemplateIds);

            if (aTemplateResult.getResult() != null)
            {
                updateResults(aTemplateResult);
                return aTemplateResult;
            }

            addNonMatchingTemplate(aMessage);

            aTemplateResult.setResult(Result.TEMPLATE_NOT_FOUND);
            updateResults(aTemplateResult);
            return aTemplateResult;
        }
        catch (final Exception e)
        {
            PrometheusMetrics.incrementGenericError(TemplateScrubber.getClusterType(), TemplateScrubber.getComponent(), CommonUtility.getApplicationServerIp(), "TMCHK-003",
                    "Template Check '" + e.getMessage() + "'");

            aTemplateResult.setResult(Result.TEMPLATE_NOT_FOUND);
            updateResults(aTemplateResult);
            return aTemplateResult;
        }
    }

    private void checkForRecentlyUsedTemplates(
            String aMessage,
            TemplateResult aResult,
            List<String> aProcessedTemplateIds)
    {
        final List<String> lRecentlyUsedTemplates = mRecentlyUsedTemplates.getRecentlyUsedTemplateIds();

        // Need to start from the tail to head.

        if (!lRecentlyUsedTemplates.isEmpty())
        {
            Collections.reverse(lRecentlyUsedTemplates);

            try
            {
                checkForTemplates(lRecentlyUsedTemplates, aMessage, aResult, aProcessedTemplateIds);
            }
            catch (final Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    private void checkForNonMatchingTemplates(
            String aMessage,
            TemplateResult aResult,
            List<String> aProcessedTemplateIds)
    {
        isInUseNonMatchingTemplateList = true;
        checkForTemplates(new ArrayList<>(mNonMatchingTemplates), aMessage, aResult, aProcessedTemplateIds);
        isInUseNonMatchingTemplateList = false;

        if (markForClearNonMatchingTemplateList)
            mNonMatchingTemplates.clear();
    }

    private void checkForTemplates(
            List<String> aTemplateIds,
            String aMessage,
            TemplateResult aResult,
            List<String> aProcessedTemplateIds)
    {

        if ((aTemplateIds != null) && !aTemplateIds.isEmpty())
        {
            // to avoid ConcurrentModificationException
            final List<String> tempList = new ArrayList<>(aTemplateIds);

            for (final String templateId : tempList)
            {

                if (aProcessedTemplateIds.contains(templateId))
                {
                    if (log.isDebugEnabled())
                        log.debug("Template id : '" + templateId + "' alreaddy processed.");

                    continue;
                }

                aProcessedTemplateIds.add(templateId);

                Result lTemplateMatch = checkForStaticWords(templateId, aMessage);

                if (lTemplateMatch == Result.TEMPLATE_FOUND)
                {
                    lTemplateMatch = TemplateUtility.isTemplateMatch(mTemplateContents.get(templateId), aMessage);

                    if (log.isDebugEnabled())
                        log.debug("Template id : '" + templateId + "' result '" + lTemplateMatch + "'");

                    if (lTemplateMatch == Result.TEMPLATE_FOUND)
                    {
                        aResult.setResult(lTemplateMatch);
                        aResult.setTemplateId(templateId);
                        break;
                    }
                }
            }
        }
    }

    private Result checkForStaticWords(
            String aTemplateId,
            String aMessage)
    {
        Result             lTemplateMatch = Result.TEMPLATE_FOUND;
        final List<String> lWordList      = mTemplateWordsList.get(aTemplateId);

        if (!lWordList.isEmpty())
        {
            int index = 0;

            for (final String s : lWordList)
            {
                index = aMessage.indexOf(s, index);

                if (index == -1)
                {
                    lTemplateMatch = Result.TEMPLATE_NOT_FOUND;
                    if (log.isDebugEnabled())
                        log.debug("Template id : '" + aTemplateId + "' All STATIC words are not matching.");
                    break;
                }

                index = index + s.length();
            }
        }

        return lTemplateMatch;
    }

    private void checkForNoVariableMessages(
            String aMessage,
            TemplateResult aResult)
    {
        final String templateId = mNoVarTemplates.get(aMessage);

        if (templateId != null)
        {
            aResult.setResult(Result.TEMPLATE_MATCHES_WITH_NO_VAR_MESSAGES);
            aResult.setTemplateId(templateId);
        }
    }

    private void checkWithNonMatchingMessages(
            String aMessage,
            TemplateResult aResult)
    {
        if (mNonMatchingMessages.isAvailable(aMessage))
            aResult.setResult(Result.TEMPLATE_MATCHES_WITH_EXISTING_FAILED_MESSAGES);
    }

    private void updateResults(
            TemplateResult aResult)
    {
        updateLastUsedTemplate(aResult.getTemplateId());
    }

    private void updateLastUsedTemplate(
            String aTemplateId)
    {

        if (aTemplateId != null)
        {
            mMostUsedTemplates.compute(aTemplateId, (
                    k,
                    v) -> v == null ? 1 : v + 1);

            mRecentlyUsedTemplates.addRecentlyUsedTemplate(aTemplateId);
        }
    }

    public void clearTemporaryTemplates()
    {

        if (!isInUseNonMatchingTemplateList)
        {
            mNonMatchingTemplates.clear();
            markForClearNonMatchingTemplateList = false;
        }
        else
            markForClearNonMatchingTemplateList = true;

        mRecentlyUsedTemplates.clear();
        mNonMatchingMessages.clear();
    }

}