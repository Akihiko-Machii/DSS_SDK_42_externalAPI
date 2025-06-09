package com.appresso.ds.dp.modules.adapter.helloworld;

import java.util.HashMap;
import java.util.Map;

import com.appresso.ds.common.fw.LoggingContext;
import com.appresso.ds.dp.spi.Operation;
import com.appresso.ds.dp.spi.OperationContext;

public class HelloWorldAdapterOperation implements Operation {
	private final OperationContext context;

	public HelloWorldAdapterOperation(OperationContext context) {
		this.context = context;
	}

	@Override
	public Map execute(Map inputData) throws Exception {
		LoggingContext log = context.log();
		log.finest("*******************");
		log.info("** Hello World ! **");
		log.warn("**  execute() !  **");
		log.finest("*******************");

		return new HashMap();
	}

	@Override
	public void destroy() {
	}
}