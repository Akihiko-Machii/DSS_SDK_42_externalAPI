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

  // JSON文字列から値を抽出するヘルパーメソッド
  private String extractJsonValue(String json, String key) {
    String searchKey = "\"" + key + "\":";
    int startIndex = json.indexOf(searchKey);
    if (startIndex == -1)
      return "";

    startIndex += searchKey.length();
    // 値の開始位置を見つける（空白やタブをスキップ）
    while (startIndex < json.length() && Character.isWhitespace(json.charAt(startIndex))) {
      startIndex++;
    }

    int endIndex;
    if (json.charAt(startIndex) == '"') {
      // 文字列値の場合
      startIndex++; // 開始の"をスキップ
      endIndex = json.indexOf('"', startIndex);
    } else {
      // 数値の場合
      endIndex = startIndex;
      while (endIndex < json.length() &&
          (Character.isDigit(json.charAt(endIndex)) || json.charAt(endIndex) == '.')) {
        endIndex++;
      }
    }

    return endIndex > startIndex ? json.substring(startIndex, endIndex) : "";
  }

  // XML形式に変換
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

  // CSVファイル出力
  private void outputToCsv(String xmlData, LoggingContext log) {
    try {
      // ファイル名は現在時刻入りで一意にする
      String fileName = "pokemon_data_" + System.currentTimeMillis() + ".csv";
      java.io.File file = new java.io.File(fileName);
      java.io.FileOutputStream fos = new java.io.FileOutputStream(fileName);
      java.io.OutputStreamWriter writer = new java.io.OutputStreamWriter(fos, StandardCharsets.UTF_8);

      // CSVヘッダー
      writer.write("name,id,height,weight\n");

      // XMLから値を抽出してCSV行として出力
      String name = extractXmlValue(xmlData, "name");
      String id = extractXmlValue(xmlData, "id");
      String height = extractXmlValue(xmlData, "height");
      String weight = extractXmlValue(xmlData, "weight");

      writer.write(String.format("%s,%s,%s,%s\n", name, id, height, weight));
      writer.close();

      // 絶対パスをログに出力
      log.info("CSVファイル出力完了: " + file.getAbsolutePath());
    } catch (Exception e) {
      log.error("CSVファイル出力エラー: " + e.getMessage(), e);
    }
  }

  // XMLから値を抽出
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

    log.info("=== プロキシ設定確認 ===");
    log.info("http.proxyHost: " + System.getProperty("http.proxyHost", "未設定"));
    log.info("http.proxyPort: " + System.getProperty("http.proxyPort", "未設定"));
    log.info("https.proxyHost: " + System.getProperty("https.proxyHost", "未設定"));
    log.info("https.proxyPort: " + System.getProperty("https.proxyPort", "未設定"));

    log.info("=== PokeAPI デバッグ開始 ===");
    log.info("設定URL: [" + url + "]");
    log.info("設定タイムアウト: " + timeout + "ms");

    HttpURLConnection connection = null;
    try {
      log.info("URL作成開始...");
      URL apiUrl = new URL(url);
      log.info("URL作成成功: " + apiUrl.toString());

      log.info("コネクション作成開始...");
      connection = (HttpURLConnection) apiUrl.openConnection();
      log.info("コネクション作成成功");

      connection.setRequestMethod("GET");
      connection.setConnectTimeout(timeout);
      connection.setReadTimeout(timeout);
      connection.setRequestProperty("Accept", "application/json");

      log.info("接続開始...");
      connection.connect();
      log.info("接続成功！");

      int responseCode = connection.getResponseCode();
      log.info("レスポンスコード取得: " + responseCode);

      InputStream inputStream = (responseCode < 400) ? connection.getInputStream() : connection.getErrorStream();
      String responseBody = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))
          .lines().collect(Collectors.joining("\n"));
      log.info("HTTP GET実行:" + url + "ステータス: " + responseCode);

      // JSON文字列から必要項目を抽出（文字列操作）
      String name = extractJsonValue(responseBody, "name");
      String id = extractJsonValue(responseBody, "id");
      String height = extractJsonValue(responseBody, "height");
      String weight = extractJsonValue(responseBody, "weight");

      // XML形式に変換
      String xmlData = convertToXml(name, id, height, weight);

      // XMLデータの確認ログを追加
      log.info("=== XML出力データ確認 ===");
      log.info("生成XMLデータ:\n" + xmlData);
      log.info("XMLデータサイズ: " + xmlData.length() + " bytes");
      log.info("出力キー: " + PokeAPIOperationFactory.KEY_JSON_OUTPUT);
      log.info("========================");

      // CSVファイルに出力
      outputToCsv(xmlData, log);

      Map<String, Object> result = new HashMap<>();
      result.put(PokeAPIOperationFactory.KEY_JSON_OUTPUT, xmlData);

      // 戻り値確認ログも追加
      log.info("=== 戻り値確認 ===");
      log.info("result.size(): " + result.size());
      log.info("結果Mapキー一覧: " + result.keySet());
      log.info("================");

      return result;
    } catch (Exception e) {
      log.error("HTTP GETリクエスト中にエラーが発生しました: " + e.getMessage(), e);
      throw new RuntimeException("HTTP GETリクエスト中にエラーが発生しました", e);
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
