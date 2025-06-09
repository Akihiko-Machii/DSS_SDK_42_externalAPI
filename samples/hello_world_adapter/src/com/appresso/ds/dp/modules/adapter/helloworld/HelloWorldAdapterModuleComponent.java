package com.appresso.ds.dp.modules.adapter.helloworld;

import com.appresso.ds.dp.spi.AdapterModuleComponent;
import com.appresso.ds.dp.spi.OperationFactory;

public class HelloWorldAdapterModuleComponent extends AdapterModuleComponent {
	@Override
	public OperationFactory[] getOperationFactories() throws Exception {
		return new OperationFactory[] { new HelloWorldAdapterOperationFactory() };
	}
}
