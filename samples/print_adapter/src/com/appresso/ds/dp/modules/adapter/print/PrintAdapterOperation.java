package com.appresso.ds.dp.modules.adapter.print;

import java.io.BufferedOutputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import com.appresso.ds.common.xmlfw.xml.PrintingHandlerFactory;
import com.appresso.ds.common.xmlfw.xml.XmlHandler;
import com.appresso.ds.common.xmlfw.xml.XmlParser;
import com.appresso.ds.dp.share.adapter.file.FileAdapterOperation;
import com.appresso.ds.dp.share.adapter.file.FileConnection;
import com.appresso.ds.dp.spi.OperationContext;
import com.appresso.ds.xmlfw.DataParserFactory;

public class PrintAdapterOperation extends FileAdapterOperation {
	private String indent;
	private String charset;

	public PrintAdapterOperation(
			FileConnection con, OperationContext context) {

		super(con, context);
	}

	@Override
	public Map execute(Map inputData) throws Exception {
		try (OutputStream out = new BufferedOutputStream(getOutputStream())) {
			// XML ハンドラを生成
			XmlHandler printHandler = PrintingHandlerFactory.createXmlPrintHandler(out, charset, isIndent());

			// XML パーサを生成
			// 引数の Map オブジェクトから入力データを取得
			// キーは入力制約 (XMLInput) を定義したキーを指定
			XmlParser parser = DataParserFactory.newXmlParser(
					inputData.get(PrintAdapterOperationFactory.KEY_XML_INPUT));

			// パース開始 (ファイルにデータが出力されます)
			parser.parse(printHandler);

			// 結果データは空の Map オブジェクトを返します。
			return new HashMap();
		}
	}

	void setIndent(String indent) {
		this.indent = indent;
	}

	void setCharset(String charset) {
		this.charset = charset;
	}

	private boolean isIndent() {
		return Boolean.parseBoolean(indent);
	}

}
