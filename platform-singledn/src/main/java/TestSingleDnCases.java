import java.util.ArrayList;
import java.util.List;

import com.itextos.beacon.commonlib.constants.DateTimeFormat;
import com.itextos.beacon.commonlib.constants.exception.ItextosException;
import com.itextos.beacon.commonlib.utility.DateTimeUtility;
import com.itextos.beacon.inmemory.customfeatures.pojo.DNDeliveryMode;
import com.itextos.beacon.inmemory.customfeatures.pojo.DlrTypeInfo;
import com.itextos.beacon.inmemory.customfeatures.pojo.SingleDnProcessType;
import com.itextos.beacon.platform.singledn.ISingleDnProcess;
import com.itextos.beacon.platform.singledn.data.SingleDnInfo;
import com.itextos.beacon.platform.singledn.data.SingleDnRequest;
import com.itextos.beacon.platform.singledn.enums.DnStatus;
import com.itextos.beacon.platform.singledn.impl.SingleDnProcessorAllSuccess;

public class TestSingleDnCases
{

    private static final String CLIENT_ID   = "6000000200000000";
    private static final String DEST        = "919745613944";
    private static final String BASE_MSG_ID = "2792204061523034983000";

    public static void main(
            String[] args)
    {

        try
        {
            final SingleDnRequest    sdr                = new SingleDnRequest(CLIENT_ID, DEST, BASE_MSG_ID, null);

            final DlrTypeInfo        dt1                = new DlrTypeInfo(CLIENT_ID, "1", "18000", SingleDnProcessType.ALL_SUCCESS, DNDeliveryMode.LAST_PART, "0",
                    DNDeliveryMode.LATEST_FAILURE_DELIVERED, "1", false);

            final ISingleDnProcess   singleDnProcessor1 = new SingleDnProcessorAllSuccess(sdr, dt1);

            /*
             * final DlrTypeInfo dt2 = new DlrTypeInfo(CLIENT_ID, "1", "18000",
             * SingleDnProcessType.ALL_FAILURE, DNDeliveryMode.EARLIEST_FAILURE_DELIVERED,
             * "0",
             * DNDeliveryMode.AVAILABLE_FIRST_SUCCESS_PART, "1", false);
             * final ISingleDnProcess singleDnProcessor2 = new
             * SingleDnProcessorAllFailure(sdr, dt2);
             * final DlrTypeInfo dt3 = new DlrTypeInfo(CLIENT_ID, "1", "18000",
             * SingleDnProcessType.ALL_FAILURE, DNDeliveryMode.EARLIEST_FAILURE_DELIVERED,
             * "0",
             * DNDeliveryMode.AVAILABLE_FIRST_SUCCESS_PART, "1", false);
             * final ISingleDnProcess singleDnProcessor3 = new
             * SingleDnProcessorAnyFailure(sdr, dt3);
             * final DlrTypeInfo dt4 = new DlrTypeInfo(CLIENT_ID, "1", "18000",
             * SingleDnProcessType.ATLEAST_ONE_SUCCESS,
             * DNDeliveryMode.EARLIEST_FAILURE_DELIVERED, "0",
             * DNDeliveryMode.AVAILABLE_FIRST_SUCCESS_PART, "1", false);
             * final ISingleDnProcess singleDnProcessor4 = new
             * SingleDnProcessorAtleastOneSuccess(sdr, dt4);
             * final DlrTypeInfo dt5 = new DlrTypeInfo(CLIENT_ID, "1", "18000",
             * SingleDnProcessType.PARTIAL_SUCCESS,
             * DNDeliveryMode.EARLIEST_FAILURE_DELIVERED, "0",
             * DNDeliveryMode.AVAILABLE_FIRST_SUCCESS_PART, "1", false);
             * final ISingleDnProcess singleDnProcessor5 = new
             * SingleDnProcessorPartialSuccess(sdr, dt5);
             * final DlrTypeInfo dt6 = new DlrTypeInfo(CLIENT_ID, "1", "18000",
             * SingleDnProcessType.PARTIAL_FAILURE,
             * DNDeliveryMode.EARLIEST_FAILURE_DELIVERED, "0",
             * DNDeliveryMode.AVAILABLE_FIRST_SUCCESS_PART, "1", false);
             * final ISingleDnProcess singleDnProcessor6 = new
             * SingleDnProcessorPartialFailure(sdr, dt6);
             */

            final List<SingleDnInfo> list               = getSingleDns();

            for (final SingleDnInfo sdi : list)
            {
                final boolean lAddSingleDnInfo = singleDnProcessor1.addSingleDnInfo(sdi);
                System.out.println(lAddSingleDnInfo);
                if (lAddSingleDnInfo)
                    break;
            }

            final SingleDnInfo lResult = singleDnProcessor1.getResult();
            System.out.println(lResult);
        }
        catch (final ItextosException e)
        {
            e.printStackTrace();
        }
    }

