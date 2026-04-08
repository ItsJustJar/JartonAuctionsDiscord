package me.jarton.pa2discord;

import com.olziedev.playerauctions.api.PlayerAuctionsAPI;
import com.olziedev.playerauctions.api.auction.Auction;
import me.jarton.pa2discord.discord.DiscordWebhookClient;
import me.jarton.pa2discord.storage.AuctionStorage;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;

public final class AuctionPollService {

    private final JartonAuctionsDiscordPlugin plugin;
    private final PlayerAuctionsAPI api;
    private final AuctionStorage storage;
    private final DiscordWebhookClient webhook;
    private final long rolePingId;

    public AuctionPollService(JartonAuctionsDiscordPlugin plugin,
                              PlayerAuctionsAPI api,
                              AuctionStorage storage,
                              DiscordWebhookClient webhook,
                              long rolePingId) {
        this.plugin = plugin;
        this.api = api;
        this.storage = storage;
        this.webhook = webhook;
        this.rolePingId = rolePingId;
    }

    public void start() {
        int pollSeconds = plugin.getConfig().getInt("update_interval_seconds",
                plugin.getConfig().getInt("poll_interval_seconds", 10));

        new BukkitRunnable() {
            @Override
            public void run() {
                pollOnce();
            }
        }.runTaskTimerAsynchronously(plugin, 40L, pollSeconds * 20L);
    }

    private void pollOnce() {
        try {
            List<Auction> live = api.getPlayerAuctions();

            for (Auction a : live) {
                long id = a.getID();

                if (storage.hasSeen(id)) continue;

                try {
                    webhook.sendAndReturnMessageId(
                            AuctionEmbedBuilder.buildNewListingPayload(a, rolePingId)
                    );
                    storage.markSeen(id);
                } catch (Exception ignored) {
                }
            }

        } catch (Exception ignored) {
        }
    }
}
