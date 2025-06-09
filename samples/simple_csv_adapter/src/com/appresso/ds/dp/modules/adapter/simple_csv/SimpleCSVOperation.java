package com.appresso.ds.dp.modules.adapter.simple_csv;

import static com.appresso.ds.dp.modules.adapter.simple_csv.Constants.KEY_FILE_PATH;
import static com.appresso.ds.dp.modules.adapter.simple_csv.Constants.KEY_TABLE;

import com.appresso.ds.common.xmlfw.table.TableDataTypeUtil;
import com.appresso.ds.dp.spi.Operation;
import com.appresso.ds.dp.spi.OperationConfiguration;
import com.appresso.ds.dp.spi.OperationContext;
import com.appresso.ds.xmlfw.XmlFrameworkConstraints;

public abstract class SimpleCSVOperation implements Operation {
	private final OperationConfiguration conf;
	private final OperationContext context;

	public SimpleCSVOperation(
			OperationConfiguration conf,
			OperationContext context) {

		this.conf = conf;
		this.context = context;
	}

	OperationContext getContext() {
		return context;
	}

	/**
	 * プロパティからファイルパスを取得します。
	 */
	String getFilePath() {
		return conf.getValue(KEY_FILE_PATH).toString();
	}

	/**
	 * テーブルプロパティから列の型一覧を取得します。
	 */
	int[] getColumnTypes() {
		int rowCount = conf.getTable(KEY_TABLE).getRowCount();

		int[] columnTypes = new int[rowCount];
		for (int i = 0; i < rowCount; i++) {
			String strTypes = conf.getTable(KEY_TABLE).getValue(i, 1).toString();
			columnTypes[i] = TableDataTypeUtil.parseType(strTypes);
		}
		return columnTypes;
	}

	/**
	 * テーブルプロパティから指定したインデックスの列の型を取得します。
	 */
	int getColumnType(int index) {
		String strType = conf.getTable(KEY_TABLE).getValue(index, 1).toString();
		return TableDataTypeUtil.parseType(strType);
	}

	/**
	 * 大容量データ処理に対応するかどうか取得します。
	 */
	boolean isLargeData() throws Exception {
		return XmlFrameworkConstraints.isLargeDataSelected(conf, context);
	}

	@Override
	public void destroy() throws Exception {
	}
}
