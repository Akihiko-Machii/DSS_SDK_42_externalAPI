package com.appresso.ds.dp.modules.adapter.simple_csv;

import static com.appresso.ds.dp.modules.adapter.simple_csv.Constants.KEY_FILE_PATH;
import static com.appresso.ds.dp.modules.adapter.simple_csv.Constants.KEY_NAME_COLUMN;
import static com.appresso.ds.dp.modules.adapter.simple_csv.Constants.KEY_TABLE;
import static com.appresso.ds.dp.modules.adapter.simple_csv.Constants.KEY_TYPE_COLUMN;
import static com.appresso.ds.dp.modules.adapter.simple_csv.Constants.TYPE_INTEGER;
import static com.appresso.ds.dp.modules.adapter.simple_csv.Constants.TYPE_STRING;
import static com.appresso.ds.dp.modules.adapter.simple_csv.Constants.typeItems;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.appresso.ds.common.kernel.modules.FileManager;
import com.appresso.ds.common.spi.constraint.FileInputFillin;
import com.appresso.ds.common.spi.constraint.Fillin;
import com.appresso.ds.common.spi.constraint.Multi;
import com.appresso.ds.common.spi.constraint.TableConstraint;
import com.appresso.ds.common.spi.param.Configuration.Table;
import com.appresso.ds.common.spi.param.Configurator;
import com.appresso.ds.common.spi.param.ParameterObject;
import com.appresso.ds.common.spi.param.ParameterObserver;
import com.appresso.ds.common.spi.param.SimpleParameter;
import com.appresso.ds.common.spi.param.TableColumn;
import com.appresso.ds.common.spi.param.TableParameter;
import com.appresso.ds.common.xmlfw.table.TableDataTypeUtil;
import com.appresso.ds.dp.spi.OperationConfiguration;
import com.appresso.ds.dp.spi.OperationConfigurator;
import com.appresso.ds.dp.spi.OperationContext;
import com.appresso.ds.dp.spi.OperationFactory;
import com.appresso.ds.dp.spi.TableSchemaGenerator;
import com.appresso.ds.xmlfw.XmlFrameworkConstraints;

public abstract class SimpleCSVOperationFactory implements OperationFactory {
	@Override
	public OperationConfigurator createOperationConfigurator(
			OperationConfiguration conf,
			OperationContext context) throws Exception {

		OperationConfigurator configurator = new OperationConfigurator(conf, context);

		// パラメータの追加
		configurator.addSimpleParameter(getFileInputParam());
		configurator.addTableParameter(getTableParam());

		// [TODO] 大容量データ処理のプロパティを設定します。
		XmlFrameworkConstraints.addTableDataParameter(configurator);

		// [TODO] ファイルパスを監視するオブザーバをセット
		configurator.setObserver(KEY_FILE_PATH, new FilePathObserver());
		return configurator;
	}

	private SimpleParameter getFileInputParam() {
		// [TODO] FileInputFillin のインスタンスを生成します。
		FileInputFillin constraint = new FileInputFillin();
		constraint.setLabel("ファイルパス");
		constraint.setShortcut("P");

		// [TODO] SimpleParameter のインスタンスを生成します。
		// - 第一引数 : KEY_FILE_PATH
		// - 第二引数 : FileInputFillin のインスタンス
		SimpleParameter param = new SimpleParameter(KEY_FILE_PATH, constraint);

		// [TODO] SimpleParameter のインスタンスを返します。
		return param;
	}

