package com.appresso.ds.dp.modules.adapter.print;

import com.appresso.ds.dp.spi.AdapterModuleComponent;
import com.appresso.ds.dp.spi.OperationFactory;

public class PrintAdapterModuleComponent extends AdapterModuleComponent {
	@Override
	public OperationFactory[] getOperationFactories() throws Exception {
		return new OperationFactory[] { new PrintAdapterOperationFactory() };
	}
}
