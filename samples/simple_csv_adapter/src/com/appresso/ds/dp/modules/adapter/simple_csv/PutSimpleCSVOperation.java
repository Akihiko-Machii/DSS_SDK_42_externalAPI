package com.appresso.ds.dp.modules.adapter.simple_csv;

import static com.appresso.ds.dp.modules.adapter.simple_csv.Constants.KEY_INPUT;

import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Map;

import com.appresso.ds.common.kernel.modules.FileManager;
import com.appresso.ds.common.xmlfw.table.Column;
import com.appresso.ds.common.xmlfw.table.Row;
import com.appresso.ds.common.xmlfw.table.TableDataType;
import com.appresso.ds.common.xmlfw.table.TableRowIterator;
import com.appresso.ds.dp.spi.OperationConfiguration;
import com.appresso.ds.dp.spi.OperationContext;
import com.appresso.ds.xmlfw.DataParserFactory;

public class PutSimpleCSVOperation extends SimpleCSVOperation {
	public PutSimpleCSVOperation(
			OperationConfiguration conf,
			OperationContext context) {

		super(conf, context);
	}

	@Override
	public Map execute(Map inputData) throws Exception {
		// ファイルマネージャを取得
		FileManager fm = (FileManager) getContext().getProxy(FileManager.class);

		try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(fm.getOutputStream(getFilePath())))) {
			// [TODO] 入力データの取得
			Object data = inputData.get(KEY_INPUT);

			try (TableRowIterator rowIterator = DataParserFactory.newTableRowIterator(data)) {
				// TableRowIterator のイテレータ処理
				while (rowIterator.hasNext()) {
					// [TODO] イテレータで Row の取得
					Row record = rowIterator.next();
					StringBuilder builder = new StringBuilder();

					for (int columnIndex = 0, columnCount = record
							.getColumnCount(); columnIndex < columnCount; columnIndex++) {
						// [TODO] Row から Column を取得
						Column column = record.getColumn(columnIndex);
						String columnValue = getColumnValue(column);

						if (columnIndex > 0) {
							builder.append(",");
						}
						builder.append(columnValue);
					}
					writer.write(builder.toString());
					writer.newLine();
				}
				writer.flush();
			}
		}
		Map ret = new HashMap();
		return ret;
	}

	/**
	 * Column オブジェクトから出力文字列を取得します。 型のチェックを行ってその型に合ったメソッドで値を取得します。
	 */
	private String getColumnValue(Column column) throws Exception {
		switch (column.getType()) {
			case TableDataType.TYPE_INT:
				return String.valueOf(column.getIntegerValue());
			default:
				return column.getStringValue();
		}
	}

	@Override
	public void destroy() throws Exception {
		// オペレーション終了処理
	}
}