    private static List<SingleDnInfo> getSingleDns()
    {
        final SingleDnInfo       sdi1        = new SingleDnInfo("2562204061523036792500", 5, 6,
                DateTimeUtility.getDateFromString("2022-04-06 15:23:03.984", DateTimeFormat.DEFAULT_WITH_MILLI_SECONDS).getTime(),
                DateTimeUtility.getDateFromString("2022-04-06 15:23:05.261", DateTimeFormat.DEFAULT_WITH_MILLI_SECONDS).getTime(), DnStatus.FAILURE);
        final SingleDnInfo       sdi2        = new SingleDnInfo("2562204061523036792200", 2, 6,
                DateTimeUtility.getDateFromString("2022-04-06 15:23:03.984", DateTimeFormat.DEFAULT_WITH_MILLI_SECONDS).getTime(),
                DateTimeUtility.getDateFromString("2022-04-06 15:23:05.255", DateTimeFormat.DEFAULT_WITH_MILLI_SECONDS).getTime(), DnStatus.SUCCESS);
        final SingleDnInfo       sdi3        = new SingleDnInfo("2562204061523036792300", 3, 6,
                DateTimeUtility.getDateFromString("2022-04-06 15:23:03.984", DateTimeFormat.DEFAULT_WITH_MILLI_SECONDS).getTime(),
                DateTimeUtility.getDateFromString("2022-04-06 15:23:05.257", DateTimeFormat.DEFAULT_WITH_MILLI_SECONDS).getTime(), DnStatus.SUCCESS);
        final SingleDnInfo       sdi4        = new SingleDnInfo("2562204061523036792600", 6, 6,
                DateTimeUtility.getDateFromString("2022-04-06 15:23:03.984", DateTimeFormat.DEFAULT_WITH_MILLI_SECONDS).getTime(),
                DateTimeUtility.getDateFromString("2022-04-06 15:23:05.264", DateTimeFormat.DEFAULT_WITH_MILLI_SECONDS).getTime(), DnStatus.FAILURE);
        final SingleDnInfo       sdi5        = new SingleDnInfo("2562204061523036792400", 4, 6,
                DateTimeUtility.getDateFromString("2022-04-06 15:23:03.984", DateTimeFormat.DEFAULT_WITH_MILLI_SECONDS).getTime(),
                DateTimeUtility.getDateFromString("2022-04-06 15:23:05.259", DateTimeFormat.DEFAULT_WITH_MILLI_SECONDS).getTime(), DnStatus.SUCCESS);
        final SingleDnInfo       sdi6        = new SingleDnInfo("2562204061523036792100", 1, 6,
                DateTimeUtility.getDateFromString("2022-04-06 15:23:03.984", DateTimeFormat.DEFAULT_WITH_MILLI_SECONDS).getTime(),
                DateTimeUtility.getDateFromString("2022-04-06 15:23:05.253", DateTimeFormat.DEFAULT_WITH_MILLI_SECONDS).getTime(), DnStatus.SUCCESS);

        final List<SingleDnInfo> returnValue = new ArrayList<>();
        returnValue.add(sdi1);
        returnValue.add(sdi2);
        returnValue.add(sdi3);
        returnValue.add(sdi4);
        returnValue.add(sdi5);
        returnValue.add(sdi6);
        return returnValue;
    }

}