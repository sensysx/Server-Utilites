package me.sensys.serverutils.listeners;

import net.dv8tion.jda.api.EmbedBuilder;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

import me.sensys.serverutils.Main;
import org.bukkit.plugin.java.JavaPlugin;

public class SpigotListener extends JavaPlugin implements Listener {

    private final Map<String, String> advancementToDisplayMap = new HashMap<>();

    public void onEnable() {
        ConfigurationSection advancementMap = getConfig().getConfigurationSection("advancementMap");
        if (advancementMap != null) {
            for (String key : advancementMap.getKeys(false)) {
                ;
                advancementToDisplayMap.put(key, advancementMap.getString(key));
            }
        }
    }

    // says what to do when someone types, joins, leaves, dies, or gets a achievement
    @EventHandler
    private void onChat(AsyncPlayerChatEvent event) {
        sendMessage(event.getPlayer(), event.getMessage(), false, Color.GRAY);
    }

    @EventHandler
    private void onJoin(PlayerJoinEvent event) {
        sendMessage(event.getPlayer(), event.getPlayer().getDisplayName() + " joined the game.", true, Color.GREEN);
    }

    @EventHandler
    private void onQuit(PlayerQuitEvent event) {
        sendMessage(event.getPlayer(), event.getPlayer().getDisplayName() + " left the game.", true, Color.BLUE);
    }

    @EventHandler
    private void onDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        String deathMessage = event.getDeathMessage() == null ? player.getDisplayName() + "died." : event.getDeathMessage();
        sendMessage(player, deathMessage, true, Color.RED);
    }

    @EventHandler
    private void onAdvancement(PlayerAdvancementDoneEvent event) {
        String advancementKey = event.getAdvancement().getKey().getKey();
        String display = advancementToDisplayMap.get(advancementKey);
        if (display == null) return;
        sendMessage(event.getPlayer(), event.getPlayer().getDisplayName() + " has made the advancement {" + display +"}", true, Color.CYAN);
    }

    // defines send message
    public void sendMessage(Player player, String content, boolean contentInAuthorLine, Color color) {
        if (Main.chatChannel == null) return;

        EmbedBuilder builder = new EmbedBuilder()
                .setAuthor(
                        contentInAuthorLine ? content : player.getDisplayName(),
                        null,
                        "https://crafatar.com/avatars/" + player.getUniqueId().toString() + "?overlay=1"
                );

        if (!contentInAuthorLine) {
            builder.setDescription(content);
        }

        Main.chatChannel.sendMessageEmbeds(builder.build()).queue();
    }

}
