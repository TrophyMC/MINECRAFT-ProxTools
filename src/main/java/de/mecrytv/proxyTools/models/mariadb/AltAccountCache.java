package de.mecrytv.proxyTools.models.mariadb;

import de.mecrytv.proxyTools.ProxyTools;
import de.mecrytv.proxyTools.cache.CacheNode;
import de.mecrytv.proxyTools.models.redis.AltAccountModel;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AltAccountCache extends CacheNode<AltAccountModel> {

    public AltAccountCache() {
        super("alt_account_cache", AltAccountModel::new);
    }

    @Override
    protected void saveToDatabase(Connection con, AltAccountModel model) {
        String sql = "INSERT INTO player_alts (ip, uuids) VALUES (?, ?) ON DUPLICATE KEY UPDATE uuids = VALUES(uuids)";
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, model.getIdentifier());
            stmt.setString(2, new com.google.gson.Gson().toJson(model.getKnownUuids()));
            stmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    @Override
    protected AltAccountModel loadFromDatabase(String identifier) {
        try (Connection con = ProxyTools.getInstance().getServiceManager().getMariaDBManager().getConnection()) {
            String sql = "SELECT uuids FROM player_alts WHERE ip = ?";
            try (PreparedStatement stmt = con.prepareStatement(sql)) {
                stmt.setString(1, identifier);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    List<String> uuids = new com.google.gson.Gson().fromJson(rs.getString("uuids"),
                            new com.google.gson.reflect.TypeToken<List<String>>(){}.getType());
                    return new AltAccountModel(identifier, uuids);
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    @Override
    public void createTableIfNotExists() {
        String sql = "CREATE TABLE IF NOT EXISTS player_alts (ip VARCHAR(45) PRIMARY KEY, uuids TEXT NOT NULL)";
        try (Connection con = ProxyTools.getInstance().getServiceManager().getMariaDBManager().getConnection();
             PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.execute();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    @Override public List<AltAccountModel> getAllFromDatabase() { return new ArrayList<>(); }
    @Override protected void removeFromDatabase(Connection conn, String identifier) throws SQLException {}
}