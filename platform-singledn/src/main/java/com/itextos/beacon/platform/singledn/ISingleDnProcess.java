package com.itextos.beacon.platform.singledn;

import com.itextos.beacon.commonlib.constants.exception.ItextosException;
import com.itextos.beacon.platform.singledn.data.SingleDnInfo;

public interface ISingleDnProcess
{

    boolean addSingleDnInfo(
            SingleDnInfo aSingleDnInfo)
            throws ItextosException;

    SingleDnInfo getResult();

}