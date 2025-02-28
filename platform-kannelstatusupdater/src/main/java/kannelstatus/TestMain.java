package kannelstatus;

import com.itextos.beacon.platform.kannelstatusupdater.process.KannelStatusRefresher;
import com.itextos.beacon.platform.kannelstatusupdater.process.response.KannelAvailability;

public class TestMain
{

    public static void main(
            String[] args)
    {
        KannelStatusRefresher.getInstance();

        try
        {
            Thread.sleep(10 * 1000);
        }
        catch (final InterruptedException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        final boolean lKannelAvailable = KannelAvailability.getInstance().isKannelAvailable("ABCD", 0);
        System.out.println(lKannelAvailable);
    }

}