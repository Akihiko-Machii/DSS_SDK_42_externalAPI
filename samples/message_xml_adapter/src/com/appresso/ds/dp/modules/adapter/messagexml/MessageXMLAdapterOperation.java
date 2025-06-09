package com.appresso.ds.dp.modules.adapter.messagexml;

import java.util.HashMap;
import java.util.Map;

import com.appresso.ds.common.xmlfw.xml.XmlBuilder;
import com.appresso.ds.dp.spi.Operation;
import com.appresso.ds.dp.spi.OperationConfiguration;
import com.appresso.ds.dp.spi.OperationContext;
import com.appresso.ds.xmlfw.DataBuilderFactory;

public class MessageXMLAdapterOperation implements Operation {
	private final OperationConfiguration conf;
	private final OperationContext context;

	public MessageXMLAdapterOperation(
			OperationConfiguration conf, OperationContext context) throws Exception {
		this.conf = conf;
		this.context = context;
	}

	@Override
	public Map execute(Map inputData) throws Exception {
		String message = conf.getValue(MessageXMLAdapterOperationFactory.KEY_MESSAGE).toString();

		// XmlBuilder の生成
		XmlBuilder builder = DataBuilderFactory.newMemoryXmlBuilder();

		builder.startDocument();
		builder.startElement("message");
		builder.cdata(message);
		builder.endElement("message");
		builder.endDocument();

		Map ret = new HashMap();
		// 生成された XML を結果データとして Map に格納
		// キーは出力型制約(XMLOutput)を定義したキーを指定します。
		ret.put(MessageXMLAdapterOperationFactory.KEY_RESULT, builder.getResult());
		return ret;
	}

	@Override
	public void destroy() throws Exception {
	}
}
