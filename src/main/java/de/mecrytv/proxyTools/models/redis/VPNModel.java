package de.mecrytv.proxyTools.models.redis;

import com.google.gson.JsonObject;
import de.mecrytv.proxyTools.models.ICacheModel;

public class VPNModel implements ICacheModel {

    private String ip;
    private boolean vpn;

    public VPNModel() {}

    public VPNModel(String ip, boolean vpn) {
        this.ip = ip;
        this.vpn = vpn;
    }

    @Override
    public String getIdentifier() { return ip; }

    @Override
    public JsonObject serialize() {
        JsonObject json = new JsonObject();
        json.addProperty("ip", ip);
        json.addProperty("vpn", vpn);
        return json;
    }

    @Override
    public void deserialize(JsonObject json) {
        this.ip = json.get("ip").getAsString();
        this.vpn = json.get("vpn").getAsBoolean();
    }

    public boolean isVpn() { return vpn; }
}