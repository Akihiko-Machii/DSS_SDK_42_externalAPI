package com.appresso.ds.dp.modules.adapter.pokeapi;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import com.appresso.ds.common.fw.LoggingContext;
import com.appresso.ds.dp.spi.Operation;
import com.appresso.ds.dp.spi.OperationContext;
import com.appresso.ds.dp.spi.OperationConfiguration;

public class PokeAPIOperation implements Operation {
  public final OperationContext context;
  public final OperationConfiguration conf;

  static final String KEY_URL = "URL";
  static final String KEY_TIMEOUT = "TIMEOUT";

  public PokeAPIOperation(OperationConfiguration conf, OperationContext context) {
    this.conf = conf;
    this.context = context;
  }

  @Override
  public Map execute(Map inputData) throws Exception {
    String url = conf.getValue(KEY_URL).toString();
    Integer timeout = Integer.parseInt(conf.getValue(KEY_TIMEOUT).toString());
    LoggingContext log = context.log();
    HttpURLConnection connection = null;
    try {
      URL apiUrl = new URL(url);
      connection = (HttpURLConnection) apiUrl.openConnection();
      connection.setRequestMethod("GET");
      connection.setConnectTimeout(timeout);
      connection.setReadTimeout(timeout);
      connection.setRequestProperty("Accept", "application/json");
      connection.connect();
      int responseCode = connection.getResponseCode();
      InputStream inputStream = (responseCode < 400) ? connection.getInputStream() : connection.getErrorStream();
      String responseBody = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))
          .lines().collect(Collectors.joining("\n"));
      log.info("HTTP GET\u5b9f\u884c:" + url + "\u30b9\u30c6\u30fc\u30bf\u30b9: " + responseCode);

      Map<String, Object> result = new HashMap<>();
      result.put("status", responseCode);
      result.put("body", responseBody);
      return result;
    } catch (Exception e) {
      log.error("HTTP GET\u30ea\u30af\u30a8\u30b9\u30c8\u4e2d\u306b\u30a8\u30e9\u30fc\u304c\u767a\u751f\u3057\u307e\u3057\u305f: " + e.getMessage(), e);
      throw new RuntimeException("HTTP GET\u30ea\u30af\u30a8\u30b9\u30c8\u4e2d\u306b\u30a8\u30e9\u30fc\u304c\u767a\u751f\u3057\u307e\u3057\u305f", e);
    } finally {
      if (connection != null) {
        connection.disconnect();
      }
    }
  }

  @Override
  public void destroy() {
  }
}
