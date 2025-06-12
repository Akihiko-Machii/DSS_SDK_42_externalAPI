package com.appresso.ds.dp.modules.adapter.pokeapi;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import com.appresso.ds.common.fw.LoggingContext;
import com.appresso.ds.dp.modules.adapter.pokeapi.PokeAPIOperationFactory;
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

  // JSON\u6587\u5b57\u5217\u304b\u3089\u5024\u3092\u62bd\u51fa\u3059\u308b\u30d8\u30eb\u30d1\u30fc\u30e1\u30bd\u30c3\u30c9
  private String extractJsonValue(String json, String key) {
    String searchKey = "\"" + key + "\":";
    int startIndex = json.indexOf(searchKey);
    if (startIndex == -1)
      return "";

    startIndex += searchKey.length();
    // \u5024\u306e\u958b\u59cb\u4f4d\u7f6e\u3092\u898b\u3064\u3051\u308b\uff08\u7a7a\u767d\u3084\u30bf\u30d6\u3092\u30b9\u30ad\u30c3\u30d7\uff09
    while (startIndex < json.length() && Character.isWhitespace(json.charAt(startIndex))) {
      startIndex++;
    }

    int endIndex;
    if (json.charAt(startIndex) == '"') {
      // \u6587\u5b57\u5217\u5024\u306e\u5834\u5408
      startIndex++; // \u958b\u59cb\u306e"\u3092\u30b9\u30ad\u30c3\u30d7
      endIndex = json.indexOf('"', startIndex);
    } else {
      // \u6570\u5024\u306e\u5834\u5408
      endIndex = startIndex;
      while (endIndex < json.length() &&
          (Character.isDigit(json.charAt(endIndex)) || json.charAt(endIndex) == '.')) {
        endIndex++;
      }
    }

    return endIndex > startIndex ? json.substring(startIndex, endIndex) : "";
  }

  // XML\u5f62\u5f0f\u306b\u5909\u63db
  private String convertToXml(String name, String id, String height, String weight) {
    StringBuilder xml = new StringBuilder();
    xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
    xml.append("<pokemon>\n");
    xml.append("  <name>").append(name).append("</name>\n");
    xml.append("  <id>").append(id).append("</id>\n");
    xml.append("  <height>").append(height).append("</height>\n");
    xml.append("  <weight>").append(weight).append("</weight>\n");
    xml.append("</pokemon>");
    return xml.toString();
  }

  // CSV\u30d5\u30a1\u30a4\u30eb\u51fa\u529b
  private void outputToCsv(String xmlData, LoggingContext log) {
    try {
      // \u30d5\u30a1\u30a4\u30eb\u540d\u306f\u73fe\u5728\u6642\u523b\u5165\u308a\u3067\u4e00\u610f\u306b\u3059\u308b
      String fileName = "pokemon_data_" + System.currentTimeMillis() + ".csv";
      java.io.File file = new java.io.File(fileName);
      java.io.FileOutputStream fos = new java.io.FileOutputStream(fileName);
      java.io.OutputStreamWriter writer = new java.io.OutputStreamWriter(fos, StandardCharsets.UTF_8);

      // CSV\u30d8\u30c3\u30c0\u30fc
      writer.write("name,id,height,weight\n");

      // XML\u304b\u3089\u5024\u3092\u62bd\u51fa\u3057\u3066CSV\u884c\u3068\u3057\u3066\u51fa\u529b
      String name = extractXmlValue(xmlData, "name");
      String id = extractXmlValue(xmlData, "id");
      String height = extractXmlValue(xmlData, "height");
      String weight = extractXmlValue(xmlData, "weight");

      writer.write(String.format("%s,%s,%s,%s\n", name, id, height, weight));
      writer.close();

      // \u7d76\u5bfe\u30d1\u30b9\u3092\u30ed\u30b0\u306b\u51fa\u529b
      log.info("CSV\u30d5\u30a1\u30a4\u30eb\u51fa\u529b\u5b8c\u4e86: " + file.getAbsolutePath());
    } catch (Exception e) {
      log.error("CSV\u30d5\u30a1\u30a4\u30eb\u51fa\u529b\u30a8\u30e9\u30fc: " + e.getMessage(), e);
    }
  }

  // XML\u304b\u3089\u5024\u3092\u62bd\u51fa
  private String extractXmlValue(String xml, String tagName) {
    String startTag = "<" + tagName + ">";
    String endTag = "</" + tagName + ">";

    int startIndex = xml.indexOf(startTag);
    if (startIndex == -1)
      return "";

    startIndex += startTag.length();
    int endIndex = xml.indexOf(endTag, startIndex);

    return endIndex > startIndex ? xml.substring(startIndex, endIndex) : "";
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

      // JSON\u6587\u5b57\u5217\u304b\u3089\u5fc5\u8981\u9805\u76ee\u3092\u62bd\u51fa\uff08\u6587\u5b57\u5217\u64cd\u4f5c\uff09
      String name = extractJsonValue(responseBody, "name");
      String id = extractJsonValue(responseBody, "id");
      String height = extractJsonValue(responseBody, "height");
      String weight = extractJsonValue(responseBody, "weight");

      // XML\u5f62\u5f0f\u306b\u5909\u63db
      String xmlData = convertToXml(name, id, height, weight);

      // CSV\u30d5\u30a1\u30a4\u30eb\u306b\u51fa\u529b
      outputToCsv(xmlData, log);

      Map<String, Object> result = new HashMap<>();
      result.put(PokeAPIOperationFactory.KEY_JSON_OUTPUT, xmlData);
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
