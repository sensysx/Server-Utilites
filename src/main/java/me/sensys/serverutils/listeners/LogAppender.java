package me.sensys.serverutils.listeners;

import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import me.sensys.serverutils.Main;
import java.text.SimpleDateFormat;
import java.util.Date;

public class LogAppender extends AbstractAppender {

    public LogAppender() {
        super("MyLogAppender", null, null);
        start();
    }

    SimpleDateFormat formatter = new SimpleDateFormat("yyyy.MM.dd 'at' HH:mm:ss z");

    //sends logs to discord
    @Override
    public void append(LogEvent event) {
        LogEvent log = event.toImmutable();

        String message = log.getMessage().getFormattedMessage();

        message = "[" +formatter.format(new Date(event.getTimeMillis())) + " " + event.getLevel().toString() + "] " + message;

        Main.consoleChannel.sendMessage(message).queue();
    }

}

