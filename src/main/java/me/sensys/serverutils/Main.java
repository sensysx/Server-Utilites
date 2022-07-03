package me.sensys.serverutils;

import lombok.SneakyThrows;
import me.sensys.serverutils.listeners.*;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.TextChannel;
import org.apache.logging.log4j.LogManager;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import javax.security.auth.login.LoginException;
import org.apache.logging.log4j.core.Logger;

public final class Main extends JavaPlugin {

    // varibles
    public static JDA jda;
    public static TextChannel chatChannel;
    public static TextChannel consoleChannel;
    public static TextChannel whitelistChannel;
    public static Plugin plugin;

    @SneakyThrows
    @Override
    public void onEnable() {

        plugin = this;

        saveDefaultConfig();

        // logs into bot
        String botToken = getConfig().getString("bot-token");
        try {
            jda = JDABuilder.createDefault(botToken)
                    .build()
                    .awaitReady();
        } catch (InterruptedException | LoginException e) {
            e.printStackTrace();

        }

        // sets varibles
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

        Logger logger = (Logger)LogManager.getRootLogger();
        LogAppender appender = new LogAppender();

        // sets up listeners
        if (getConfig().getString("disc-mc-chat") == "true") {
            jda.addEventListener(new DiscordListener());
            getServer().getPluginManager().registerEvents(new SpigotListener(), this);
        }

        if (getConfig().getString("console-api") == "true") {
            jda.addEventListener(new CommandListener());
            logger.addAppender(appender);
        }

        if (getConfig().getString("whitelist") == "true") {
            jda.addEventListener(new WhitelistListener());
        }


    }

    @Override
    public void onDisable() {

        // logs out of bot
        if (jda != null) jda.shutdownNow();
    }



}




