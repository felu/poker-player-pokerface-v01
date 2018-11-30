package org.leanpoker.player;

import com.google.gson.JsonObject;

public class Card {
  private final JsonObject json;

  public Card(JsonObject json) {
    this.json = json;
  }

  public int getIntValue() {
    String value = json.get("rank").getAsString();
    if (value.equals("J")) {
      return 11;
    } else if (value.equals("Q")) {
      return 12;
    } else if (value.equals("K")) {
      return 13;
    }else if (value.equals("A")) {
      return 14;
    } else {
      return Integer.parseInt(value);
    }
  }

  @Override public String toString() {
    return json.toString();
  }

  public String toRanking() {
    //{"rank":"5","suit":"diamonds"},
    return "{\"rank\":\"" + getRank() + "\",\"suit\":\"" + getSuite() + "\"}";
  }

  public String getRank() {
    return json.get("rank").getAsString();
  }

  public String getSuite() {
    return json.get("suit").getAsString();
  }
}
