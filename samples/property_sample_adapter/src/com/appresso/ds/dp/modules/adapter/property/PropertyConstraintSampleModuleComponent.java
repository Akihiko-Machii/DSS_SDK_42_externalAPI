package com.appresso.ds.dp.modules.adapter.property;

import com.appresso.ds.dp.spi.AdapterModuleComponent;
import com.appresso.ds.dp.spi.OperationFactory;

public class PropertyConstraintSampleModuleComponent extends AdapterModuleComponent {
	@Override
	public OperationFactory[] getOperationFactories() throws Exception {
		return new OperationFactory[] { new PropertyConstraintSampleFactory() };
	}
}
