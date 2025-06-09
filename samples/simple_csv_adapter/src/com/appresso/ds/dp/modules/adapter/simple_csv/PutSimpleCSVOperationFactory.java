package com.appresso.ds.dp.modules.adapter.simple_csv;

import static com.appresso.ds.dp.modules.adapter.simple_csv.Constants.KEY_INPUT;

import com.appresso.ds.common.spi.constraint.XMLInput;
import com.appresso.ds.dp.spi.Operation;
import com.appresso.ds.dp.spi.OperationConfiguration;
import com.appresso.ds.dp.spi.OperationContext;
import com.appresso.ds.dp.spi.OperationIO;
import com.appresso.ds.dp.spi.OperationMetaData;

public class PutSimpleCSVOperationFactory extends SimpleCSVOperationFactory {
	@Override
	public String getOperationName() {
		return "put_data";
	}

	@Override
	public OperationMetaData getOperationMetaData(OperationContext context) throws Exception {
		OperationMetaData meta = new OperationMetaData(this, context);

		// [TODO ] ベース名を設定します。
		meta.setBaseName("write");

		// [TODO ] オペレーションアイコンの名前を設定します。
		meta.setLabel("書き込み");

		return meta;
	}

	@Override
	public OperationIO createOperationIO(
			OperationConfiguration conf,
			OperationContext context) throws Exception {

		OperationIO io = new OperationIO();

		XMLInput input = new XMLInput(KEY_INPUT);

		// [TODO] XMLInput のインスタンスに XML Schema を設定
		input.setSchema(createSchema(conf));

		io.addInput(input);
		return io;
	}

	@Override
	public Operation createOperation(
			OperationConfiguration conf,
			OperationContext context) throws Exception {

		// [TODO] PutSimpleCSVOperation のインスタンスを返します。
		return new PutSimpleCSVOperation(conf, context);
	}
}
