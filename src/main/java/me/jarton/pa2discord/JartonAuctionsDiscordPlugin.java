package me.jarton.pa2discord;

import com.olziedev.playerauctions.api.PlayerAuctionsAPI;
import me.jarton.pa2discord.discord.DiscordWebhookClient;
import me.jarton.pa2discord.storage.AuctionStorage;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.stream.Collectors;

public class JartonAuctionsDiscordPlugin extends JavaPlugin {

    private DiscordWebhookClient webhook;
    private AuctionStorage storage;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }

        String webhookUrl = getConfig().getString("webhook_url", "").trim();

        long rolePingId = getConfig().getLong("ping_role_id",
                getConfig().getLong("role_ping_id", 0L));

        if (webhookUrl.isEmpty()) {
            getLogger().severe("webhook_url is missing in config.yml");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        this.webhook = new DiscordWebhookClient(webhookUrl);
        this.storage = new AuctionStorage(getDataFolder());

        Bukkit.getScheduler().runTaskLater(this, () -> {
            try {
                PlayerAuctionsAPI api = PlayerAuctionsAPI.getInstance();

                var currentIds = api.getPlayerAuctions().stream()
                        .map(a -> a.getID())
                        .collect(Collectors.toSet());

                storage.markAllSeen(currentIds);

                new AuctionPollService(this, api, storage, webhook, rolePingId).start();

                getLogger().info("JartonAuctionsDiscord: Baseline set. Now posting ONLY new listings.");

            } catch (Exception e) {
                e.printStackTrace();
                getServer().getPluginManager().disablePlugin(this);
            }
        }, 40L); // 2 seconds (40 ticks)
    }
}
