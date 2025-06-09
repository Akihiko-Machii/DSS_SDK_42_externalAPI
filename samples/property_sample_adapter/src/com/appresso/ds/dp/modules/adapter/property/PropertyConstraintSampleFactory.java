package com.appresso.ds.dp.modules.adapter.property;

import com.appresso.ds.common.dp.InvalidPropertyConfigurationException;
import com.appresso.ds.common.fw.var.VariableUtil;
import com.appresso.ds.common.kernel.modules.FileManager;
import com.appresso.ds.common.spi.constraint.CharsetNameFillinMulti;
import com.appresso.ds.common.spi.constraint.CheckBox;
import com.appresso.ds.common.spi.constraint.FileInputFillin;
import com.appresso.ds.common.spi.constraint.Fillin;
import com.appresso.ds.common.spi.constraint.FillinMulti;
import com.appresso.ds.common.spi.constraint.InformationConstraint;
import com.appresso.ds.common.spi.constraint.Item;
import com.appresso.ds.common.spi.constraint.Multi;
import com.appresso.ds.common.spi.constraint.MultipleLineFillin;
import com.appresso.ds.common.spi.constraint.NumberFillin;
import com.appresso.ds.common.spi.constraint.ParameterGroup;
import com.appresso.ds.common.spi.constraint.PasswordFillin;
import com.appresso.ds.common.spi.param.Configurator;
import com.appresso.ds.common.spi.param.InformationParameter;
import com.appresso.ds.common.spi.param.ParameterObject;
import com.appresso.ds.common.spi.param.ParameterObserver;
import com.appresso.ds.common.spi.param.SimpleParameter;
import com.appresso.ds.dp.spi.Operation;
import com.appresso.ds.dp.spi.OperationConfiguration;
import com.appresso.ds.dp.spi.OperationConfigurator;
import com.appresso.ds.dp.spi.OperationContext;
import com.appresso.ds.dp.spi.OperationIO;
import com.appresso.ds.dp.spi.PutDataOperationFactory;

public class PropertyConstraintSampleFactory extends PutDataOperationFactory {
	private static final String KEY_FILLIN_GROUP = "FILLIN_GROUP";
	private static final String KEY_MULTI_GROUP = "MULTI_GROUP";
	private static final String KEY_OTHER_GROUP = "OTHER_GROUP";

	private static final String KEY_MULTI_COMBOBOX = "MULTI_COMBOBOX";
	private static final String KEY_MULTI_RADIOBUTTON = "MULTI_RADIOBUTTON";
	private static final String KEY_MULTI_LIST = "MULTI_LIST";
	private static final String KEY_FILLIN_MULTI = "FILLIN_MULTI";
	private static final String KEY_CHARSETNAMEFILLIN_MULTI = "CHARSETNAMEFILLIN_MULTI";

	private static final String KEY_FILLIN = "FILLIN";
	private static final String KEY_FILEINPUT_FILLIN = "FILEINPUT_FILLIN";
	private static final String KEY_PASSWORD_FILLIN = "PASSWORD_FILLIN";
	private static final String KEY_NUMBER_FILLIN = "NUMBER_FILLIN";
	private static final String KEY_MULTIPLELINE_FILLIN = "MULTIPLELINE_FILLIN";

	private static final String KEY_CHECKBOX = "CHECKBOX";
	private static final String KEY_INFO_CONSTRAINT = "INFO_CONSTRAINT";

	private static final String[] MultiRadioButtonItems = new String[] { "Multi(ComboBox)を無効", "Multi(List)を無効",
			"FillinMultiを無効" };
	private static final String[] MultiComboBoxItems = new String[] { "CharsetNameFillinMultiを有効",
			"CharsetNameFillinMultiを無効" };

