package com.plantpal.app;

import com.plantpal.app.data.SpeciesCache;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

// Talks to the Perenual API. MUST be called from a background thread.
public class PlantApi {

    public static List<SpeciesCache> search(String query) throws IOException, JSONException {
        if (Constants.PLANT_API_KEY.startsWith("PASTE")) {
            throw new IOException("API key not set in Constants.java");
        }

        String urlString = Constants.PLANT_API_URL
                + "?key=" + URLEncoder.encode(Constants.PLANT_API_KEY, "UTF-8")
                + "&q=" + URLEncoder.encode(query, "UTF-8");

        HttpURLConnection conn = (HttpURLConnection) new URL(urlString).openConnection();
        try {
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);

            int code = conn.getResponseCode();
            if (code != HttpURLConnection.HTTP_OK) {
                throw new IOException("HTTP error " + code);
            }

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), "UTF-8"));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            reader.close();

            return parse(sb.toString());
        } finally {
            conn.disconnect();
        }
    }

    // Turns the JSON text into a list of SpeciesCache objects
    private static List<SpeciesCache> parse(String json) throws JSONException {
        List<SpeciesCache> result = new ArrayList<>();

        JSONObject root = new JSONObject(json);
        JSONArray data = root.optJSONArray("data");
        if (data == null) return result;

        for (int i = 0; i < data.length(); i++) {
            JSONObject item = data.optJSONObject(i);
            if (item == null) continue;

            int id = item.optInt("id", -1);
            String name = clean(item.optString("common_name", ""));
            if (id <= 0 || name.isEmpty()) continue;

            SpeciesCache s = new SpeciesCache();
            s.apiId = id;
            s.commonName = capitalize(name);
            s.scientificName = joinValues(item.opt("scientific_name"));
            s.watering = normalizeWatering(item.optString("watering", ""));
            s.sunlight = joinValues(item.opt("sunlight"));
            result.add(s);
        }
        return result;
    }

    // Android's optString returns the text "null" for JSON null, so we filter that out
    private static String clean(String s) {
        if (s == null) return "";
        s = s.trim();
        return s.equalsIgnoreCase("null") ? "" : s;
    }

    private static String capitalize(String s) {
        if (s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    // The API gives some fields as an array (["full sun","part shade"]) and some as text.
    private static String joinValues(Object value) {
        String text = "";
        if (value instanceof JSONArray) {
            JSONArray arr = (JSONArray) value;
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < arr.length(); i++) {
                String part = clean(arr.optString(i, ""));
                if (part.isEmpty()) continue;
                if (sb.length() > 0) sb.append(", ");
                sb.append(part);
            }
            text = sb.toString();
        } else if (value instanceof String) {
            text = clean((String) value);
        }
        return text.isEmpty() ? "Unknown" : capitalize(text);
    }

    // Only three values are meaningful to us. Free-plan accounts can get an
    // "Upgrade plans..." message instead of the real value, so anything else becomes "Average".
    private static String normalizeWatering(String raw) {
        String w = raw == null ? "" : raw.toLowerCase();
        if (w.contains("frequent")) return "Frequent";
        if (w.contains("minimum")) return "Minimum";
        return "Average";
    }
}