package me.sensys.serverutils;

import lombok.SneakyThrows;
import me.sensys.serverutils.listeners.*;
import me.sensys.serverutils.commands.*;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
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
    public static Guild guild;

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

        // sets variables
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

}




