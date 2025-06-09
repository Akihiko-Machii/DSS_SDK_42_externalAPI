package com.appresso.ds.dp.modules.adapter.resource;

import com.appresso.ds.dp.spi.AdapterModuleComponent;
import com.appresso.ds.dp.spi.OperationFactory;
import com.appresso.ds.dp.spi.ResourceFactory;

public class SampleResourceAdapterModuleComponent extends AdapterModuleComponent {

	public SampleResourceAdapterModuleComponent() {
	}

	@Override
	// リソースファクトリのインスタンスを配列で返します。
	public ResourceFactory[] getResourceFactories() throws Exception {
		return new ResourceFactory[] { new SampleResourceFactory() };
	}

	@Override
	public OperationFactory[] getOperationFactories() throws Exception {
		return new OperationFactory[] { new SampleOperationFactory() };
	}
}
