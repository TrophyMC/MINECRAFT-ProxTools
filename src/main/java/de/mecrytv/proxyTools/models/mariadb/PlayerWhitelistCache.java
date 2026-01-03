package de.mecrytv.proxyTools.models.mariadb;

import de.mecrytv.proxyTools.ProxyTools;
import de.mecrytv.proxyTools.cache.CacheNode;
import de.mecrytv.proxyTools.models.redis.PlayerWhitelistModel;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PlayerWhitelistCache extends CacheNode<PlayerWhitelistModel> {

    public PlayerWhitelistCache(){
        super("player_whitelist_cache", PlayerWhitelistModel::new);
    }

    @Override
    protected void saveToDatabase(Connection con, PlayerWhitelistModel model) {
        String sql = "INSERT INTO player_whitelist (uuid, name, whitelisted_at) VALUES (?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE name = VALUES(name), whitelisted_at = VALUES(whitelisted_at)";

        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, model.getIdentifier());
            stmt.setString(2, model.getName());
            stmt.setTimestamp(3, model.getWhitelistedAt());
            stmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected PlayerWhitelistModel loadFromDatabase(String identifier) {
        try (Connection con = ProxyTools.getInstance().getServiceManager().getMariaDBManager().getConnection()) {
            String sql = "SELECT uuid, name, whitelisted_at FROM player_whitelist WHERE uuid = ?";
            PreparedStatement stmt = con.prepareStatement(sql);
            stmt.setString(1, identifier);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return new PlayerWhitelistModel(identifier, rs.getString("name"), rs.getTimestamp("whitelisted_at"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<PlayerWhitelistModel> getAllFromDatabase() {
        List<PlayerWhitelistModel> list = new ArrayList<>();
        String sql = "SELECT uuid, name, whitelisted_at FROM player_whitelist";

        try (Connection con = ProxyTools.getInstance().getServiceManager().getMariaDBManager().getConnection();
             PreparedStatement stmt = con.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                list.add(new PlayerWhitelistModel(
                        rs.getString("uuid"),
                        rs.getString("name"),
                        rs.getTimestamp("whitelisted_at")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public void createTableIfNotExists() {
        String sql = "CREATE TABLE IF NOT EXISTS player_whitelist (" +
                "uuid VARCHAR(36) PRIMARY KEY, " +
                "name VARCHAR(16) NOT NULL, " + // Neue Spalte
                "whitelisted_at TIMESTAMP NOT NULL" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;";

        try (Connection con = ProxyTools.getInstance().getServiceManager().getMariaDBManager().getConnection();
             PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void removeFromDatabase(Connection conn, String identifier) throws SQLException {
        String sql = "DELETE FROM player_whitelist WHERE uuid = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, identifier);
            ps.executeUpdate();
        }
    }
}
