package com.itextos.beacon.commonlib.stringprocessor.validator.drools;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseFactory;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderError;
import org.drools.builder.KnowledgeBuilderErrors;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.ResourceType;
import org.drools.io.ResourceFactory;
import org.drools.runtime.StatefulKnowledgeSession;

public class DroolsValidator
{

    private static final Log log = LogFactory.getLog(DroolsValidator.class);

    private static class SingletonHolder
    {

        static final DroolsValidator INSTANCE = new DroolsValidator();

    }

    public static DroolsValidator getInstance()
    {
        return SingletonHolder.INSTANCE;
    }

    private final Map<String, KnowledgeBase> knowledgeBaseMap = new HashMap<>();

    public boolean validate(
            String aFilePath,
            String aValue)
    {
        final KnowledgeBase kbase = knowledgeBaseMap.computeIfAbsent(aFilePath, k -> getKnowledgeBase(aFilePath));

        if (kbase == null)
            return false;

        final Response response = new Response(aValue);
        createSession(kbase, response);
        return response.isValidated();
    }

    private static void createSession(
            KnowledgeBase aKbase,
            Response aResponse)
    {
        final StatefulKnowledgeSession ksession = aKbase.newStatefulKnowledgeSession();

        ksession.insert(aResponse);
        ksession.fireAllRules();
        ksession.dispose();
    }

    private static KnowledgeBase getKnowledgeBase(
            String aFilePath)
    {

        try
        {
            return readKnowledgeBase(aFilePath);
        }
        catch (final Exception e)
        {
            log.error("Exception while reading drools knowledge base | Drools filePath " + aFilePath, e);
        }
        return null;
    }

    private static KnowledgeBase readKnowledgeBase(
            String aFilePath)
    {
        final KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
        kbuilder.add(ResourceFactory.newFileResource(aFilePath), ResourceType.DRL);

        final KnowledgeBuilderErrors errors = kbuilder.getErrors();

        if (!errors.isEmpty())
        {
            for (final KnowledgeBuilderError error : errors)
                log.error("Drools file : '" + aFilePath + "' Error : '" + error + "'");

            throw new IllegalArgumentException("Could not parse knowledge.");
        }

        final KnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase();
        kbase.addKnowledgePackages(kbuilder.getKnowledgePackages());
        return kbase;
    }

}