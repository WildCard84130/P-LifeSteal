package eu.vibemc.lifesteal.bans;

import com.google.gson.Gson;
import eu.vibemc.lifesteal.Main;
import eu.vibemc.lifesteal.bans.models.Ban;
import eu.vibemc.lifesteal.other.Config;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class BanLocalUtil {

    private static ArrayList<Ban> localBans = new ArrayList<>();

    public static Ban createLocalBan(Player player) throws IOException {
        if (getLocalBan(player.getUniqueId()) != null) {
            return null;
        }
        Ban createdBan;
        if (Config.getInt("banTime") > 0) {
            int banTime = Config.getInt("banTime") * 60;
            long unixTime = System.currentTimeMillis() / 1000L + banTime;
            Ban ban = new Ban(player.getUniqueId(), unixTime);
            localBans.add(ban);
            saveLocalBans();
            createdBan = ban;
        } else {
            Ban ban = new Ban(player.getUniqueId(), 5283862620L);
            localBans.add(ban);
            saveLocalBans();
            createdBan = ban;
        }
        player.setMaxHealth(Config.getInt("reviveHeartAmount"));
        Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getInstance(), () -> {
            if (player.isOnline()) {
                if (Config.getBoolean("banOn0Hearts")) {
                    player.kickPlayer(Config.getMessage("noMoreHeartsBan"));
                    if (Config.getBoolean("broadcastBanFrom0Hearts")) {
                        player.getServer().broadcastMessage(Config.getMessage("bannedNoMoreHeartsBroadcast").replace("${player}", player.getName()));
                    }
                }
            }
        }, 10L);

        return createdBan;
    }


    public static OfflinePlayer getOfflinePlayerByLocalBan(Ban ban) {
        return Main.getInstance().getServer().getOfflinePlayer(ban.getPlayerUUID());
    }

    public static Ban getLocalBan(UUID uuid) throws IOException {
        for (Ban ban : localBans) {
            if (ban.getPlayerUUID().equals(uuid)) {
                // check if ban is still valid
                if (ban.getUnbanTime() > System.currentTimeMillis() / 1000L) {
                    return ban;
                } else {
                    localBans.remove(ban);
                    saveLocalBans();
                    return null;
                }
            }
        }
        return null;
    }

    public static boolean deleteLocalBan(UUID uuid) throws IOException {
        if (getLocalBan(uuid) == null) {
            return false;
        }
        localBans.remove(getLocalBan(uuid));
        saveLocalBans();
        return true;
    }

    public static void saveLocalBans() throws IOException {
        Gson gson = new Gson();
        File file = new File(Main.getInstance().getDataFolder().getAbsolutePath() + "/bans.json");
        file.getParentFile().mkdir();
        file.createNewFile();
        Writer writer = null;
        writer = new FileWriter(file, false);
        gson.toJson(localBans, writer);
        writer.flush();
        writer.close();
    }

    public static void loadLocalBans() throws IOException {
        Gson gson = new Gson();
        File file = new File(Main.getInstance().getDataFolder().getAbsolutePath() + "/bans.json");
        if (file.exists()) {
            Reader reader = new FileReader(file);
            Ban[] b = gson.fromJson(reader, Ban[].class);
            localBans = new ArrayList<>(Arrays.asList(b));
        }
    }

    public static List<Ban> findAllLocalBans() {
        return localBans;
    }
}
