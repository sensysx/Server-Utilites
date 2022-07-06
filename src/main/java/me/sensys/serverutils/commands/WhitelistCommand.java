package me.sensys.serverutils.commands;

import me.sensys.serverutils.Main;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
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

public class WhitelistCommand extends ListenerAdapter {

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

    public void onSlashCommandInteraction(@NotNull SlashCommandEvent event) {
        if (event.getName().equals("whitelist")) {
            if (!event.getChannel().equals(Main.whitelistChannel)) {
                event.reply("Please use the designated whitelist channel, thank you").setEphemeral(true).queue();
                return;
            }

            // turns the uuid into a object from a string
            String username = event.getOption("username").getAsString();
            String Uuid = getUuid(username);
            final OfflinePlayer player = Bukkit.getOfflinePlayer(UUID.fromString(java.util.UUID.fromString(Uuid.replaceFirst("(\\p{XDigit}{8})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}+)", "$1-$2-$3-$4-$5")).toString()));

            if (Uuid == "error") {
                event.reply("Invalid Username").setEphemeral(true).queue();
                return;
            }

            // whitelists
            player.setWhitelisted(true);

            // sends message when you are whitelisted
            event.reply(username + "has been whitelisted").setEphemeral(true).queue();
            event.getGuild().addRoleToMember(event.getMember(), Main.whitelistRole);
        }
    }

}
