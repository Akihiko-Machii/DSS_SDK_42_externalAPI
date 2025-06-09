package com.appresso.ds.dp.modules.adapter.resource;

import com.appresso.ds.common.spi.constraint.ResourceConstraint;
import com.appresso.ds.common.spi.param.ResourceParameter;
import com.appresso.ds.dp.spi.Operation;
import com.appresso.ds.dp.spi.OperationConfiguration;
import com.appresso.ds.dp.spi.OperationConfigurator;
import com.appresso.ds.dp.spi.OperationContext;
import com.appresso.ds.dp.spi.OperationFactory;
import com.appresso.ds.dp.spi.OperationIO;
import com.appresso.ds.dp.spi.OperationMetaData;

public class SampleOperationFactory implements OperationFactory {
	public static final String KEY_SAMPLE_RESOURCE = "SAMPLE_RESOURCE";

	@Override
	public String getOperationName() {
		return "get_data";
	}

	@Override
	public OperationMetaData getOperationMetaData(
			OperationContext context) throws Exception {

		OperationMetaData meta = new OperationMetaData(this, context);
		meta.setLabel("サンプルリソース");
		meta.setBaseName("Sample Resource");
		meta.setEditSchemaSupported(false);
		return meta;
	}

	@Override
	public OperationConfigurator createOperationConfigurator(
			OperationConfiguration conf,
			OperationContext context) throws Exception {

		// OperationConfigurator を生成
		OperationConfigurator configurator = new OperationConfigurator(conf, context);

		// ResourceConstraint を生成
		ResourceConstraint constraint = new ResourceConstraint();
		// ラベルを設定
		constraint.setLabel("Sample Resource");
		// SampleResource 固有の名前を設定
		constraint.setResourceNameRegex(SampleResourceFactory.RESOURCE_NAME);
		// リソースパラメータを生成
		ResourceParameter param = new ResourceParameter(KEY_SAMPLE_RESOURCE, constraint);

		// リソースパラメータを追加
		configurator.addResourceParameter(param);
		return configurator;
	}

	@Override
	public OperationIO createOperationIO(
			OperationConfiguration conf,
			OperationContext context) throws Exception {
		return null;
	}

	@Override
	public Operation createOperation(
			OperationConfiguration conf,
			OperationContext context) throws Exception {

		return new SampleOperation(conf, context);
	}
}
