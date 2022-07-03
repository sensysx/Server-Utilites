package me.sensys.serverutils;

import lombok.SneakyThrows;
import me.sensys.serverutils.listeners.CommandListener;
import me.sensys.serverutils.listeners.DiscordListener;
import me.sensys.serverutils.listeners.LogAppender;
import me.sensys.serverutils.listeners.SpigotListener;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.TextChannel;
import org.apache.logging.log4j.LogManager;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import javax.security.auth.login.LoginException;
import org.apache.logging.log4j.core.Logger;

public final class Main extends JavaPlugin {

    public static JDA jda;
    public static TextChannel chatChannel;
    public static TextChannel consoleChannel;
    public static Plugin plugin;

    @SneakyThrows
    @Override
    public void onEnable() {

        plugin = this;

        saveDefaultConfig();

        String botToken = getConfig().getString("bot-token");
        try {
            jda = JDABuilder.createDefault(botToken)
                    .build()
                    .awaitReady();
        } catch (InterruptedException | LoginException e) {
            e.printStackTrace();

        }

        String chatChannelId = getConfig().getString("chat-channel-id");
        if (chatChannelId != null) {
            chatChannel = jda.getTextChannelById(chatChannelId);
        }

        String consoleChannelId = getConfig().getString("console-channel-id");
        if (consoleChannelId != null) {
            consoleChannel = jda.getTextChannelById(consoleChannelId);
        }

        Logger logger = (Logger)LogManager.getRootLogger();
        LogAppender appender = new LogAppender();

        if (getConfig().getString("disc-mc-chat") == "true") {
            jda.addEventListener(new DiscordListener());
            getServer().getPluginManager().registerEvents(new SpigotListener(), this);
        }

        if (getConfig().getString("console-api") == "true") {
            jda.addEventListener(new CommandListener());
            logger.addAppender(appender);
        }


    }

    @Override
    public void onDisable() {
        consoleChannel.sendMessage("Console Terminated").queue();
        if (jda != null) jda.shutdownNow();
    }



}




