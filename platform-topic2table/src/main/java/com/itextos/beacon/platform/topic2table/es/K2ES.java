package com.itextos.beacon.platform.topic2table.es;

import java.util.List;

import com.itextos.beacon.commonlib.message.BaseMessage;

public interface K2ES {

	 public void pushtoElasticSearch( List<BaseMessage> mMessagesToInsert);
}
