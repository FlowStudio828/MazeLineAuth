package top.mazeline.auth;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class ApiClient {
    private final JavaPlugin plugin;
    private final Gson gson = new Gson();

    public ApiClient(JavaPlugin plugin) { this.plugin = plugin; }

    public JsonObject post(String action, Map<String, Object> data) throws IOException {
        URL url = new URL(Constants.API_URL + "?action=" + action);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        conn.setConnectTimeout(10000);
        conn.setReadTimeout(15000);
        String body = gson.toJson(data);
        try (OutputStream os = conn.getOutputStream()) {
            os.write(body.getBytes(StandardCharsets.UTF_8));
        }
        int code = conn.getResponseCode();
        InputStream is = (code >= 200 && code < 300) ? conn.getInputStream() : conn.getErrorStream();
        if (is == null) return null;
        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) sb.append(line);
        }
        String text = sb.toString().trim();
        if (text.isEmpty()) return null;
        try {
            return gson.fromJson(text, JsonObject.class);
        } catch (Exception e) {
            plugin.getLogger().warning("API 返回非 JSON: " + text);
            return null;
        }
    }
}