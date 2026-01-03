package de.mecrytv.proxyTools.models.redis;

import com.google.gson.JsonObject;
import de.mecrytv.proxyTools.models.ICacheModel;

public class AntiBotModel implements ICacheModel {

    private String ip;
    private long verifiedAt;

    public AntiBotModel() {}

    public AntiBotModel(String ip, long verifiedAt) {
        this.ip = ip;
        this.verifiedAt = verifiedAt;
    }

    @Override
    public String getIdentifier() { return ip; }

    @Override
    public JsonObject serialize() {
        JsonObject json = new JsonObject();
        json.addProperty("ip", ip);
        json.addProperty("verifiedAt", verifiedAt);
        return json;
    }

    @Override
    public void deserialize(JsonObject json) {
        this.ip = json.get("ip").getAsString();
        this.verifiedAt = json.get("verifiedAt").getAsLong();
    }

    public long getVerifiedAt() { return verifiedAt; }
}