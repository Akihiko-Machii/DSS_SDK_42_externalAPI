package com.appresso.ds.dp.modules.adapter.resource;

import com.appresso.ds.common.fw.LoggingContext;
import com.appresso.ds.dp.spi.Resource;
import com.appresso.ds.dp.spi.ResourceConfiguration;
import com.appresso.ds.dp.spi.ResourceContext;

public class SampleResource implements Resource {
	private final String message;
	private final LoggingContext log;

	public SampleResource(
			ResourceConfiguration conf,
			ResourceContext context) {

		this.message = conf.getValue(SampleResourceFactory.KEY_MESSAGE).toString();
		this.log = context.log();
	}

	@Override
	public void setup() throws Exception {
		log.info("*** Resource SetUp ***");
	}

	@Override
	public void cleanup() throws Exception {
		log.info("*** Resource CleanUp ***");
	}

	@Override
	public void destroy() throws Exception {
		// server.log に出力されます。
		log.info("*** Resource Destroy ***");
	}

	public String getMessage() {
		return message;
	}
}
