package eu.vibemc.lifesteal.mysql;

import com.mysql.cj.jdbc.MysqlConnectionPoolDataSource;
import com.mysql.cj.jdbc.MysqlDataSource;
import eu.vibemc.lifesteal.Main;
import eu.vibemc.lifesteal.bans.models.Ban;
import eu.vibemc.lifesteal.other.Config;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

public class MySQL {
    private static MysqlDataSource dataSource = new MysqlConnectionPoolDataSource();

    public static void setup() throws SQLException {
        dataSource.setServerName(Config.getString("mysql.host"));
        dataSource.setPortNumber(Config.getInt("mysql.port"));
        dataSource.setDatabaseName(Config.getString("mysql.database"));
        dataSource.setUser(Config.getString("mysql.username"));
        dataSource.setPassword(Config.getString("mysql.password"));
        try (Connection conn = dataSource.getConnection()) {
            if (!conn.isValid(5)) {
                Bukkit.getConsoleSender().sendMessage(ChatColor.DARK_RED + "Can't connect to MySQL!");
            } else {
                Bukkit.getConsoleSender().sendMessage(ChatColor.DARK_GREEN + "Connected to MySQL!");
                // create new table if not exists called ls_bans with columns: uuid as VARCHAR(255) and unban_time as long
                conn.prepareStatement("CREATE TABLE IF NOT EXISTS ls_bans (uuid VARCHAR(255), unbanTime BIGINT)").execute();
                conn.close();
            }
        }
    }

}
