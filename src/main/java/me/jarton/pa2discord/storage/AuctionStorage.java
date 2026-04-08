package me.jarton.pa2discord.storage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Type;
import java.util.*;

public class AuctionStorage {

    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final File seenFile;
    private final File mappingFile;

    private final Set<Long> seenAuctionIds = new HashSet<>();

    private final Map<Long, String> tracked = new HashMap<>();

    public AuctionStorage(File dataFolder) {
        this.seenFile = new File(dataFolder, "seen_auctions.json");
        this.mappingFile = new File(dataFolder, "auction_message_map.json");
        load();
    }

    public boolean hasSeen(long auctionId) {
        return seenAuctionIds.contains(auctionId);
    }

    public void markSeen(long auctionId) {
        if (seenAuctionIds.add(auctionId)) saveSeen();
    }

    public void markAllSeen(Collection<Long> ids) {
        boolean changed = false;
        for (Long id : ids) {
            if (seenAuctionIds.add(id)) changed = true;
        }
        if (changed) saveSeen();
    }

    public void trackMessage(long auctionId, String messageId) {
        tracked.put(auctionId, messageId);
        saveMapping();
    }

    public String getTrackedMessageId(long auctionId) {
        return tracked.get(auctionId);
    }

    public Map<Long, String> getAllTrackedMessages() {
        return new HashMap<>(tracked);
    }

    public void removeTracked(long auctionId) {
        if (tracked.remove(auctionId) != null) saveMapping();
    }

    private void load() {
        try {
            if (seenFile.exists()) {
                Type setType = new TypeToken<Set<Long>>() {}.getType();
                try (FileReader r = new FileReader(seenFile)) {
                    Set<Long> loaded = gson.fromJson(r, setType);
                    if (loaded != null) seenAuctionIds.addAll(loaded);
                }
            }
        } catch (Exception ignored) {}

        try {
            if (mappingFile.exists()) {
                Type mapType = new TypeToken<Map<Long, String>>() {}.getType();
                try (FileReader r = new FileReader(mappingFile)) {
                    Map<Long, String> loaded = gson.fromJson(r, mapType);
                    if (loaded != null) tracked.putAll(loaded);
                }
            }
        } catch (Exception ignored) {}
    }

    private void saveSeen() {
        try (FileWriter w = new FileWriter(seenFile)) {
            gson.toJson(seenAuctionIds, w);
        } catch (Exception ignored) {}
    }

    private void saveMapping() {
        try (FileWriter w = new FileWriter(mappingFile)) {
            gson.toJson(tracked, w);
        } catch (Exception ignored) {}
    }
}
