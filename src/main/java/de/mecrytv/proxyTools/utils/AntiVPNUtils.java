package de.mecrytv.proxyTools.utils;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.mecrytv.proxyTools.ProxyTools;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

public class AntiVPNUtils {

    private static final HttpClient httpClient = HttpClient.newHttpClient();

    public static CompletableFuture<Boolean> isProxy(String ip){
        String apiKey = ProxyTools.getInstance().getServiceManager().getConfig().getString("anti_vpn.api_key");
        String url = "https://proxycheck.io/v2/" + ip + "?key=" + apiKey + "&vpn=1";

        if (apiKey.isEmpty()) return CompletableFuture.completedFuture(false);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() == 200) {
                        JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();

                        if (json.has(ip)) {
                            JsonObject ipData = json.getAsJsonObject(ip);
                            return ipData.has("proxy") && ipData.get("proxy").getAsString().equals("yes");
                        }
                    }
                    return false;
                }).exceptionally(ex -> {
                    ex.printStackTrace();
                    return false;
                });
    }
}
