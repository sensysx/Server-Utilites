package me.sensys.serverutils.listeners;

import me.sensys.serverutils.Main;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.apache.commons.io.IOUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.net.URL;
import java.util.UUID;

public class WhitelistListener extends ListenerAdapter {

    // converts minecraft username to uuid
    public String getUuid(String name) {
        String url = "https://api.mojang.com/users/profiles/minecraft/" + name;
        try {
            @SuppressWarnings("deprecation")
            String UUIDJson = IOUtils.toString(new URL(url));
            if (UUIDJson.isEmpty()) return "invalid name";
            JSONObject UUIDObject = (JSONObject) JSONValue.parseWithException(UUIDJson);
            return UUIDObject.get("id").toString();
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }

        return "error";
    }

    //  sets prefix variable
    String whitelistcommand = "whitelist/";

    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if (event.getMessage().getContentDisplay().startsWith("whitelist/")) {

            // makes sure its the right channel before it runs the rest of the code
            if (!event.getChannel().equals(Main.whitelistChannel)) return;

            // transfers uuid string into object
            String username = (event.getMessage().getContentDisplay().replace(whitelistcommand, ""));
            String Uuid = getUuid(username);
            final OfflinePlayer player = Bukkit.getOfflinePlayer(UUID.fromString(java.util.UUID.fromString(Uuid.replaceFirst("(\\p{XDigit}{8})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}+)", "$1-$2-$3-$4-$5")).toString()));

            // whitelists
            player.setWhitelisted(true);

            // sends message when you are whitelisted
            event.getMessage().reply(username + "has been whitelisted").queue();
        }

    }
}
