package de.mecrytv.proxyTools.mariadb;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import de.mecrytv.proxyTools.ProxyTools;

import java.sql.Connection;
import java.sql.SQLException;

public class MariaDBManager {

    private HikariDataSource dataSource;

    public MariaDBManager() {
        String host = ProxyTools.getInstance().getServiceManager().getConfig().getString("mariadb.host");
        int port = ProxyTools.getInstance().getServiceManager().getConfig().getInt("mariadb.port");
        String database = ProxyTools.getInstance().getServiceManager().getConfig().getString("mariadb.database");
        String username = ProxyTools.getInstance().getServiceManager().getConfig().getString("mariadb.user");
        String password = ProxyTools.getInstance().getServiceManager().getConfig().getString("mariadb.password");

        HikariConfig mariaDBConfig = new HikariConfig();

        mariaDBConfig.setUsername(username);
        mariaDBConfig.setPassword(password);

        mariaDBConfig.setConnectionTimeout(2000);
        mariaDBConfig.setMaximumPoolSize(10);
        mariaDBConfig.setDriverClassName("com.mysql.cj.jdbc.Driver");

        String jdbcURL = "jdbc:mysql://" + host + ":" + port + "/" + database + "?autoReconnect=true&serverTimezone=Europe/Berlin&useSSL=false";
        mariaDBConfig.setJdbcUrl(jdbcURL);

        dataSource = new HikariDataSource(mariaDBConfig);

        try {
            Connection connection = getConnection();
            closeConnection(connection);
        } catch (SQLException e) {
            throw new IllegalStateException("MySQL-Initialisierung fehlgeschlagen", e);
        }
    }

    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public void closeConnection(Connection connection) {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            ProxyTools.getInstance().getLogger().warn("Error closing connection: " + e.getMessage());
        }
    }

    public void shutDown() {
        dataSource.close();
    }
}
