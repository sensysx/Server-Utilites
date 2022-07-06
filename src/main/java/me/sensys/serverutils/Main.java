package me.sensys.serverutils;

import lombok.SneakyThrows;
import me.sensys.serverutils.listeners.*;
import me.sensys.serverutils.commands.*;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import javax.security.auth.login.LoginException;
import org.apache.logging.log4j.core.Logger;
import org.jetbrains.annotations.NotNull;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;

import java.awt.*;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.bukkit.Bukkit.getServer;

public final class Main extends JavaPlugin {

    // varibles
    public static JDA jda;
    public static TextChannel chatChannel;
    public static TextChannel consoleChannel;
    public static TextChannel whitelistChannel;
    public static Plugin plugin;
    public static Guild guild;
    public static Role whitelistRole;
    private final Map<String, String> advancementToDisplayMap = new HashMap<>();

    @SneakyThrows
    @Override
    public void onEnable() {

        plugin = this;

        saveDefaultConfig();

        // logs into bot
        String botToken = getConfig().getString("bot-token");
        try {
            jda = JDABuilder.createDefault(botToken)
                    .setActivity(Activity.watching("Watching The Minecraft Server"))
                    .addEventListeners(new WhitelistCommand())
                    .build().awaitReady();
        } catch (InterruptedException | LoginException e) {
            e.printStackTrace();

        }

        // sets variables

        ConfigurationSection advancementMap = getConfig().getConfigurationSection("advancementMap");
        if (advancementMap != null) {
            for (String key : advancementMap.getKeys(false)) {
                ;
                advancementToDisplayMap.put(key, advancementMap.getString(key));
            }
        }
        String chatChannelId = getConfig().getString("chat-channel-id");
        if (chatChannelId != null) {
            chatChannel = jda.getTextChannelById(chatChannelId);
        }

        String consoleChannelId = getConfig().getString("console-channel-id");
        if (consoleChannelId != null) {
            consoleChannel = jda.getTextChannelById(consoleChannelId);
        }

        String whitelistChannelId = getConfig().getString("whitelist-channel-id");
        if (whitelistChannelId != null) {
            whitelistChannel = jda.getTextChannelById(whitelistChannelId);
        }

        String guildId = getConfig().getString("guild-id");
        if (consoleChannelId != null) {
            guild = jda.getGuildById(guildId);
        } else {
            System.out.println("Guild ID null, Please check your discord server id in the config for any mistakes");
        }

        String whitelistRoleId = getConfig().getString("whitelist-role-id");
        if (whitelistRoleId != null) {
            whitelistRole = jda.getRoleById(whitelistRoleId);
        }

        Logger logger = (Logger) LogManager.getRootLogger();
        LogAppender appender = new LogAppender();

        // sets up listeners

        jda.addEventListener(new DiscordListener());
        getServer().getPluginManager().registerEvents(new SpigotListener(), this);

        jda.addEventListener(new CommandListener());
        logger.addAppender(appender);

        guild.updateCommands()
                .addCommands(new CommandData("whitelist", "whitelists a player in the minecraft server")
                        .addOption(OptionType.STRING, "username", "The username of the minecraft account that you want to be whitelisted")
                ).queue();


    }

    @Override
    public void onDisable() {

        // logs out of bot
        if (jda != null) jda.shutdownNow();
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


    public final class SpigotListener implements Listener {
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
            sendMessage(event.getPlayer(), event.getPlayer().getDisplayName() + " has made the advancement {" + display + "}", true, Color.CYAN);
        }
    }

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

        @Override
        public void onSlashCommand(@NotNull SlashCommandEvent event) {
            if (event.getName().equals("whitelist")) {
                if (!event.getChannel().equals(Main.whitelistChannel)) {
                    event.reply("Please use the designated whitelist channel, thank you").setEphemeral(true).queue();
                    return;
                }

                // turns the uuid into a object from a string
                String username = event.getOption("username").getAsString();

                getServer().getScheduler().scheduleSyncDelayedTask(Main.plugin, () -> {
                    getServer().dispatchCommand(getServer().getConsoleSender(), "whitelist add " + username);
                });

                // sends message when you are whitelisted
                event.reply(username + " has been whitelisted").setEphemeral(true).queue();
                guild.addRoleToMember(event.getMember(), whitelistRole).queue();

            }
        }
    }
}







