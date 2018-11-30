package org.leanpoker.player;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class RankingService {
  List<Card> cards = new ArrayList<>();
  public RankingService init(List<Card> cards) {
    this.cards = cards;
    return this;
  }

  public RankingService callRankingService() {
    String param = buildRequestParam();
    System.out.println("Ranking-Request: " + param);
    try {
      System.out.println("Ranking: " + executePost("http://rainman.leanpoker.org/rank", param));
    } catch (Throwable t) {
      t.printStackTrace();
    }
    return this;
  }

  public static String executePost(String targetURL, String urlParameters) {
    HttpURLConnection connection = null;

    try {
      //Create connection
      URL url = new URL(targetURL);
      connection = (HttpURLConnection) url.openConnection();
      connection.setRequestMethod("POST");

      connection.setRequestProperty("Content-Length", Integer.toString(urlParameters.getBytes().length));

      connection.setUseCaches(false);
      connection.setDoOutput(true);

      //Send request
      DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
      wr.writeBytes(urlParameters);
      wr.close();

      //Get Response
      InputStream is = connection.getInputStream();
      BufferedReader rd = new BufferedReader(new InputStreamReader(is));
      StringBuilder response = new StringBuilder(); // or StringBuffer if Java version 5+
      String line;
      while ((line = rd.readLine()) != null) {
        response.append(line);
        response.append('\r');
      }
      rd.close();
      return response.toString();
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    } finally {
      if (connection != null) {
        connection.disconnect();
      }
    }
  }

  public String buildRequestParam() {
    String ret = "cards=[";
    boolean first =true;
    for (Card card: cards) {
      ret += card.toRanking();
      if (!first) {
        ret += ",";
      }
      first = false;
    }
    return ret + "]";
  }
}
