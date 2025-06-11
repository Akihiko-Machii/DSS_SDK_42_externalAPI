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

    log.info("=== \u30d7\u30ed\u30ad\u30b7\u8a2d\u5b9a\u78ba\u8a8d ===");
    log.info("http.proxyHost: " + System.getProperty("http.proxyHost", "\u672a\u8a2d\u5b9a"));
    log.info("http.proxyPort: " + System.getProperty("http.proxyPort", "\u672a\u8a2d\u5b9a"));
    log.info("https.proxyHost: " + System.getProperty("https.proxyHost", "\u672a\u8a2d\u5b9a"));
    log.info("https.proxyPort: " + System.getProperty("https.proxyPort", "\u672a\u8a2d\u5b9a"));

    log.info("=== PokeAPI \u30c7\u30d0\u30c3\u30b0\u958b\u59cb ===");
    log.info("\u8a2d\u5b9aURL: [" + url + "]");
    log.info("\u8a2d\u5b9a\u30bf\u30a4\u30e0\u30a2\u30a6\u30c8: " + timeout + "ms");

    HttpURLConnection connection = null;
    try {
      log.info("URL\u4f5c\u6210\u958b\u59cb...");
      URL apiUrl = new URL(url);
      log.info("URL\u4f5c\u6210\u6210\u529f: " + apiUrl.toString());

      log.info("\u30b3\u30cd\u30af\u30b7\u30e7\u30f3\u4f5c\u6210\u958b\u59cb...");
      connection = (HttpURLConnection) apiUrl.openConnection();
      log.info("\u30b3\u30cd\u30af\u30b7\u30e7\u30f3\u4f5c\u6210\u6210\u529f");

      connection.setRequestMethod("GET");
      connection.setConnectTimeout(timeout);
      connection.setReadTimeout(timeout);
      connection.setRequestProperty("Accept", "application/json");

      log.info("\u63a5\u7d9a\u958b\u59cb...");
      connection.connect();
      log.info("\u63a5\u7d9a\u6210\u529f\uff01");

      int responseCode = connection.getResponseCode();
      log.info("\u30ec\u30b9\u30dd\u30f3\u30b9\u30b3\u30fc\u30c9\u53d6\u5f97: " + responseCode);

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
