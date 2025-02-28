package com.itextos.beacon.commonlib.stringprocessor.main;

import java.util.ArrayList;
import java.util.List;

import com.itextos.beacon.commonlib.constants.ClusterType;
import com.itextos.beacon.commonlib.constants.InterfaceGroup;
import com.itextos.beacon.commonlib.constants.InterfaceType;
import com.itextos.beacon.commonlib.constants.MessagePriority;
import com.itextos.beacon.commonlib.constants.MessageType;
import com.itextos.beacon.commonlib.constants.MiddlewareConstant;
import com.itextos.beacon.commonlib.constants.RouteType;
import com.itextos.beacon.commonlib.constants.exception.ItextosRuntimeException;
import com.itextos.beacon.commonlib.message.MessageRequest;
import com.itextos.beacon.commonlib.stringprocessor.dto.ParamDataType;
import com.itextos.beacon.commonlib.stringprocessor.dto.ValidatorMaster;
import com.itextos.beacon.commonlib.stringprocessor.dto.ValidatorParams;
import com.itextos.beacon.commonlib.stringprocessor.process.ValidatorProcess;

public class Test
{

    public static void main(
            String[] args)
    {
        MessageRequest sampleMessage;
		try {
			sampleMessage = new MessageRequest(ClusterType.BULK, InterfaceType.FTP, InterfaceGroup.API, MessageType.PROMOTIONAL, MessagePriority.PRIORITY_0, RouteType.INTERNATIONAL);
		
        sampleMessage.putValue(MiddlewareConstant.MW_MSG, "Message from Static Inserter");
        sampleMessage.putValue(MiddlewareConstant.MW_MOBILE_NUMBER, "9876543211");
        sampleMessage.putValue(MiddlewareConstant.MW_ENCRYPTED_MOBILENUMBER, "ENCRYPTED MOBIL NUMBVER XXXXX");

        final ValidatorParams       params          = new ValidatorParams(1, "dest", "encrypted_dest", "XXXXX", ParamDataType.STRING, "", "[EMPTY],[NULL],{9876543210},987654321", "");

        final List<ValidatorParams> validatorParams = new ArrayList();
        validatorParams.add(params);
        final ValidatorMaster master          = new ValidatorMaster(false, "{\"version\": \"1.0\",\"toNumber\" : \"{1}\"}", "{[", "]}", ",", validatorParams);

        final String          replcedTemplate = ValidatorProcess.processTemplate(master, sampleMessage);
        System.out.println(replcedTemplate);
		} catch (ItextosRuntimeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

}
