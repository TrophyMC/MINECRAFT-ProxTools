package de.mecrytv.proxyTools.models.mariadb;

import de.mecrytv.proxyTools.ProxyTools;
import de.mecrytv.proxyTools.cache.CacheNode;
import de.mecrytv.proxyTools.models.redis.ServerMaintenanceModel;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ServerMaintenanceCache extends CacheNode<ServerMaintenanceModel> {

    public ServerMaintenanceCache() {
        super("server_maintenance_cache", ServerMaintenanceModel::new);
    }

    @Override
    protected void saveToDatabase(Connection con, ServerMaintenanceModel model) {
        String sql = "INSERT INTO server_maintenance (id, active, activator_uuid) VALUES (?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE active = VALUES(active), activator_uuid = VALUES(activator_uuid)";

        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, model.getIdentifier());
            stmt.setBoolean(2, model.isActive());
            stmt.setString(3, model.getActivatorUUID());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected ServerMaintenanceModel loadFromDatabase(String identifier) {
        try (Connection con = ProxyTools.getInstance().getServiceManager().getMariaDBManager().getConnection()) {
            String sql = "SELECT active, activator_uuid FROM server_maintenance WHERE id = ?";
            try (PreparedStatement stmt = con.prepareStatement(sql)) {
                stmt.setString(1, identifier);
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    return new ServerMaintenanceModel(rs.getBoolean("active"), rs.getString("activator_uuid"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<ServerMaintenanceModel> getAllFromDatabase() {
        List<ServerMaintenanceModel> list = new ArrayList<>();
        String sql = "SELECT active, activator_uuid FROM server_maintenance";

        try (Connection con = ProxyTools.getInstance().getServiceManager().getMariaDBManager().getConnection();
             PreparedStatement stmt = con.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                list.add(new ServerMaintenanceModel(
                        rs.getBoolean("active"),
                        rs.getString("activator_uuid")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public void createTableIfNotExists() {
        String sql = "CREATE TABLE IF NOT EXISTS server_maintenance (" +
                "id VARCHAR(32) PRIMARY KEY, " +
                "active BOOLEAN NOT NULL DEFAULT FALSE, " +
                "activator_uuid VARCHAR(36) NOT NULL" +
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
        // Eigentlich wird der Wartungsmodus nie gelöscht, nur auf 'active = false' gesetzt,
        // aber für die Vollständigkeit des CacheNodes:
        String sql = "DELETE FROM server_maintenance WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, identifier);
            ps.executeUpdate();
        }
    }
}