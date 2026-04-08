package me.jarton.pa2discord.discord;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class DiscordWebhookClient {

    private final Gson gson = new Gson();
    private final String webhookBaseUrl;

    public DiscordWebhookClient(String webhookUrl) {
        this.webhookBaseUrl = webhookUrl;
    }

    public String sendAndReturnMessageId(JsonObject payload) throws Exception {
        URL url = new URL(webhookBaseUrl + "?wait=true");
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "application/json");
        con.setDoOutput(true);

        byte[] out = payload.toString().getBytes(StandardCharsets.UTF_8);
        try (OutputStream os = con.getOutputStream()) {
            os.write(out);
        }

        int code = con.getResponseCode();
        byte[] bytes = (code >= 200 && code < 300)
                ? con.getInputStream().readAllBytes()
                : (con.getErrorStream() != null ? con.getErrorStream().readAllBytes() : new byte[0]);

        String body = new String(bytes, StandardCharsets.UTF_8);

        if (code < 200 || code >= 300) {
            throw new RuntimeException("Webhook send failed: HTTP " + code + " -> " + body);
        }

        JsonObject resp = gson.fromJson(body, JsonObject.class);
        return resp.get("id").getAsString();
    }

    public void editMessage(String messageId, JsonObject payload) throws Exception {
        URL url = new URL(webhookBaseUrl + "/messages/" + messageId);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("PATCH");
        con.setRequestProperty("Content-Type", "application/json");
        con.setDoOutput(true);

        byte[] out = payload.toString().getBytes(StandardCharsets.UTF_8);
        try (OutputStream os = con.getOutputStream()) {
            os.write(out);
        }

        int code = con.getResponseCode();
        if (code < 200 || code >= 300) {
            byte[] err = con.getErrorStream() != null ? con.getErrorStream().readAllBytes() : new byte[0];
            throw new RuntimeException("Webhook edit failed: HTTP " + code + " -> " + new String(err, StandardCharsets.UTF_8));
        }
    }
}
