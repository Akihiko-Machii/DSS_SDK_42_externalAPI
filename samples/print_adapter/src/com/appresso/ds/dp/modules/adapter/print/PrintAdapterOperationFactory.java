package com.appresso.ds.dp.modules.adapter.print;

import com.appresso.ds.common.spi.constraint.CharsetNameFillinMulti;
import com.appresso.ds.common.spi.constraint.FileInputFillin;
import com.appresso.ds.common.spi.constraint.Item;
import com.appresso.ds.common.spi.constraint.Multi;
import com.appresso.ds.common.spi.constraint.XMLInput;
import com.appresso.ds.common.spi.param.SimpleParameter;
import com.appresso.ds.dp.share.adapter.file.FileAdapterUtil;
import com.appresso.ds.dp.share.adapter.file.FileConnection;
import com.appresso.ds.dp.spi.Operation;
import com.appresso.ds.dp.spi.OperationConfiguration;
import com.appresso.ds.dp.spi.OperationConfigurator;
import com.appresso.ds.dp.spi.OperationContext;
import com.appresso.ds.dp.spi.OperationIO;
import com.appresso.ds.dp.spi.PutDataOperationFactory;

public class PrintAdapterOperationFactory extends PutDataOperationFactory {
	static final String KEY_XML_INPUT = "XML_INPUT";
	static final String KEY_FILE = "FILE";
	static final String KEY_INDENT = "INDENT";
	static final String KEY_CHARSET = "CHARSET";

	@Override
	public OperationConfigurator createOperationConfigurator(
			OperationConfiguration conf,
			OperationContext context) throws Exception {

		OperationConfigurator configurator = new OperationConfigurator(conf, context);

		configurator.addSimpleParameter(createFileInputParameter());
		configurator.addSimpleParameter(createCharsetParameter());
		configurator.addSimpleParameter(createIndentParameter());

		return configurator;
	}

	@Override
	public OperationIO createOperationIO(
			OperationConfiguration conf,
			OperationContext context) throws Exception {

		OperationIO io = new OperationIO();
		// 入力データが XML 型であることを定義
		XMLInput input = new XMLInput(KEY_XML_INPUT);
		io.addInput(input);

		return io;
	}

	@Override
	public Operation createOperation(
			OperationConfiguration conf,
			OperationContext context) throws Exception {

		// プロパティで設定された値を取得します。
		String filePath = conf.getValue(KEY_FILE).toString();
		String indent = conf.getValue(KEY_INDENT).toString();
		String charset = conf.getValue(KEY_CHARSET).toString();

		// FileConnection を生成
		FileConnection con = FileAdapterUtil.getConnection(filePath, context, conf);
		PrintAdapterOperation op = new PrintAdapterOperation(con, context);

		// インデント、文字セットをオペレーションに設定
		op.setIndent(indent);
		op.setCharset(charset);

		return op;
	}

	private SimpleParameter createFileInputParameter() {
		// ファイルを指定するプロパティ制約を定義
		FileInputFillin constraint = new FileInputFillin();
		constraint.setLabel("ファイル");
		constraint.setRequired(true);
		constraint.setShortcut("F");
		constraint.setDescription("書き込み先のファイルを指定します");

		return new SimpleParameter(KEY_FILE, constraint);
	}

	private SimpleParameter createIndentParameter() {
		// 選択形式のプロパティ制約を定義
		Multi constraint = new Multi();
		constraint.setLabel("インデント");
		constraint.setRequired(true);
		constraint.setShortcut("I");
		constraint.setDescription("インデントを入れるかどうか指定します");
		// 選択肢を定義
		constraint.setItems(new Item[] {
				new Item("true"),
				new Item("false")
		});

		// GUI の表示形式をコンボボックスに設定
		constraint.setStyle(Multi.STYLE_COMBOBOX);

		return new SimpleParameter(KEY_INDENT, constraint);
	}

	private SimpleParameter createCharsetParameter() {
		// 文字セットを選択できるプロパティ制約を定義します。
		CharsetNameFillinMulti constraint = new CharsetNameFillinMulti();
		constraint.setLabel("文字セット");
		constraint.setRequired(true);
		constraint.setShortcut("C");
		constraint.setDescription("書き込み先の文字セットを指定します");

		return new SimpleParameter(KEY_CHARSET, constraint);
	}

}
