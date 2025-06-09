package com.appresso.ds.dp.modules.adapter.helloworld;

import com.appresso.ds.dp.spi.GetDataOperationFactory;
import com.appresso.ds.dp.spi.Operation;
import com.appresso.ds.dp.spi.OperationConfiguration;
import com.appresso.ds.dp.spi.OperationConfigurator;
import com.appresso.ds.dp.spi.OperationContext;
import com.appresso.ds.dp.spi.OperationIO;

public class HelloWorldAdapterOperationFactory extends GetDataOperationFactory {
	@Override
	public OperationConfigurator createOperationConfigurator(
			OperationConfiguration conf, OperationContext context) throws Exception {

		// プロパティは実装しないので、null を返します。
		return null;
	}

	@Override
	public OperationIO createOperationIO(
			OperationConfiguration conf, OperationContext context) throws Exception {

		// データの入出力はしないので、null を返します。
		return null;
	}

	@Override
	public Operation createOperation(
			OperationConfiguration conf, OperationContext context) throws Exception {

		// オペレーションを返します。
		return new HelloWorldAdapterOperation(context);
	}
}