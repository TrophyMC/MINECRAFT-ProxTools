package de.mecrytv.proxyTools.models.redis;

import com.google.gson.JsonObject;
import de.mecrytv.proxyTools.models.ICacheModel;

public class ServerMaintenanceModel implements ICacheModel {

    private boolean active;
    private String activatorUUID;

    public ServerMaintenanceModel() {}

    public ServerMaintenanceModel(boolean active, String activatorUUID) {
        this.active = active;
        this.activatorUUID = activatorUUID;
    }

    @Override
    public String getIdentifier() {
        return "server_maintenance";
    }

    @Override
    public JsonObject serialize() {
        JsonObject json = new JsonObject();
        json.addProperty("active", active);
        json.addProperty("activatorUUID", activatorUUID);
        return json;
    }

    @Override
    public void deserialize(JsonObject json) {
        this.active = json.get("active").getAsBoolean();
        this.activatorUUID = json.get("activatorUUID").getAsString();
    }

    public boolean isActive() {
        return active;
    }
    public void setActive(boolean active) {
        this.active = active;
    }
    public String getActivatorUUID() {
        return activatorUUID;
    }
    public void setActivatorUUID(String activatorUUID) {
        this.activatorUUID = activatorUUID;
    }
}
