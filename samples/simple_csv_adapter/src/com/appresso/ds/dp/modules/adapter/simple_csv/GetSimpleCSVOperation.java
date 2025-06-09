package com.appresso.ds.dp.modules.adapter.simple_csv;

import static com.appresso.ds.dp.modules.adapter.simple_csv.Constants.KEY_OUTPUT;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import com.appresso.ds.common.kernel.modules.FileManager;
import com.appresso.ds.common.xmlfw.table.TableDataBuilder;
import com.appresso.ds.common.xmlfw.table.TableDataType;
import com.appresso.ds.dp.spi.OperationConfiguration;
import com.appresso.ds.dp.spi.OperationContext;
import com.appresso.ds.xmlfw.DataBuilderFactory;

public class GetSimpleCSVOperation extends SimpleCSVOperation {
	public GetSimpleCSVOperation(
			OperationConfiguration conf,
			OperationContext context) {

		super(conf, context);
	}

	@Override
	public Map execute(Map inputData) throws Exception {
		Map res = new HashMap();

		// ファイルマネージャを取得
		FileManager fm = (FileManager) getContext().getProxy(FileManager.class);

		String filePath = getFilePath();

		// ファイルパス入力値のチェック
		if (filePath == null || filePath.trim().isEmpty()) {
			return res;
		}

		// ファイルパス入力値のチェック
		if (!fm.exists(filePath) || fm.isDirectory(filePath)) {
			return res;
		}

		try (BufferedReader reader = new BufferedReader(new InputStreamReader(fm.getInputStream(filePath)))) {
			// [TODO] TableDataBuilder の生成
			TableDataBuilder builder = DataBuilderFactory.newTableDataBuilder(getColumnTypes(), isLargeData());

			// [TODO] データ作成開始のマーク
			builder.startConstruction();

			reader.lines().forEach(line -> {
				// [TODO] 行開始のマーク
				try {
					builder.startRow();

					// カンマ区切りの文字列を解析
					String[] columns = line.split(",");
					for (int i = 0; i < columns.length; i++) {
						// [TODO] 型をチェックしてカラムを追加
						switch (getColumnType(i)) {
							case TableDataType.TYPE_INT:
								builder.column(Integer.parseInt(columns[i]));
								break;
							default:
								builder.column(columns[i]);
						}
					}

					// [TODO] 行終了のマーク
					builder.endRow();
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			});

			// [TODO] データ作成終了のマーク
			builder.endConstruction();

			// [TODO] 結果データの取得
			Object result = builder.getResult();

			// [TODO] Map オブジェクトに結果データを格納
			res.put(KEY_OUTPUT, result);
		}
		return res;
	}

	@Override
	public void destroy() throws Exception {
		// オペレーション終了処理
	}
}