	private TableParameter getTableParam() {
		// [TODO] TableConstraint のインスタンスを生成ます。
		TableConstraint constraint = new TableConstraint();
		constraint.setLabel("列一覧");

		// [TODO] TableParameter のインスタンスを生成します。
		// - 第一引数 : KEY_TABLE
		// - 第二引数 : TableConstraint のインスタンス
		TableParameter param = new TableParameter(KEY_TABLE, constraint);

		// [TODO] 1列目の定義 (名前)
		// - TableColumn のインスタンスを生成します。
		// - 第一引数 : KEY_NAME_COLUMN
		// - 第二引数 : Fillin のインスタンス
		TableColumn nameColumn = new TableColumn(KEY_NAME_COLUMN, new Fillin());
		nameColumn.getConstraint().setLabel("名前");

		// [TODO] 2列目の定義 (型)
		// - TableColumn のインスタンスを生成します。
		// - 第一引数 : KEY_TYPE_COLUMN
		// - 第二引数 : Multi のインスタンス
		// - Multi の選択肢は、static フィールドの typeItems を設定
		Multi typeColumnConstraint = new Multi();
		typeColumnConstraint.setLabel("型");
		typeColumnConstraint.setItems(typeItems);
		TableColumn typeColumn = new TableColumn(KEY_TYPE_COLUMN, typeColumnConstraint);

		// [TODO] TableParameter に TableColumn のインスタンスを追加します。
		param.addColumn(nameColumn);
		param.addColumn(typeColumn);

		// [TODO] TableParameter のインスタンスを返します。
		return param;
	}

	String createSchema(OperationConfiguration conf) {
		Table table = conf.getTable(KEY_TABLE);
		// テーブルプロパティの行数を取得します。
		int rowCount = table.getRowCount();

		// TableSchemaGenerator を生成
		TableSchemaGenerator generator = new TableSchemaGenerator();

		for (int i = 0; i < rowCount; i++) {

			// [TODO] テーブルプロパティから設定されている名前を取得
			String name = table.getValue(i, KEY_NAME_COLUMN).toString();

			// [TODO] テーブルプロパティから設定されている型を取得
			String type = table.getValue(i, KEY_TYPE_COLUMN).toString();

			generator.addColumn(name, TableDataTypeUtil.parseType(type));
		}

		// 文字列として XMLSchema を返します。
		return generator.generateXMLSchema();
	}

	private class FilePathObserver implements ParameterObserver {
		@Override
		public String[] valueChanged(
				ParameterObject parameter,
				Configurator configurator) throws Exception {

			OperationConfigurator oc = (OperationConfigurator) configurator;
			// ファイルマネージャを取得
			FileManager fm = (FileManager) oc.getContext().getProxy(FileManager.class);

			SimpleParameter filePathParam = (SimpleParameter) parameter;

			// プロパティで設定されているファイルパスを取得
			String filePath = filePathParam.getCurrentValue();

			// 値が null または 空文字の場合、空の配列を返します。
			if (filePath == null || filePath.trim().isEmpty()) {
				return new String[0];
			}

			// 存在していない場合、空の配列を返します。
			if (!fm.exists(filePath)) {
				return new String[0];
			}

			// ファイルパスがディレクトリの場合、空の配列を返します。
			if (fm.isDirectory(filePath)) {
				return new String[0];
			}

			// [TODO] テーブルパラメータを取得
			TableParameter tableParam = (TableParameter) oc.getParameter(KEY_TABLE);

			// [TODO] テーブルプロパティに新しい値を設定
			tableParam.setNewValues(getNewValues(fm, filePath));

			// [TODO] 更新対象となるテーブルプロパティのキーを指定
			return new String[] { KEY_TABLE };
		}
	}

	/**
	 * 入力ファイルの一行目を読み取り、テーブルプロパティに設定する列名と型を返します。
	 * 
	 * @return String[][] テーブルプロパティに設定する値
	 */
	private String[][] getNewValues(FileManager fm, String filePath) throws Exception {

		try (BufferedReader reader = new BufferedReader(new InputStreamReader(fm.getInputStream(filePath)))) {

			List<String[]> newValues = new ArrayList<>();

			// ファイルの一行目を取得します。
			Optional<String> firstRecord = reader.lines().findFirst();
			firstRecord.ifPresent(line -> {
				for (String name : line.split(",")) {
					// Listに 列名,データ型 のString配列を追加します。
					newValues.add(new String[] { name, toTypeString(name) });
				}
			});
			// Listを二次元配列に変換し、返します。
			return newValues.toArray(new String[0][]);
		}
	}

	private String toTypeString(String str) {
		try {
			Integer.parseInt(str);
		} catch (Exception e) {
			return TYPE_STRING;
		}
		return TYPE_INTEGER;
	}
}
