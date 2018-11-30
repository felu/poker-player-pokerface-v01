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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Player {

  static final String VERSION = "Pokerface Java player 19";
  private static final String FAKE_CARDS =
      "cards=[\n" + "    {\"rank\":\"5\",\"suit\":\"diamonds\"},\n" + "    {\"rank\":\"6\",\"suit\":\"diamonds\"},\n" + "    {\"rank\":\"7\",\"suit\":\"diamonds\"},\n" + "    {\"rank\":\"7\",\"suit\":\"spades\"},\n" + "    {\"rank\":\"8\",\"suit\":\"diamonds\"},\n" + "    {\"rank\":\"9\",\"suit\":\"diamonds\"}\n" + "]";

  public static int betRequest(JsonElement request) {
    int smallblind = request.getAsJsonObject().get("small_blind").getAsInt();
    if (areBadCards(request)) {
      return 0;
    }
    int rank = new RankingService().init(getAllCards(request)).callRankingService().getRank();
    int minimumBetAmount = getMinimumRaiseAmount(request);
    if(rank >= 0 && minimumBetAmount != -1) {
      if(rank == 0) {
        return 0;
      }
      return getCallAmount(request) + 2 * smallblind * rank;
    }
    if (minimumBetAmount == -1) {
      return 20 * smallblind;
    }
    if (areGoodCards(request)) {
      return 5 * minimumBetAmount;
    }
    return getCallAmount(request);
  }

  private static List<Card> getAllCards(JsonElement request) {
    List<Card> handCards = getPlayer(request).getCards();
    handCards.addAll(getBoardCards(request));
    return handCards;
  }

  private static List<Card> getBoardCards(JsonElement request) {
    JsonArray community_cards = request.getAsJsonObject().get("community_cards").getAsJsonArray();
    List<Card> result = new ArrayList<>();
    Iterator<JsonElement> it = community_cards.iterator();
    while (it.hasNext()) {
      JsonElement cardJson = it.next();
      result.add(new Card(cardJson.getAsJsonObject()));
    }
    return result;
  }

  private static boolean areGoodCards(JsonElement request) {
    try {
      PlayerData me = getPlayer(request);
      boolean result = me.getCards().get(0).getIntValue() > 10 && me.getCards().get(1).getIntValue() > 10;
      if (result) {
        System.out.println("Having good cards bad cards: " + me.getCards());
      } else {
        System.out.println("Avarage cards: " + me.getCards());
      }
      return result;
    } catch (Throwable t) {
      System.err.println("Exception in getMinimumRaiseAmount");
      t.printStackTrace();
      return true;
    }
  }

  private static boolean areBadCards(JsonElement request) {
    try {
      PlayerData me = getPlayer(request);
      if (me.getCards().get(0).getIntValue() == me.getCards().get(1).getIntValue()) {
        return false;
      }
      return me.getCards().get(0).getIntValue() < 10 && me.getCards().get(1).getIntValue() < 10;
    } catch (Throwable t) {
      System.err.println("Exception in getMinimumRaiseAmount");
      t.printStackTrace();
      return true;
    }
  }

  private static int getMinimumRaiseAmount(JsonElement request) {
    try {
      int callAmount = getCallAmount(request);
      int minimum_raise = request.getAsJsonObject().get("minimum_raise").getAsInt();
      return callAmount + minimum_raise;
    } catch (Throwable t) {
      System.err.println("Exception in getMinimumRaiseAmount");
      t.printStackTrace();
      return -1;
    }
  }

  private static int getCallAmount(JsonElement request) {
    try {
      int current_buy_in = request.getAsJsonObject().get("current_buy_in").getAsInt();
      PlayerData me = getPlayer(request);

      int currentBet = me.getCurrentBet(); //players[in_action][bet]

      return current_buy_in - currentBet;
    } catch (Throwable t) {
      System.err.println("Exception in getMinimumRaiseAmount");
      t.printStackTrace();
      return -1;
    }
  }

  private static PlayerData getPlayer(JsonElement request) {
    JsonArray players = request.getAsJsonObject().get("players").getAsJsonArray();
    int in_action = request.getAsJsonObject().get("in_action").getAsInt();
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
