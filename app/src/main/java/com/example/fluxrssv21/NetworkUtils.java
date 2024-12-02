package com.example.fluxrssv21;

import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class NetworkUtils {

    public static String fetchAPIData(String urlString) {
        StringBuilder result = new StringBuilder();
        try {
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line);
            }
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result.toString();
    }

    public static String fetchSteamAppId(String gameName) {
        try {
            String searchUrl = "https://store.steampowered.com/api/storesearch/?term=" +
                    URLEncoder.encode(gameName, "UTF-8") + "&cc=US";
            String response = fetchAPIData(searchUrl);
            JSONObject jsonObject = new JSONObject(response);
            JSONArray items = jsonObject.getJSONArray("items");
            if (items.length() > 0) {
                return items.getJSONObject(0).getString("id");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String fetchPlainId(String gameName) {
        try {
            String urlString = "https://api.isthereanydeal.com/games/lookup/v1?key=e075dd8fa55116bfec5899906e8a5c8f319ef828&title=" + URLEncoder.encode(gameName, "UTF-8");
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Content-Type", "application/json");
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
                JSONObject jsonObject = new JSONObject(response.toString());
                if (jsonObject.getBoolean("found")) {
                    JSONObject gameObject = jsonObject.getJSONObject("game");
                    return gameObject.getString("id");
                }
            } else {
                System.err.println("Error: Unable to fetch plain ID. Response code: " + responseCode);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Exception occurred while fetching plain ID for game: " + gameName);
        }
        return null;
    }

    public static String fetchGamePrice(String gameId) {
        try {
            String urlString = "https://api.isthereanydeal.com/games/overview/v2?key=e075dd8fa55116bfec5899906e8a5c8f319ef828";
            JSONArray gameIdArray = new JSONArray();
            gameIdArray.put(gameId);
            byte[] postData = gameIdArray.toString().getBytes(StandardCharsets.UTF_8);
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Content-Length", Integer.toString(postData.length));
            connection.setDoOutput(true);
            try (DataOutputStream writer = new DataOutputStream(connection.getOutputStream())) {
                writer.write(postData);
            }
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
                JSONObject jsonObject = new JSONObject(response.toString());
                JSONArray pricesArray = jsonObject.getJSONArray("prices");
                if (pricesArray.length() > 0) {
                    JSONObject gameObject = pricesArray.getJSONObject(0);
                    JSONObject currentPriceObject = gameObject.getJSONObject("current");
                    String shopName = currentPriceObject.getJSONObject("shop").getString("name");
                    double priceAmount = currentPriceObject.getJSONObject("price").getDouble("amount");
                    String currency = currentPriceObject.getJSONObject("price").getString("currency");
                    return String.format("Best current price: %.2f %s (%s)", priceAmount, currency, shopName);
                }
            } else {
                System.err.println("Failed to fetch game prices: Response code " + responseCode);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "No current price found.";
    }


    public static String parseSteamResponse(String response, String appId) {
        try {
            JSONObject jsonObject = new JSONObject(response);
            JSONObject gameData = jsonObject.getJSONObject(appId);
            if (gameData.getBoolean("success")) {
                return gameData.getJSONObject("data")
                        .getJSONObject("price_overview")
                        .getString("final_formatted");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "Price not found";
    }

    public static String fetchGameCover(String response, String appId) {
        try {
            JSONObject jsonObject = new JSONObject(response);
            JSONObject gameData = jsonObject.getJSONObject(appId);
            if (gameData.getBoolean("success")) {
                return gameData.getJSONObject("data").getString("header_image");

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String fetchGameDescription(String response, String appId) {
        try {
            JSONObject jsonObject = new JSONObject(response);
            JSONObject gameData = jsonObject.getJSONObject(appId);
            if (gameData.getBoolean("success")) {
                return gameData.getJSONObject("data").getString("short_description");

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
}