package fr.mrtigreroux.tigerreportssupports.listeners;

import java.util.Objects;

import fr.mrtigreroux.tigerreports.logs.Logger;
import fr.mrtigreroux.tigerreportssupports.bots.DiscordBot;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.EventListener;

/**
 * @author MrTigreroux
 */

public class DiscordListener implements EventListener {

	private final DiscordBot bot;

	public DiscordListener(DiscordBot bot) {
		super();
		this.bot = Objects.requireNonNull(bot);
	}

	@Override
	public void onEvent(GenericEvent event) {
		if (event instanceof MessageReceivedEvent) {
			MessageReceivedEvent e = (MessageReceivedEvent) event;
			Logger.EVENTS.info(() -> "DiscordListener: onEvent(): MessageReceivedEvent: " + e);
			if (e.getChannelType().equals(ChannelType.TEXT)) {
				User u = e.getAuthor();
				if (!u.isBot()) {
					String message = e.getMessage().getContentRaw().toLowerCase();
					if (message.startsWith("/tigerreports ")) {
						bot.onCommand(e.getTextChannel(), message.substring(14), u);
					}
				}
			}
		}
	}

}
