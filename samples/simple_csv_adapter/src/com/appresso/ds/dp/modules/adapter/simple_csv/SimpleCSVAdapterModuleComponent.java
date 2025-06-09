package com.appresso.ds.dp.modules.adapter.simple_csv;

import com.appresso.ds.dp.spi.AdapterModuleComponent;
import com.appresso.ds.dp.spi.OperationFactory;

public class SimpleCSVAdapterModuleComponent extends AdapterModuleComponent {
	@Override
	public OperationFactory[] getOperationFactories() throws Exception {
		return new OperationFactory[] {
				new GetSimpleCSVOperationFactory(),
				new PutSimpleCSVOperationFactory()
		};
	}
}
