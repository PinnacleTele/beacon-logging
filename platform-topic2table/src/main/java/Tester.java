import java.util.ArrayList;
import java.util.List;

import com.itextos.beacon.commonlib.constants.ClusterType;
import com.itextos.beacon.commonlib.constants.Component;
import com.itextos.beacon.commonlib.constants.InterfaceGroup;
import com.itextos.beacon.commonlib.constants.InterfaceType;
import com.itextos.beacon.commonlib.constants.MessagePriority;
import com.itextos.beacon.commonlib.constants.MessageType;
import com.itextos.beacon.commonlib.constants.MiddlewareConstant;
import com.itextos.beacon.commonlib.constants.RouteType;
import com.itextos.beacon.commonlib.constants.Table2DBInserterId;
import com.itextos.beacon.commonlib.constants.exception.ItextosRuntimeException;
import com.itextos.beacon.commonlib.message.BaseMessage;
import com.itextos.beacon.commonlib.message.MessageRequest;
import com.itextos.beacon.platform.topic2table.inserter.DynamicTableInserter;
import com.itextos.beacon.platform.topic2table.inserter.ITableInserter;
import com.itextos.beacon.platform.topic2table.inserter.StaticTableInserter;

public class Tester
{

    // public static void main(
    // String[] args)
    // {
    //
    // try
    // {
    // final Connection con =
    // DBDataSourceFactory.getConnection(JndiInfo.CONFIGURARION_DB);
    //
    // try
    // {
    // final PreparedStatement pstmt = con.prepareStatement("select * from
    // topic2table_config", 1003, 1007);
    //
    // try
    // {
    // pstmt.setFetchSize(1000);
    // final ResultSet mResultSet = pstmt.executeQuery();
    // System.err.println("-----------------------------------------------");
    // mResultSet.next();
    // System.out.println(mResultSet.getString(1));
    // System.err.println("-----------------------end------------------------");
    // if (pstmt != null)
    // pstmt.close();
    // }
    // catch (final Throwable throwable)
    // {
    // if (pstmt != null)
    // try
    // {
    // pstmt.close();
    // }
    // catch (final Throwable throwable1)
    // {
    // throwable.addSuppressed(throwable1);
    // }
    // throw throwable;
    // }
    // if (con != null)
    // con.close();
    // }
    // catch (final Throwable throwable)
    // {
    // if (con != null)
    // try
    // {
    // con.close();
    // }
    // catch (final Throwable throwable1)
    // {
    // throwable.addSuppressed(throwable1);
    // }
    // throw throwable;
    // }
    // }
    // catch (final Exception e)
    // {
    // e.printStackTrace();
    // }
    // finally
    // {}
    // }

    public static void main(
            String[] args) throws ItextosRuntimeException
    {
        final List<BaseMessage> messageList   = new ArrayList<>();
        final MessageRequest    sampleMessage = new MessageRequest(ClusterType.BULK, InterfaceType.FTP, InterfaceGroup.API, MessageType.PROMOTIONAL, MessagePriority.PRIORITY_0,
                RouteType.INTERNATIONAL);
        sampleMessage.putValue(MiddlewareConstant.MW_MSG, "Message from Static Inserter");
        sampleMessage.putValue(MiddlewareConstant.MW_MESSAGE_ID, "3012108071224110005500");
        messageList.add(sampleMessage);
        final ITableInserter inserter = new StaticTableInserter(Component.T2DB_NO_PAYLOAD_DN, Table2DBInserterId.NO_PAYLOAD_DN, messageList);
        inserter.process();
        sampleMessage.putValue(MiddlewareConstant.MW_MSG, "Message from Dynamic Inserter");

        /*
         * INSETAD OF HAVING A NEW ENTRY IN TABLE WE CAN REUSE THIS IF SAME
         * CONFIGURATION IS NEEDED
         */
        final ITableInserter inserter2 = new DynamicTableInserter(Component.T2DB_NO_PAYLOAD_DN, Table2DBInserterId.NO_PAYLOAD_DN, messageList);
        inserter2.process();

        // final ITableInserter inserter2 = new
        // DynamicTableInserter(Component.INTERFACES,
        // InmemoryId.TABLE_INSERTER_INFO.toString() + "_DYNAMIC", messageList);
        // inserter2.process();
    }

}