	@Override
	public OperationConfigurator createOperationConfigurator(
			OperationConfiguration conf,
			OperationContext context) throws Exception {

		OperationConfigurator configurator = new OperationConfigurator(conf, context);
		// プロパティのタブを任意に設定することができます
		configurator.addGroup(new ParameterGroup(KEY_FILLIN_GROUP, "Fillin"));
		configurator.addGroup(new ParameterGroup(KEY_MULTI_GROUP, "Multi"));
		configurator.addGroup(new ParameterGroup(KEY_OTHER_GROUP, "その他"));

		configurator.addSimpleParameter(KEY_FILLIN_GROUP, createFillinParameter());
		configurator.addSimpleParameter(KEY_FILLIN_GROUP, createFileInputFillinParameter(context));
		configurator.addSimpleParameter(KEY_FILLIN_GROUP, createPasswordFillinParameter());
		configurator.addSimpleParameter(KEY_FILLIN_GROUP, createNumberFillinParameter());
		configurator.addSimpleParameter(KEY_FILLIN_GROUP, createMultipleLineFillinParameter());

		configurator.addSimpleParameter(KEY_MULTI_GROUP, createMultiRadioButtonParameter());
		configurator.addSimpleParameter(KEY_MULTI_GROUP, createMultiListParameter(conf));
		configurator.addSimpleParameter(KEY_MULTI_GROUP, createFillinMultiParameter(conf));
		configurator.addSimpleParameter(KEY_MULTI_GROUP, createMultiComboBoxParameter(conf));
		configurator.addSimpleParameter(KEY_MULTI_GROUP, createCharsetNameFillinMultiParameter(conf));

		configurator.addSimpleParameter(KEY_OTHER_GROUP, createCheckBoxParameter());
		configurator.addInformationParameter(KEY_OTHER_GROUP, createInformationConstraintParameter());

		// 特定のフィールドに監視オブジェクト (Observer) をセットすることができます
		configurator.setObserver(KEY_MULTI_COMBOBOX, new MultiComboBoxObserver());
		configurator.setObserver(KEY_MULTI_RADIOBUTTON, new MultiRadioButtonObserver());
		return configurator;
	}

	@Override
	public OperationIO createOperationIO(
			OperationConfiguration conf,
			OperationContext context) throws Exception {
		return null;
	}

	@Override
	public Operation createOperation(
			OperationConfiguration conf,
			OperationContext context) throws Exception {
		return null;
	}

	// ==============================================================================================
	// com.appresso.ds.common.dp.constraint.Fillin の サブクラスのプロパティ制約
	// ==============================================================================================

	private SimpleParameter createFillinParameter() {
		Fillin constraint = new Fillin();
		constraint.setLabel("Fillin");
		constraint.setRequired(true);
		constraint.setShortcut("F");
		constraint.setLength(4);
		constraint.setDescription("5 文字以上入力するとエラーメッセージが表示されます");
		return new SimpleParameter(KEY_FILLIN, constraint);
	}

	private SimpleParameter createMultipleLineFillinParameter() {
		MultipleLineFillin constraint = new MultipleLineFillin();
		constraint.setLabel("MultipleLineFillin");
		constraint.setRequired(true);
		constraint.setShortcut("M");
		constraint.setRows(2);
		constraint.setDescription("設定した行数は 2 です");
		return new SimpleParameter(KEY_MULTIPLELINE_FILLIN, constraint);
	}

	private SimpleParameter createFileInputFillinParameter(
			final OperationContext context) {

		FileInputFillin constraint = new FileInputFillin();
		constraint.setLabel("FileInputFillin");
		constraint.setRequired(true);
		constraint.setShortcut("I");
		constraint.setFileExtensions(new String[] { "xml" });
		constraint.setDescription("DataSpider ファイルシステムのパスを指定します");
		SimpleParameter parameter = new SimpleParameter(KEY_FILEINPUT_FILLIN, constraint);

		// 入力されたファイルパスをチェックするための Verifier を定義します
		parameter.setVerifier((value, configurator) -> {
			if (value == null || value.isEmpty()) {
				throw new InvalidPropertyConfigurationException("ファイルパスが入力されていません。");
			}

			if (VariableUtil.isVariableIncluded(value)) {
				return;
			}

			FileManager fileManager = (FileManager) context.getProxy(FileManager.class);
			if (!fileManager.exists(value)) {
				throw new InvalidPropertyConfigurationException("指定されたファイルは存在しません。");
			}
			if (fileManager.isDirectory(value)) {
				throw new InvalidPropertyConfigurationException("指定されたパスはディレクトリです。");
			}
			if (!fileManager.canRead(value)) {
				throw new InvalidPropertyConfigurationException("指定されたファイルを読み込むことはできません。");
			}
		});

		return parameter;
	}

