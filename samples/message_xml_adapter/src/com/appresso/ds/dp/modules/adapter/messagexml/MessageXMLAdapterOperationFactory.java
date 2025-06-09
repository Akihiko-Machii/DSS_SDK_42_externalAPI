package com.appresso.ds.dp.modules.adapter.messagexml;

import com.appresso.ds.common.spi.constraint.Fillin;
import com.appresso.ds.common.spi.constraint.XMLOutput;
import com.appresso.ds.common.spi.param.SimpleParameter;
import com.appresso.ds.dp.spi.GetDataOperationFactory;
import com.appresso.ds.dp.spi.Operation;
import com.appresso.ds.dp.spi.OperationConfiguration;
import com.appresso.ds.dp.spi.OperationConfigurator;
import com.appresso.ds.dp.spi.OperationContext;
import com.appresso.ds.dp.spi.OperationIO;

public class MessageXMLAdapterOperationFactory extends GetDataOperationFactory {
	static final String KEY_MESSAGE = "MESSAGE";
	static final String KEY_RESULT = "RESULT";

	@Override
	public OperationConfigurator createOperationConfigurator(
			OperationConfiguration conf, OperationContext context) throws Exception {

		// オペレーションの設定方法 (プロパティ) を表すクラスです。
		OperationConfigurator configurator = new OperationConfigurator(conf, context);

		// パラメータ (プロパティ) を追加します。
		configurator.addSimpleParameter(createMessageParameter());

		return configurator;
	}

	@Override
	public OperationIO createOperationIO(
			OperationConfiguration conf, OperationContext context) throws Exception {

		OperationIO io = new OperationIO();
		// 出力データが XML 型であることを定義します。
		XMLOutput output = new XMLOutput(KEY_RESULT);
		io.addOutput(output);

		return io;
	}

	@Override
	public Operation createOperation(
			OperationConfiguration conf, OperationContext context) throws Exception {

		return new MessageXMLAdapterOperation(conf, context);
	}

	// プロパティを表すパラメータの作成
	private SimpleParameter createMessageParameter() {

		// パラメータに自由入力型のプロパティ制約を定義します。
		Fillin fillin = new Fillin();
		fillin.setLabel("メッセージ");
		fillin.setRequired(true);
		fillin.setShortcut("M");
		fillin.setDescription("出力メッセージを指定");

		return new SimpleParameter(KEY_MESSAGE, fillin);
	}
}
