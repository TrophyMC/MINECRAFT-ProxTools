package de.mecrytv.proxyTools.models.mariadb;

import de.mecrytv.proxyTools.ProxyTools;
import de.mecrytv.proxyTools.cache.CacheNode;
import de.mecrytv.proxyTools.models.redis.VPNModel;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class VPNCache extends CacheNode<VPNModel> {

    public VPNCache() {
        super("vpn_cache", VPNModel::new);
    }

    @Override
    protected void saveToDatabase(Connection con, VPNModel model) {
        String sql = "INSERT INTO vpn_storage (ip, is_vpn) VALUES (?, ?) ON DUPLICATE KEY UPDATE is_vpn = VALUES(is_vpn)";
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, model.getIdentifier());
            stmt.setBoolean(2, model.isVpn());
            stmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    @Override
    protected VPNModel loadFromDatabase(String identifier) {
        try (Connection con = ProxyTools.getInstance().getServiceManager().getMariaDBManager().getConnection()) {
            String sql = "SELECT is_vpn FROM vpn_storage WHERE ip = ?";
            try (PreparedStatement stmt = con.prepareStatement(sql)) {
                stmt.setString(1, identifier);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) return new VPNModel(identifier, rs.getBoolean("is_vpn"));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    @Override
    public void createTableIfNotExists() {
        String sql = "CREATE TABLE IF NOT EXISTS vpn_storage (ip VARCHAR(45) PRIMARY KEY, is_vpn BOOLEAN NOT NULL)";
        try (Connection con = ProxyTools.getInstance().getServiceManager().getMariaDBManager().getConnection();
             PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.execute();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    @Override
    public List<VPNModel> getAllFromDatabase() { return new ArrayList<>(); }

    @Override
    protected void removeFromDatabase(Connection conn, String identifier) throws SQLException {
        String sql = "DELETE FROM vpn_storage WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, identifier);
            ps.executeUpdate();
        }
    }
}