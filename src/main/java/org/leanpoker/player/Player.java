package org.leanpoker.player;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Iterator;

public class Player {

  static final String VERSION = "Pokerface Java player 11";
  private static final String FAKE_CARDS =
      "cards=[\n" + "    {\"rank\":\"5\",\"suit\":\"diamonds\"},\n" + "    {\"rank\":\"6\",\"suit\":\"diamonds\"},\n" + "    {\"rank\":\"7\",\"suit\":\"diamonds\"},\n" + "    {\"rank\":\"7\",\"suit\":\"spades\"},\n" + "    {\"rank\":\"8\",\"suit\":\"diamonds\"},\n" + "    {\"rank\":\"9\",\"suit\":\"diamonds\"}\n" + "]";

  public static int betRequest(JsonElement request) {
    int smallblind = request.getAsJsonObject().get("small_blind").getAsInt();
    if (isBadCard(request)) {
      return 0;
    }
    int minimumBetAmount = getNextAmoutSmart(request, smallblind);
    return minimumBetAmount != -1 ? minimumBetAmount : 20 * smallblind;
  }

  private static boolean isBadCard(JsonElement request) {
    try {
      PlayerData me = getPlayer(request);
      boolean result = me.getCards().get(0).getIntValue() < 10 && me.getCards().get(1).getIntValue() < 10;
      if(result) {
        System.out.println("Folding bad cards: "  + me.getCards());
      } else {
        System.out.println("Keeping cards: "  + me.getCards());
      }
      return result;
    } catch (Throwable t) {
      System.err.println("Exception in getNextAmoutSmart");
      t.printStackTrace();
      return true;
    }
  }

  private static int getNextAmoutSmart(JsonElement request, int smallblind) {
    try {
      int minimum_raise = request.getAsJsonObject().get("minimum_raise").getAsInt();
      int current_buy_in = request.getAsJsonObject().get("current_buy_in").getAsInt();
      PlayerData me = getPlayer(request);
      System.out.println("Player: " + me.toString());
      System.out.println("minimum_raise: " + minimum_raise);
      System.out.println("current_buy_in: " + current_buy_in);

      int currentBet = me.getCurrentBet(); //players[in_action][bet]

      System.out.println("currentBet: " + currentBet);

      System.out.println("Smallblind is: " + smallblind);
      return current_buy_in - currentBet + minimum_raise;
    } catch (Throwable t) {
      System.err.println("Exception in getNextAmoutSmart");
      t.printStackTrace();
      return -1;
    }
  }

  private static PlayerData getPlayer(JsonElement request) {
    JsonArray players = request.getAsJsonObject().get("players").getAsJsonArray();
    int in_action = request.getAsJsonObject().get("in_action").getAsInt();
    System.out.println("in_action: " + in_action);
    JsonObject me = null;
    Iterator<JsonElement> it = players.iterator();
    while (it.hasNext()) {
      JsonElement player = it.next();
      if (player.getAsJsonObject().get("id").getAsInt() == in_action) {
        me = player.getAsJsonObject();
      }
    }
    return new PlayerData(me);
  }

  private static void tryRankingService() {
    try {
      System.out.println("Ranking: " + executePost("http://rainman.leanpoker.org/rank", FAKE_CARDS));
    } catch (Throwable t) {
      t.printStackTrace();
    }
  }

  public static void showdown(JsonElement game) {
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

  public static void main(String[] args) {
    tryRankingService();
  }
}
