package com.appresso.ds.dp.modules.adapter.pokeapi;

import com.appresso.ds.dp.spi.GetDataOperationFactory;
import com.appresso.ds.dp.spi.OperationConfiguration;
import com.appresso.ds.dp.spi.OperationConfigurator;
import com.appresso.ds.dp.spi.OperationContext;
import com.appresso.ds.dp.spi.OperationIO;
import com.appresso.ds.common.spi.constraint.Fillin;
import com.appresso.ds.common.spi.constraint.NumberFillin;
import com.appresso.ds.common.spi.param.SimpleParameter;
import com.appresso.ds.common.spi.constraint.StringOutput;

public class PokeAPIOperationFactory extends GetDataOperationFactory {
    static final String KEY_URL = "URL";
    static final String KEY_TIMEOUT = "TIMEOUT";
    static final String KEY_JSON_OUTPUT = "JSON_OUTPUT";

    private SimpleParameter createUrlParam() {
        Fillin url = new Fillin();
        url.setLabel("リクエストURL");
        url.setRequired(true);
        url.setDescription("HTTPリクエストを送る先のURL");
        return new SimpleParameter(KEY_URL, url);
    }

    private SimpleParameter createTimeoutParam() {
        NumberFillin number = new NumberFillin();
        number.setLabel("タイムアウト(ms)");
        number.setRequired(true);
        number.setInitialValue("30000"); // デフォルト30秒
        number.setDescription("リクエストのタイムアウト時間（ミリ秒）");
        return new SimpleParameter(KEY_TIMEOUT, number);
    }

    @Override
    public OperationConfigurator createOperationConfigurator(OperationConfiguration conf, OperationContext context)
            throws Exception {
        OperationConfigurator configurator = new OperationConfigurator(conf, context);

        configurator.addSimpleParameter(createUrlParam());
        configurator.addSimpleParameter(createTimeoutParam());

        return configurator;
    }

    @Override
    public OperationIO createOperationIO(OperationConfiguration conf, OperationContext context) throws Exception {
        OperationIO io = new OperationIO();

        StringOutput jsonOutput = new StringOutput(KEY_JSON_OUTPUT);
        io.addOutput(jsonOutput);

        return io;
    }

    @Override
    public PokeAPIOperation createOperation(OperationConfiguration conf, OperationContext context) throws Exception {
        return new PokeAPIOperation(conf, context);
    }
}
