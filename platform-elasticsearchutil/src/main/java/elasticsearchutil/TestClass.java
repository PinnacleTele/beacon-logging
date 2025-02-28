package elasticsearchutil;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.itextos.beacon.commonlib.constants.ClusterType;
import com.itextos.beacon.commonlib.constants.InterfaceGroup;
import com.itextos.beacon.commonlib.constants.InterfaceType;
import com.itextos.beacon.commonlib.constants.MessagePriority;
import com.itextos.beacon.commonlib.constants.MessageType;
import com.itextos.beacon.commonlib.constants.MiddlewareConstant;
import com.itextos.beacon.commonlib.constants.RouteType;
import com.itextos.beacon.commonlib.constants.exception.ItextosRuntimeException;
import com.itextos.beacon.commonlib.message.MessageRequest;
import com.itextos.beacon.platform.elasticsearchutil.EsProcess;
import com.itextos.beacon.platform.elasticsearchutil.data.R3Info;
import com.itextos.beacon.platform.elasticsearchutil.types.DlrQueryMulti;

public class TestClass
{

    public static void main(
            String[] args)
    {
        System.out.println("Time is " + new Date());
        // insetDlrSub();
        // inserDlrDn();
        // queryDlr();
        queryDlrMulti();
    }

    
    private static void inserDlrDn() throws ItextosRuntimeException
    {
        final String           date   = "2021-05-31";
        final DecimalFormat    df     = new DecimalFormat("00");

        final SimpleDateFormat sdf    = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        final String           longId = "20210531152113439";
        final String           time   = date + " 10:30:30.000";
        final String           msdId  = "IMessage-";

        for (int index = 0; index < 1; index++)
        {
            final MessageRequest nm           = new MessageRequest(ClusterType.BULK, //
                    InterfaceType.HTTP_JAPI, //
                    InterfaceGroup.API, //
                    MessageType.TRANSACTIONAL, //
                    MessagePriority.PRIORITY_0, //
                    RouteType.DOMESTIC);

            final String         mobileNumber = "988422720" + (index % 10);
            final String         ctime        = date + " 11:42:" + df.format(index % 60) + ".000";

            nm.putValue(MiddlewareConstant.MW_CLIENT_ID, "kumarapandian");
            nm.putValue(MiddlewareConstant.MW_MOBILE_NUMBER, mobileNumber);
            nm.putValue(MiddlewareConstant.MW_MSG_RECEIVED_DATE, date);
            nm.putValue(MiddlewareConstant.MW_FILE_ID, longId);
            nm.putValue(MiddlewareConstant.MW_MSG_RECEIVED_TIME, time);
            nm.putValue(MiddlewareConstant.MW_BASE_MESSAGE_ID, longId);
            nm.putValue(MiddlewareConstant.MW_MESSAGE_ID, msdId + index);
            nm.putValue(MiddlewareConstant.MW_MSG, "test message " + index);
            nm.putValue(MiddlewareConstant.MW_CARRIER_RECEIVED_TIME, ctime);
            nm.putValue(MiddlewareConstant.MW_DN_CLI_STATUS_CODE, ((index % 3) == 0 ? "201" : "200"));
            nm.putValue(MiddlewareConstant.MW_DN_CLI_STATUS_DESC, "SREEDHAR");

            try
            {
                // EsProcess.insertDlrQueryDn(nm);
            }
            catch (final Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    // private static void queryDlr()
    // {
    // final DlrQueryReq dqr = new DlrQueryReq("kumarapandian", "20210531152113439",
    // null, "9884227206");
    // final List<Map<MiddlewareConstant, String>> lDlrQueryInfo =
    // EsProcess.getDlrQueryInfo(dqr);
    // System.out.println(lDlrQueryInfo);
    // }

    private static void queryDlrMulti()
    {
        final List<String>                          fileIdList       = getFileIdList();
        final List<String>                          mobileNumberList = getMobileNumberList();
        final List<String>                          cliMsgIdList     = getClientMessageIdList();
        final DlrQueryMulti                         dqr              = new DlrQueryMulti("6000000200000000", fileIdList, cliMsgIdList, mobileNumberList);

        final List<Map<MiddlewareConstant, String>> lDlrQueryInfo    = EsProcess.getDlrQueryInfo(dqr);
        System.out.println(lDlrQueryInfo);
    }

    private static List<String> getClientMessageIdList()
    {
        final List<String> returnValue = new ArrayList<>();
        // returnValue.add("one");
        // returnValue.add("two");
        return returnValue;
    }

    private static List<String> getMobileNumberList()
    {
        final List<String> returnValue = new ArrayList<>();
        // returnValue.add("919999999999");
        // returnValue.add("919884227203");
        return returnValue;
    }

    private static List<String> getFileIdList()
    {
        final List<String> returnValue = new ArrayList<>();
        // returnValue.add("3062108121120473919400");
        // returnValue.add("3062108121120382490800");
        // returnValue.add("3062108121105458412400");
        returnValue.add("1662203171430420002700");
        /*
         * returnValue.add("3002108091037510001200");
         * returnValue.add("3012109091355100000100");
         * returnValue.add("3052109070832520001500");
         */
        return returnValue;
    }

    private static void insetDlrSub() throws ItextosRuntimeException
    {
        final String           date   = "2021-05-31";
        final DecimalFormat    df     = new DecimalFormat("00");

        final SimpleDateFormat sdf    = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        final String           longId = "20210531152113439";
        final String           time   = date + " 10:30:30.000";
        final String           msdId  = "IMessage-";

        for (int index = 0; index < 1; index++)
        {
            final MessageRequest nm           = new MessageRequest(ClusterType.BULK, //
                    InterfaceType.HTTP_JAPI, //
                    InterfaceGroup.API, //
                    MessageType.TRANSACTIONAL, //
                    MessagePriority.PRIORITY_0, //
                    RouteType.DOMESTIC);

            final String         mobileNumber = "988422720" + (index % 10);
            final String         ctime        = date + " 10:40:" + df.format(index % 60) + ".000";

            nm.putValue(MiddlewareConstant.MW_CLIENT_ID, "kumarapandian");
            nm.putValue(MiddlewareConstant.MW_MOBILE_NUMBER, mobileNumber);
            nm.putValue(MiddlewareConstant.MW_FILE_ID, longId);
            nm.putValue(MiddlewareConstant.MW_MSG_RECEIVED_DATE, date);
            nm.putValue(MiddlewareConstant.MW_MSG_RECEIVED_TIME, time);
            nm.putValue(MiddlewareConstant.MW_BASE_MESSAGE_ID, longId);
            nm.putValue(MiddlewareConstant.MW_MESSAGE_ID, msdId + index);
            nm.putValue(MiddlewareConstant.MW_MSG, "test message " + index);
            nm.putValue(MiddlewareConstant.MW_CARRIER_SUBMIT_TIME, ctime);
            nm.putValue(MiddlewareConstant.MW_CARRIER_ORI_STATUS_CODE, ((index % 2) == 0 ? "500" : "600"));

            try
            {
                // EsProcess.insertDlrQuerySub(nm);
            }
            catch (final Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    public static void main1(
            String[] args)
            throws Exception
    {
        // EsProcess.insertAgingDn(new MessageRequest(""));
        // EsProcess.updateAgingDn(new MessageRequest(""));
        // EsProcess.deleteAgingDn(new MessageRequest(""));
        //
        // EsProcess.insertSingleDn(new MessageRequest(""));
        // EsProcess.getSingleDn("client_id", "base_message_id",
        // MiddlewareConstant.MW_DELIVERY_TIME, EsSortOrder.ASCENDING);
        // EsProcess.deleteSingleDn(new MessageRequest(""));
        // EsProcess.deleteSingleDn("client_id", "base_message_id");
        //
        // EsProcess.insertDlrQuerySub(new MessageRequest(""));
        // EsProcess.insertDlrQueryDn(new MessageRequest(""));
        // final List<Map<MiddlewareConstant, String>> lDlrQueryInfo =
        // EsProcess.getDlrQueryInfo(new DlrQueryReq(null, null, null, null));

        EsProcess.insertR3Message(new R3Info(ClusterType.BULK, "shortcode", new Date()));
    }

}
