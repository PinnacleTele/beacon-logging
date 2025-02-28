package com.itextos.beacon.platform.elasticsearchutil;

import java.io.IOException;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;

import com.itextos.beacon.platform.elasticsearchutil.data.R3Info;
import com.itextos.beacon.platform.elasticsearchutil.types.EsTypeParent;
import com.itextos.beacon.platform.elasticsearchutil.utility.EsUtility;

public class R3Message
{

    private static final Log log = LogFactory.getLog(R3Message.class);

    private R3Message()
    {}

    static void insertR3Message(
            R3Info aR3Info)
            throws Exception
    {

        try
        {
            final String indexName = EsUtility.getEsIndexName(EsTypeParent.R3);

            if (log.isDebugEnabled())
                log.debug("Index Name " + indexName);

            final IndexRequest        lInsertUpdateRequest = EsUtility.getR3InsertRequest(indexName, aR3Info);
            final RestHighLevelClient tempEsClient         = EsProcess.getInstance().getEsConnection();
            final IndexResponse       lIndex               = tempEsClient.index(lInsertUpdateRequest, RequestOptions.DEFAULT);

            if (log.isDebugEnabled())
                log.debug("Index '" + lIndex.getIndex() + "' Id '" + lIndex.getId() + "' Version '" + lIndex.getVersion() + "'");

            EsProcess.getInstance().updateLastUsed();
        }
        catch (final IOException e)
        {
            log.error("Exception while doing the R3 Process", e);
            throw e;
        }
    }

    static Map<String, Object> getShortCodeData(
            String shortCode)
    {
        Map<String, Object> dataMap             = null;
        RestHighLevelClient restHighLevelClient = null;

        try
        {
            final String     indexName        = EsUtility.getEsIndexName(EsTypeParent.R3);
            final GetRequest getshortCodeData = new GetRequest(indexName, shortCode);
            GetResponse      getResponse      = null;
            restHighLevelClient = EsProcess.getInstance().getEsConnection();

            getResponse         = restHighLevelClient.get(getshortCodeData, RequestOptions.DEFAULT);

            if (getResponse != null)
                dataMap = getResponse.getSourceAsMap();
            EsProcess.getInstance().updateLastUsed();

            System.out.println("ShortCode Data is : " + dataMap);
        }
        catch (final Exception e)
        {
            log.error("Exception while doing the R3 Process", e);
        }
        finally
        {

            try
            {
                if (restHighLevelClient != null)
                    restHighLevelClient.close();
            }
            catch (final IOException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return dataMap;
    }

}