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
        url.setLabel("\u30ea\u30af\u30a8\u30b9\u30c8URL");
        url.setRequired(true);
        url.setDescription("HTTP\u30ea\u30af\u30a8\u30b9\u30c8\u3092\u9001\u308b\u5148\u306eURL");
        return new SimpleParameter(KEY_URL, url);
    }

    private SimpleParameter createTimeoutParam() {
        NumberFillin number = new NumberFillin();
        number.setLabel("\u30bf\u30a4\u30e0\u30a2\u30a6\u30c8(ms)");
        number.setRequired(true);
        number.setInitialValue("30000"); // \u30c7\u30d5\u30a9\u30eb\u30c830\u79d2
        number.setDescription("\u30ea\u30af\u30a8\u30b9\u30c8\u306e\u30bf\u30a4\u30e0\u30a2\u30a6\u30c8\u6642\u9593\uff08\u30df\u30ea\u79d2\uff09");
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