	private SimpleParameter createPasswordFillinParameter() {
		PasswordFillin constraint = new PasswordFillin();
		constraint.setLabel("PasswordFillin");
		constraint.setRequired(true);
		constraint.setShortcut("P");
		constraint.setDescription("文字列がHIDDEN_STRINGで隠されるプロパティ制約です");
		return new SimpleParameter(KEY_PASSWORD_FILLIN, constraint);
	}

	private SimpleParameter createNumberFillinParameter() {
		NumberFillin constraint = new NumberFillin();
		constraint.setLabel("NumberFillin");
		constraint.setRequired(true);
		constraint.setShortcut("N");
		constraint.setAllowDouble(false);
		constraint.setAllowMax(true);
		constraint.setAllowMin(true);
		constraint.setMaxValue(100D);
		constraint.setMinValue(0D);
		constraint.setDescription("0 以上 100 以下の値以外の 数値を入力するとエラーメッセージが表示されます");
		return new SimpleParameter(KEY_NUMBER_FILLIN, constraint);
	}

	// ==============================================================================================
	// com.appresso.ds.common.dp.constraint.Multi の サブクラスのプロパティ制約
	// ==============================================================================================

	private SimpleParameter createMultiRadioButtonParameter() {
		Multi constraint = new Multi();
		constraint.setLabel("Multi(RadioButton)");
		constraint.setRequired(true);
		constraint.setShortcut("R");
		constraint.setStyle(Multi.STYLE_RADIOBUTTON);
		constraint.setDescription("選択した値によって無効になるフィールドを切り替えます");
		constraint.setItems(new Item[] {
				new Item(MultiRadioButtonItems[0]),
				new Item(MultiRadioButtonItems[1]),
				new Item(MultiRadioButtonItems[2])
		});
		return new SimpleParameter(KEY_MULTI_RADIOBUTTON, constraint);
	}

	private SimpleParameter createMultiComboBoxParameter(
			OperationConfiguration conf) {

		Multi constraint = new Multi();
		constraint.setLabel("Multi(ComboBox)");
		constraint.setRequired(true);
		constraint.setShortcut("B");
		constraint.setDescription("false を選択すると FillinMulti のプロパティが無効になります");
		constraint.setItems(new Item[] {
				new Item(MultiComboBoxItems[0]),
				new Item(MultiComboBoxItems[1])
		});

		String radioButtonValue = conf.getValue(KEY_MULTI_RADIOBUTTON).toString();
		constraint.setEnabled(!MultiRadioButtonItems[0].equals(radioButtonValue));

		return new SimpleParameter(KEY_MULTI_COMBOBOX, constraint);
	}

	private SimpleParameter createMultiListParameter(
			OperationConfiguration conf) {

		Multi constraint = new Multi();
		constraint.setLabel("Multi(List)");
		constraint.setRequired(true);
		constraint.setShortcut("L");
		constraint.setStyle(Multi.STYLE_LIST);
		constraint.setDescription("false を選択すると FillinMulti のプロパティが無効になります");
		constraint.setItems(new Item[] {
				new Item(Boolean.TRUE.toString()),
				new Item(Boolean.FALSE.toString())
		});

		String radioButtonValue = conf.getValue(KEY_MULTI_RADIOBUTTON).toString();
		constraint.setEnabled(!MultiRadioButtonItems[1].equals(radioButtonValue));

		return new SimpleParameter(KEY_MULTI_LIST, constraint);
	}

	private SimpleParameter createFillinMultiParameter(
			OperationConfiguration conf) {

		FillinMulti constraint = new FillinMulti();
		constraint.setLabel("FillinMulti");
		constraint.setRequired(true);
		constraint.setShortcut("F");
		constraint.setDescription("自由入力可能な選択型のプロパティ制約です");
		constraint.setItems(new Item[] {
				new Item(Boolean.TRUE.toString()),
				new Item(Boolean.FALSE.toString())
		});

		String radioButtonValue = conf.getValue(KEY_MULTI_RADIOBUTTON).toString();
		constraint.setEnabled(!MultiRadioButtonItems[2].equals(radioButtonValue));

		return new SimpleParameter(KEY_FILLIN_MULTI, constraint);
	}

