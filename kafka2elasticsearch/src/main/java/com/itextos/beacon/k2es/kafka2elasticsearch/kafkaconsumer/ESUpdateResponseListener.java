package com.itextos.beacon.k2es.kafka2elasticsearch.kafkaconsumer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.ResponseListener;
import org.elasticsearch.client.RestClient;
import org.json.simple.JSONObject;

import com.itextos.beacon.k2es.kafka2elasticsearch.start.StartApplication;

public class ESUpdateResponseListener
        implements
        ResponseListener
{

    private static final Log log = LogFactory.getLog(ESUpdateResponseListener.class);
    protected JSONObject     ESData;
    RestClient               ESClient;

    public ESUpdateResponseListener(
            JSONObject aESData,
            RestClient aESClient)
    {
        this.ESData   = aESData;
        this.ESClient = aESClient;
    }

    @Override
    public void onSuccess(
            Response aResponse)
    {
        // TODO Auto-generated method stub
    }

    @Override
    public void onFailure(
            Exception aException)
    {

        if (ESClient != null)
        {
            final String msgId = (String) ESData.get(StartApplication.ESIndexUniqueColumn);
            final String msg   = String.format("Error while updating Data:[%s] %s", msgId, ESData.toJSONString());
            log.error("Error while updating Data: " + ESData.toJSONString());
            final String err_msg = String.format("Error occurred [%s]", msgId);
            log.error(err_msg, aException);

            try
            {
                final Request ESReq = new Request("POST", "/" + StartApplication.ESIndexName +
                        "/_update/" + msgId);
                ESReq.setJsonEntity(ESData.toJSONString());
                final ESUpdateResponseListener uRL = new ESUpdateResponseListener(ESData,
                        null);

                synchronized (ESClient)
                {
                    ESClient.performRequestAsync(ESReq, uRL);
                }
            }
            catch (final Exception e)
            {
                log.error("Error while adding Failed data", e);
            }
        }
    }

}
