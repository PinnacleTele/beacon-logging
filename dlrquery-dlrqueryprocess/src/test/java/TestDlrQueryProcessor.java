import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.Connection;

import com.itextos.beacon.commonlib.commondbpool.DBDataSourceFactory;
import com.itextos.beacon.commonlib.commondbpool.JndiInfo;
import com.itextos.beacon.dlrquery.queryprocess.QueryProcessor;
import com.itextos.beacon.dlrquery.request.RequestData;

public class TestDlrQueryProcessor
{

    public static void main(
            String[] args)
            throws UnknownHostException
    {

        try (
                Connection con = DBDataSourceFactory.getConnection(JndiInfo.CONFIGURARION_DB);)
        {}
        catch (final Exception e)
        {
            e.printStackTrace();
            return;
        }

        final String         lAccessKey    = "4XAn$Yr8lr8U";
        final String         lCustMid      = "";
        final String         lMobileNumber = "";
        final String         lFileId       = "1662203171430420002700";

        final RequestData    requestData   = new RequestData(lAccessKey, InetAddress.getLocalHost().getHostAddress(), lFileId, lCustMid, lMobileNumber);
        final QueryProcessor qurProcessor  = new QueryProcessor(requestData);
        final String         response      = qurProcessor.query();

        System.out.println("Response is " + response);
    }

}
