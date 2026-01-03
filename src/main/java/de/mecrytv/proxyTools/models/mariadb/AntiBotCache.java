package de.mecrytv.proxyTools.models.mariadb;

import de.mecrytv.proxyTools.ProxyTools;
import de.mecrytv.proxyTools.cache.CacheNode;
import de.mecrytv.proxyTools.models.redis.AntiBotModel;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AntiBotCache extends CacheNode<AntiBotModel> {

    public AntiBotCache() {
        super("antibot_cache", AntiBotModel::new);
    }

    @Override
    protected void saveToDatabase(Connection con, AntiBotModel model) {
        String sql = "INSERT INTO verified_ips (ip, verified_at) VALUES (?, ?) ON DUPLICATE KEY UPDATE verified_at = VALUES(verified_at)";
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, model.getIdentifier());
            stmt.setLong(2, model.getVerifiedAt());
            stmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    @Override
    protected AntiBotModel loadFromDatabase(String identifier) {
        try (Connection con = ProxyTools.getInstance().getServiceManager().getMariaDBManager().getConnection()) {
            String sql = "SELECT verified_at FROM verified_ips WHERE ip = ?";
            try (PreparedStatement stmt = con.prepareStatement(sql)) {
                stmt.setString(1, identifier);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) return new AntiBotModel(identifier, rs.getLong("verified_at"));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    @Override
    public void createTableIfNotExists() {
        String sql = "CREATE TABLE IF NOT EXISTS verified_ips (ip VARCHAR(45) PRIMARY KEY, verified_at BIGINT NOT NULL)";
        try (Connection con = ProxyTools.getInstance().getServiceManager().getMariaDBManager().getConnection();
             PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.execute();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    @Override public List<AntiBotModel> getAllFromDatabase() { return new ArrayList<>(); }
    @Override protected void removeFromDatabase(Connection conn, String identifier) throws SQLException {}
}