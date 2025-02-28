package com.itextos.beacon.platform.agingdn.processor;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONObject;

import com.itextos.beacon.commonlib.message.DeliveryObject;

public class AgingElasticSearchUtil
{

    private static Log    log                 = LogFactory.getLog(AgingElasticSearchUtil.class);

    private static String COLLECTION_NAME     = "aging";
    private static String MONGO_INTERNAL_TYPE = "internal";

    /**
     * Using this method to receive the Aging
     *
     * @return
     */
    public static List<JSONObject> getAgingDNInfo(
            int DIVISOR_VAL,
            int REMAINDER_VAL)
    {
        // Get from Elastic Search

        return null;
    }

    /*
     * public static List<JSONObject> getAgingDNInfo(
     * int DIVISOR_VAL,
     * int REMAINDER_VAL,
     * String threadName)
     * {
     * if (log.isDebugEnabled())
     * log.debug(threadName + " getAgingDNInfo() - Calling..");
     * MongoDatabase database = null;
     * final List<JSONObject> agingDNList = new ArrayList<>();
     * try
     * {
     * database = MongoDBPoolTon.getInstance().getDB(COLLECTION_NAME,
     * MONGO_INTERNAL_TYPE);
     * if (log.isDebugEnabled())
     * log.debug(threadName + " getAgingDNInfo() - MondoDB database - " + database);
     * if (database != null)
     * {
     * MongoCollection<Document> collection =
     * database.getCollection(COLLECTION_NAME);
     * if (collection == null)
     * database.createCollection(COLLECTION_NAME);
     * if (log.isDebugEnabled())
     * log.debug(threadName + " getAgingDNInfo() - Collection Object - " +
     * COLLECTION_NAME + " : " + collection);
     * collection = database.getCollection(COLLECTION_NAME);
     * final Document inQuery = new Document();
     * final BasicDBList modArgs = new BasicDBList();
     * modArgs.add(DIVISOR_VAL);
     * modArgs.add(REMAINDER_VAL);
     * // inQuery.put(MapKey.FPS_ID, FPS_ID);
     * inQuery.put(MapKey.AGEING_SCHD_TIME, new Document("$lte",
     * System.currentTimeMillis()));
     * inQuery.put(MapKey.DEST, new Document("$mod", modArgs));
     * if (log.isDebugEnabled())
     * log.debug(threadName + " Aging DN Fetch Query : " + inQuery);
     * final FindIterable<Document> cursor = collection.find(inQuery);
     * final MongoCursor<Document> documentIterator = cursor.iterator();
     * Document curDocument = null;
     * while (documentIterator.hasNext())
     * {
     * curDocument = documentIterator.next();
     * final JSONObject jsonAgingDn = new JSONObject();
     * jsonAgingDn.put(MapKey.ESME_ADDRESS, (String)
     * curDocument.get(MapKey.ESME_ADDRESS));
     * jsonAgingDn.put(MapKey.MID, (String) curDocument.get(MapKey.MID));
     * jsonAgingDn.put(MapKey.DEST, (Long) curDocument.get(MapKey.DEST));
     * jsonAgingDn.put(MapKey.FPS_ID,
     * String.valueOf(curDocument.get(MapKey.FPS_ID)));
     * jsonAgingDn.put(MapKey.ROUTEID, (String) curDocument.get(MapKey.ROUTEID));
     * jsonAgingDn.put(MapKey.DN_COME_FROM, (String)
     * curDocument.get(MapKey.DN_COME_FROM));
     * jsonAgingDn.put(MapKey.AGING_TYPE, (String)
     * curDocument.get(MapKey.AGING_TYPE));
     * jsonAgingDn.put(MapKey.FULL_MAP_MSG, (String)
     * curDocument.get(MapKey.FULL_MAP_MSG));
     * jsonAgingDn.put(MapKey.STATUS_ID, (String)
     * curDocument.get(MapKey.STATUS_ID));
     * agingDNList.add(jsonAgingDn);
     * }
     * if (log.isDebugEnabled())
     * log.debug(threadName + " getAgingDNInfo() - AgingDN List -" + agingDNList);
     * }
     * }
     * catch (final Exception e)
     * {
     * log.error(threadName +
     * " Exception occer while featching Ageing DN info from Mongo DB..", e);
     * }
     * return agingDNList;
     * }
     */

    public static boolean deleteRecordFromElasticSearch(
            DeliveryObject aDeliveryObject)
    {
        // TODO : Delete the record in elestic search
        return true;
    }

    /*
     * public static boolean deleteRecordFromMongoDB(
     * JSONObject jsonMsg,
     * String threadName)
     * {
     * if (log.isDebugEnabled())
     * log.debug(threadName + " Calling deleteRecordFromMongoDB ...");
     * MongoDatabase database = null;
     * boolean isMongoTransSuccess = false;
     * try
     * {
     * database = MongoDBPoolTon.getInstance().getDB(COLLECTION_NAME,
     * MONGO_INTERNAL_TYPE);
     * if (log.isDebugEnabled())
     * log.debug(threadName + " deleteRecordFromMongoDB() - Mongo database -" +
     * database);
     * if (database != null)
     * {
     * MongoCollection<Document> collection =
     * database.getCollection(COLLECTION_NAME);
     * if (collection == null)
     * collection = database.getCollection(COLLECTION_NAME);
     * final Document query = new Document();
     * query.put(MapKey.ESME_ADDRESS, jsonMsg.get(MapKey.ESME_ADDRESS));
     * query.put(MapKey.DEST,
     * Long.parseLong(Utility.nullCheck(jsonMsg.get(MapKey.DEST))));
     * query.put(MapKey.FPS_ID,
     * Long.parseLong(Utility.nullCheck(jsonMsg.get(MapKey.FPS_ID))));
     * query.put(MapKey.MID, jsonMsg.get(MapKey.MID));
     * query.put(MapKey.AGEING_DN_TYPE, jsonMsg.get(MapKey.AGEING_DN_TYPE));
     * if (log.isDebugEnabled())
     * log.debug(threadName + " deleteRecordFromMongoDB() - Delete Query : " +
     * query);
     * final long recordCnt = collection.deleteOne(query).getDeletedCount();
     * if (log.isDebugEnabled())
     * log.debug(threadName + " deleteRecordFromMongoDB() - Delete record count - "
     * + recordCnt);
     * if (recordCnt > 0)
     * isMongoTransSuccess = true;
     * }
     * }
     * catch (final Exception e)
     * {
     * log.error(threadName + " Problem while deleting data from MongoDB ..", e);
     * isMongoTransSuccess = false;
     * }
     * return isMongoTransSuccess;
     * }
     */
}
