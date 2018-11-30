package org.leanpoker.player;

import com.google.gson.JsonElement;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class Player {

    static final String VERSION = "Pokerface Java player";
    private static final String FAKE_CARDS = "{\n" + "  \"cards\": [\n" + "    {\n" + "      \"rank\": \"5\",\n" + "      \"suit\": \"diamonds\"\n" + "    },\n" + "    {\n" + "      \"rank\": \"6\",\n" + "      \"suit\": \"diamonds\"\n" + "    },\n" + "    {\n" + "      \"rank\": \"7\",\n" + "      \"suit\": \"diamonds\"\n" + "    },\n" + "    {\n" + "      \"rank\": \"7\",\n"
        + "      \"suit\": \"spades\"\n" + "    },\n" + "    {\n" + "      \"rank\": \"8\",\n" + "      \"suit\": \"diamonds\"\n" + "    },\n" + "    {\n" + "      \"rank\": \"9\",\n" + "      \"suit\": \"diamonds\"\n" + "    }\n" + "  ]\n" + "}";

    public static int betRequest(JsonElement request) {
        int smallblind = request.getAsJsonObject().get("small_blind").getAsInt();
        System.out.println("Smallblind is: " + smallblind);
        tryRankingService();
        return 2 * smallblind;
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
            connection.setRequestProperty("Content-Type", "application/json");

            connection.setRequestProperty("Content-Length", Integer.toString(urlParameters.getBytes().length));
            connection.setRequestProperty("Content-Language", "en-US");

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
}
