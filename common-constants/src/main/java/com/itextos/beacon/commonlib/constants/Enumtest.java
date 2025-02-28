package com.itextos.beacon.commonlib.constants;

import java.util.ArrayList;
import java.util.List;

public class Enumtest
{


}

class TempThread
        extends
        Thread
{

    ClusterType ct = null;

    @Override
    public void run()
    {
        ct = ClusterType.getCluster("bulk");
        System.out.println(">>>>>>>>>>>>>>>>> " + ct);
    }

}