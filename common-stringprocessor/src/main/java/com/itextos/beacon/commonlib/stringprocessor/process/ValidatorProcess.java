package com.itextos.beacon.commonlib.stringprocessor.process;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.message.BaseMessage;
import com.itextos.beacon.commonlib.stringprocessor.dto.ValidatorMaster;
import com.itextos.beacon.commonlib.stringprocessor.dto.ValidatorParams;

public class ValidatorProcess
{

    private static final Log log = LogFactory.getLog(ValidatorProcess.class);

    private ValidatorProcess()
    {}

    public static String processTemplate(
            ValidatorMaster aVlidatorMaster,
            BaseMessage aMessage)
    {
        final List<ValidatorParams> aClientHandoverParams = aVlidatorMaster.getValidatorParams();
        final StringBuilder         template              = ValidatorUtil.getTemplateBuilder(aVlidatorMaster);

        for (final ValidatorParams handoverParams : aClientHandoverParams)
        {
            final String defaultValue = handoverParams.getDefaultValue();
            final String replaceKey   = "{" + handoverParams.getParamSeqNo() + "}";
            String       finalValue   = defaultValue;

            try
            {
                finalValue = ValidatorUtil.getValueFromTheConstant(handoverParams, aMessage);
            }
            catch (final Exception e)
            {
                log.error("Exception while getting the http param value from constant. Handover Params " + handoverParams + " template " + template + " Message " + aMessage, e);
            }

            try
            {
                ValidatorUtil.getReplacedStringBuffer(template, replaceKey, finalValue, aVlidatorMaster.isEncodeRequired());
            }
            catch (final Exception e)
            {
                log.error("Exception parsing the template. Handover Params " + handoverParams + " template " + template + " Message " + aMessage, e);
            }
        }
        return template.toString();
    }

}