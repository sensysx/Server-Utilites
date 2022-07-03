package me.sensys.serverutils.listeners;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import me.sensys.serverutils.Main;
import org.bukkit.Bukkit;

import static org.bukkit.Bukkit.getServer;

public final class CommandListener extends ListenerAdapter {



    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if (!event.getChannel().equals(Main.consoleChannel)) return;

        Member member = event.getMember();
        if (member == null || member.getUser().isBot()) return;

        String command = event.getMessage().getContentDisplay();
        getServer().getScheduler().scheduleSyncDelayedTask(Main.plugin, () -> {
            getServer().dispatchCommand(getServer().getConsoleSender(), command);
        });
    }}
