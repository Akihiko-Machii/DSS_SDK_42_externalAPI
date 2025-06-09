package com.appresso.ds.dp.modules.adapter.resource;

import com.appresso.ds.common.dp.ResourceInfo;
import com.appresso.ds.common.spi.constraint.Fillin;
import com.appresso.ds.common.spi.param.SimpleParameter;
import com.appresso.ds.dp.spi.Resource;
import com.appresso.ds.dp.spi.ResourceConfiguration;
import com.appresso.ds.dp.spi.ResourceConfigurator;
import com.appresso.ds.dp.spi.ResourceContext;
import com.appresso.ds.dp.spi.ResourceFactory;
import com.appresso.ds.dp.spi.ResourceMetaData;

public class SampleResourceFactory implements ResourceFactory {
	public static final String KEY_MESSAGE = "MESSAGE";
	public static final String RESOURCE_NAME = "sample";

	@Override
	// SampleResource 固有の文字列 (キー) を返します。
	public String getResourceName() {
		return RESOURCE_NAME;
	}

	@Override
	public ResourceMetaData getResourceMetaData(
			ResourceContext context) throws Exception {

		// ResourceMetaData を生成
		ResourceMetaData meta = new ResourceMetaData(this, context);
		// リソースのアイコン名を設定
		meta.setLabel("Sample Resource");
		meta.setDefaultType(ResourceInfo.TYPE_GLOBAL_RESOURCE);
		// プールのサポートを有効に設定
		meta.setPoolSupported(true);
		meta.setPoolDisabled(false);
		return meta;
	}

	@Override
	public ResourceConfigurator createResourceConfigurator(
			ResourceConfiguration conf,
			ResourceContext context) throws Exception {

		ResourceConfigurator configurator = new ResourceConfigurator(conf, context);

		Fillin constraint = new Fillin();
		constraint.setLabel("Message");

		SimpleParameter param = new SimpleParameter(KEY_MESSAGE, constraint);
		configurator.addSimpleParameter(param);

		return configurator;
	}

	@Override
	public Resource createResource(
			ResourceConfiguration conf,
			ResourceContext context) throws Exception {

		context.log().info("*** Create new resource ***");
		// リソースのインスタンスを返します。
		return new SampleResource(conf, context);
	}
}