	private SimpleParameter createCharsetNameFillinMultiParameter(
			OperationConfiguration conf) {

		CharsetNameFillinMulti constraint = new CharsetNameFillinMulti();
		constraint.setLabel("CharsetNameFillinMulti");
		constraint.setRequired(true);
		constraint.setShortcut("C");
		constraint.setDescription("デフォルトは Windows の場合 Windows-31J です");

		String radioButtonValue = conf.getValue(KEY_MULTI_COMBOBOX).toString();
		constraint.setEnabled(MultiComboBoxItems[0].equals(radioButtonValue));

		return new SimpleParameter(KEY_CHARSETNAMEFILLIN_MULTI, constraint);
	}

	// ==============================================================================================
	// com.appresso.ds.common.dp.constraint.CheckBox
	// ==============================================================================================

	private SimpleParameter createCheckBoxParameter() {
		CheckBox constraint = new CheckBox();
		constraint.setLabel("CheckBox");
		constraint.setRequired(true);
		constraint.setShortcut("C");
		constraint.setChecked(true);
		constraint.setDescription("初期状態はチェックされています");
		return new SimpleParameter(KEY_CHECKBOX, constraint);
	}

	// ==============================================================================================
	// com.appresso.ds.common.dp.constraint.InformationConstraint
	// ==============================================================================================

	private InformationParameter createInformationConstraintParameter() {
		InformationConstraint constraint = new InformationConstraint("", "");
		constraint.setLabel("InformationConstraint");
		constraint.setRequired(true);
		constraint.setShortcut("I");
		constraint.setTitle("タイトル");
		constraint.setMessage("メッセージ");
		constraint.setDescription("ここはプロパティの説明です");
		return new InformationParameter(KEY_INFO_CONSTRAINT, constraint);
	}

	// ==============================================================================================
	// Observer
	// ==============================================================================================

	private class MultiComboBoxObserver implements ParameterObserver {
		@Override
		public String[] valueChanged(
				ParameterObject parameter, Configurator configurator) throws Exception {
			SimpleParameter multiParam = (SimpleParameter) parameter;
			String currentValue = multiParam.getCurrentValue();

			SimpleParameter charsetNameFillinMultiParam = (SimpleParameter) configurator
					.getParameter(KEY_CHARSETNAMEFILLIN_MULTI);

			// Observer をセットしたフィールドの値によって別のフィールドの
			// 状態を動的に変更することができます
			if (MultiComboBoxItems[0].equals(currentValue)) {
				charsetNameFillinMultiParam.getConstraint().setEnabled(true);
			} else if (MultiComboBoxItems[1].equals(currentValue)) {
				charsetNameFillinMultiParam.getConstraint().setEnabled(false);
			}

			// 変更されたフィールドのキーを返します
			return new String[] { KEY_CHARSETNAMEFILLIN_MULTI };
		}
	}

	private class MultiRadioButtonObserver implements ParameterObserver {
		@Override
		public String[] valueChanged(ParameterObject parameter, Configurator configurator) throws Exception {
			SimpleParameter radioButtonParam = (SimpleParameter) parameter;
			String currentValue = radioButtonParam.getCurrentValue();

			SimpleParameter comboBoxParam = (SimpleParameter) configurator.getParameter(KEY_MULTI_COMBOBOX);
			SimpleParameter listParam = (SimpleParameter) configurator.getParameter(KEY_MULTI_LIST);
			SimpleParameter fillinMultiParam = (SimpleParameter) configurator.getParameter(KEY_FILLIN_MULTI);

			if (MultiRadioButtonItems[0].equals(currentValue)) {
				comboBoxParam.getConstraint().setEnabled(false);
				listParam.getConstraint().setEnabled(true);
				fillinMultiParam.getConstraint().setEnabled(true);
			} else if (MultiRadioButtonItems[1].equals(currentValue)) {
				comboBoxParam.getConstraint().setEnabled(true);
				listParam.getConstraint().setEnabled(false);
				fillinMultiParam.getConstraint().setEnabled(true);
			} else if (MultiRadioButtonItems[2].equals(currentValue)) {
				comboBoxParam.getConstraint().setEnabled(true);
				listParam.getConstraint().setEnabled(true);
				fillinMultiParam.getConstraint().setEnabled(false);
			}

			// 変更されたフィールドのキーを返します
			return new String[] {
					KEY_MULTI_COMBOBOX,
					KEY_MULTI_LIST,
					KEY_FILLIN_MULTI
			};
		}
	}
}
