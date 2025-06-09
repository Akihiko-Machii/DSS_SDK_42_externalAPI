package com.appresso.ds.dp.modules.adapter.simple_csv;

import static com.appresso.ds.dp.modules.adapter.simple_csv.Constants.KEY_OUTPUT;

import com.appresso.ds.common.spi.constraint.XMLOutput;
import com.appresso.ds.dp.spi.Operation;
import com.appresso.ds.dp.spi.OperationConfiguration;
import com.appresso.ds.dp.spi.OperationContext;
import com.appresso.ds.dp.spi.OperationIO;
import com.appresso.ds.dp.spi.OperationMetaData;

public class GetSimpleCSVOperationFactory extends SimpleCSVOperationFactory {
	@Override
	public String getOperationName() {
		return "get_data";
	}

	@Override
	public OperationMetaData getOperationMetaData(OperationContext context) throws Exception {
		OperationMetaData meta = new OperationMetaData(this, context);

		// [TODO ] ベース名を設定します。
		meta.setBaseName("read");

		// [TODO ] オペレーションアイコンの名前を設定します。
		meta.setLabel("読み取り");

		return meta;
	}

	@Override
	public OperationIO createOperationIO(
			OperationConfiguration conf,
			OperationContext context) throws Exception {

		OperationIO io = new OperationIO();

		XMLOutput output = new XMLOutput(KEY_OUTPUT);

		// [TODO] XMLOutput のインスタンスに XML Schema を設定
		output.setSchema(createSchema(conf));

		io.addOutput(output);
		return io;
	}

	@Override
	public Operation createOperation(
			OperationConfiguration conf,
			OperationContext context) throws Exception {

		// [TODO] GetSimpleCSVOperation のインスタンスを返します。
		return new GetSimpleCSVOperation(conf, context);
	}
}
