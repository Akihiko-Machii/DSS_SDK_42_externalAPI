package com.appresso.ds.dp.modules.adapter.simple_csv;

import com.appresso.ds.common.spi.constraint.Item;

public interface Constants {

	/**
	 * 選択肢 (文字列型) です。
	 */
	String TYPE_STRING = "String";

	/**
	 * 選択肢 (整数型) です。
	 */
	String TYPE_INTEGER = "int";

	/**
	 * 型の選択肢です。
	 */
	Item[] typeItems = new Item[] {
			new Item(TYPE_STRING),
			new Item(TYPE_INTEGER)
	};

	/**
	 * テーブルプロパティのキーです。
	 */
	String KEY_TABLE = "TABLE";

	/**
	 * 入力制約のキーです。
	 */
	String KEY_INPUT = "INPUT";

	/**
	 * 出力制約のキーです。
	 */
	String KEY_OUTPUT = "OUTPUT";

	/**
	 * 列 (名前) のキーです。
	 */
	String KEY_NAME_COLUMN = "NAME_COLUMN";

	/**
	 * 列 (型) のキーです。
	 */
	String KEY_TYPE_COLUMN = "TYPE_COLUMN";

	/**
	 * ファイルを指定するプロパティのキーです。
	 */
	String KEY_FILE_PATH = "FILE_PATH";
}
