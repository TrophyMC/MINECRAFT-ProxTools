package de.mecrytv.proxyTools.models.redis;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import de.mecrytv.proxyTools.models.ICacheModel;

import java.util.ArrayList;
import java.util.List;

public class AltAccountModel implements ICacheModel {

    private String ip;
    private List<String> knownUuids = new ArrayList<>();
    private static final Gson GSON = new Gson();

    public AltAccountModel() {}

    public AltAccountModel(String ip, List<String> knownUuids) {
        this.ip = ip;
        this.knownUuids = knownUuids;
    }

    @Override
    public String getIdentifier() { return ip; }

    @Override
    public JsonObject serialize() {
        JsonObject json = new JsonObject();
        json.addProperty("ip", ip);
        json.addProperty("uuids", GSON.toJson(knownUuids));
        return json;
    }

    @Override
    public void deserialize(JsonObject json) {
        this.ip = json.get("ip").getAsString();
        this.knownUuids = GSON.fromJson(json.get("uuids").getAsString(),
                new TypeToken<List<String>>(){}.getType());
    }

    public List<String> getKnownUuids() { return knownUuids; }

    public void addUuid(String uuid) {
        if (!knownUuids.contains(uuid)) {
            knownUuids.add(uuid);
        }
    }
}