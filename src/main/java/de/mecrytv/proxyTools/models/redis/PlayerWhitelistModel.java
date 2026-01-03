package de.mecrytv.proxyTools.models.redis;

import com.google.gson.JsonObject;
import de.mecrytv.proxyTools.models.ICacheModel;
import java.sql.Timestamp;

public class PlayerWhitelistModel implements ICacheModel {

    private String uuid;
    private String username;
    private Timestamp whitelistedAt;

    public PlayerWhitelistModel() {}

    public PlayerWhitelistModel(String uuid, String username, Timestamp whitelistedAt) {
        this.uuid = uuid;
        this.username = username;
        this.whitelistedAt = whitelistedAt;
    }

    @Override
    public String getIdentifier() { return uuid; }

    @Override
    public JsonObject serialize() {
        JsonObject json = new JsonObject();
        json.addProperty("uuid", uuid);
        json.addProperty("username", username);
        if (whitelistedAt != null) {
            json.addProperty("whitelistedAt", whitelistedAt.getTime());
        } else {
            json.addProperty("whitelistedAt", System.currentTimeMillis());
        }
        return json;
    }

    @Override
    public void deserialize(JsonObject json) {
        this.uuid = json.get("uuid").getAsString();
        if (json.has("username")) {
            this.username = json.get("username").getAsString();
        }
        this.whitelistedAt = new Timestamp(json.get("whitelistedAt").getAsLong());
    }

    public Timestamp getWhitelistedAt() { return whitelistedAt; }
    public void setWhitelistedAt(Timestamp whitelistedAt) { this.whitelistedAt = whitelistedAt; }
    public String getName() { return username; }
    public void setName(String username) { this.username = username; }
}