package com.appresso.ds.dp.modules.adapter.messagexml;

import com.appresso.ds.dp.spi.AdapterModuleComponent;
import com.appresso.ds.dp.spi.GetDataOperationFactory;
import com.appresso.ds.dp.spi.OperationFactory;

public class MessageXMLAdapterModuleComponent extends AdapterModuleComponent {
	@Override
	public OperationFactory[] getOperationFactories() throws Exception {
		return new GetDataOperationFactory[] { new MessageXMLAdapterOperationFactory() };
	}
}
