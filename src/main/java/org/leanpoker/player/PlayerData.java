package org.leanpoker.player;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class PlayerData {
  private final JsonObject json;

  public PlayerData(JsonObject playerJson) {
    this.json = playerJson;
  }

  public int getCurrentBet() {
    return json.get("bet").getAsInt();
  }

  public List<Card> getCards() {
    JsonArray jsonCards = json.get("hole_cards").getAsJsonArray();
    List<Card> result = new ArrayList<>();

    Iterator<JsonElement> it = jsonCards.iterator();
    while (it.hasNext()) {
      JsonElement cardJson = it.next();
      result.add(new Card(cardJson.getAsJsonObject()));
    }
    return result;
  }

  @Override public String toString() {
    return json.toString();
  }
}
