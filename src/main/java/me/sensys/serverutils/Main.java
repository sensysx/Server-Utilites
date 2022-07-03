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
    private LogAppender appender;

    @SneakyThrows
    @Override
    public void onEnable() {

        plugin = this;

        saveDefaultConfig();

        String botToken = "OTkyODY4MTQ4NTMxNDMzNTEy.GXG71C.vPxjIXnN8-RfgOjnIaLEukZGI9hicDZS-gEaEE";
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


        consoleChannel = jda.getTextChannelById("992861677773135932");
        consoleChannel.sendMessage("Console Accesed");


        Logger logger = (Logger)LogManager.getRootLogger();
        LogAppender appender = new LogAppender();

        if (getConfig().getString("Disc-Mc-Chat") == "true") {
            jda.addEventListener(new DiscordListener());
            getServer().getPluginManager().registerEvents(new SpigotListener(), this);
        }

        jda.addEventListener(new CommandListener());
        logger.addAppender(appender);


    }

    @Override
    public void onDisable() {
        consoleChannel.sendMessage("Console Terminated").queue();
        if (jda != null) jda.shutdownNow();
    }



}




