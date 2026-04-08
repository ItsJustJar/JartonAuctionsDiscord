package me.jarton.pa2discord;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.olziedev.playerauctions.api.auction.Auction;

public final class AuctionEmbedBuilder {

    public static final int GREEN = 0x57F287; // Discord green (legacy)
    public static final int RED   = 0xED4245; // Discord red   (legacy)
    public static final int ORANGE = 0xF59E0B; // New listing color

    private AuctionEmbedBuilder() {}

    public static JsonObject buildNewListingPayload(Auction auction, long rolePingId) {
        String seller = (auction.getAuctionPlayer() != null && auction.getAuctionPlayer().getName() != null)
                ? auction.getAuctionPlayer().getName()
                : "Unknown";

        String itemName;
        try {
            itemName = auction.getPrettyItemName(true);
        } catch (Throwable t) {
            itemName = "Item";
        }

        long amount;
        try {
            amount = auction.getItemAmount();
        } catch (Throwable t) {
            amount = 1;
        }

        double price;
        try {
            price = auction.getPrice();
        } catch (Throwable t) {
            price = 0.0;
        }

        long id = auction.getID();

        JsonObject embed = new JsonObject();
        embed.addProperty("title", "💲 New Listing");
        embed.addProperty("color", ORANGE);

        JsonArray fields = new JsonArray();
        fields.add(field("Seller", seller, true));
        fields.add(field("Item", itemName, true));
        fields.add(field("Amount", String.valueOf(amount), true));
        fields.add(field("Price", String.format("%.2f", price), true));
        fields.add(field("Auction ID", String.valueOf(id), true));
        embed.add("fields", fields);

        JsonObject footer = new JsonObject();
        footer.addProperty("text", "Reach out to the seller for more information.");
        embed.add("footer", footer);

        JsonObject payload = new JsonObject();

        if (rolePingId > 0) payload.addProperty("content", "<@&" + rolePingId + ">");
        else payload.addProperty("content", "");

        JsonObject allowed = new JsonObject();

        JsonArray parse = new JsonArray();
        parse.add("roles");
        allowed.add("parse", parse);

        if (rolePingId > 0) {
            JsonArray roles = new JsonArray();
            roles.add(String.valueOf(rolePingId));
            allowed.add("roles", roles);
        }

        payload.add("allowed_mentions", allowed);

        JsonArray embeds = new JsonArray();
        embeds.add(embed);
        payload.add("embeds", embeds);

        return payload;
    }


    public static JsonObject buildActivePayload(Auction auction, long rolePingId) {
        return buildPayload(auction, true, rolePingId);
    }

    public static JsonObject buildEndedPayload(Auction auction) {
        return buildPayload(auction, false, 0L);
    }

    private static JsonObject buildPayload(Auction auction, boolean active, long rolePingId) {
        String seller = (auction.getAuctionPlayer() != null && auction.getAuctionPlayer().getName() != null)
                ? auction.getAuctionPlayer().getName()
                : "Unknown";

        String itemName;
        try {
            itemName = auction.getPrettyItemName(true);
        } catch (Throwable t) {
            itemName = "Item";
        }

        long amount;
        try {
            amount = auction.getItemAmount();
        } catch (Throwable t) {
            amount = 1;
        }

        double price;
        try {
            price = auction.getPrice();
        } catch (Throwable t) {
            price = 0.0;
        }

        long id = auction.getID();

        JsonObject embed = new JsonObject();
        embed.addProperty("title", active ? "🟢 Auction Active" : "🔴 Auction Ended");
        embed.addProperty("color", active ? GREEN : RED);

        JsonArray fields = new JsonArray();
        fields.add(field("Seller", seller, true));
        fields.add(field("Item", itemName, true));
        fields.add(field("Amount", String.valueOf(amount), true));
        fields.add(field("Price", String.format("%.2f", price), true));
        fields.add(field("Status", active ? "Active" : "Ended", true));
        fields.add(field("Auction ID", String.valueOf(id), true));
        embed.add("fields", fields);

        JsonObject payload = new JsonObject();
        if (active && rolePingId > 0) payload.addProperty("content", "<@&" + rolePingId + ">");
        else payload.addProperty("content", "");

        JsonObject allowed = new JsonObject();
        JsonArray parse = new JsonArray();
        parse.add("roles");
        allowed.add("parse", parse);

        if (rolePingId > 0) {
            JsonArray roles = new JsonArray();
            roles.add(String.valueOf(rolePingId));
            allowed.add("roles", roles);
        }

        payload.add("allowed_mentions", allowed);

        JsonArray embeds = new JsonArray();
        embeds.add(embed);
        payload.add("embeds", embeds);

        return payload;
    }

    private static JsonObject field(String name, String value, boolean inline) {
        JsonObject f = new JsonObject();
        f.addProperty("name", name);
        f.addProperty("value", value);
        f.addProperty("inline", inline);
        return f;
    }
}
