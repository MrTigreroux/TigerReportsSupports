package fr.mrtigreroux.tigerreportssupports.listeners;

import fr.mrtigreroux.tigerreportssupports.TigerReportsSupports;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.EventListener;

/**
 * @author MrTigreroux
 */

public class DiscordListener implements EventListener {

	@Override
	public void onEvent(GenericEvent event) {
		if (event instanceof MessageReceivedEvent) {
			MessageReceivedEvent e = (MessageReceivedEvent) event;
			if (e.getChannelType().equals(ChannelType.TEXT)) {
				User u = e.getAuthor();
				if (!u.isBot()) {
					String message = e.getMessage().getContentRaw().toLowerCase();
					if (message.startsWith("/tigerreports "))
						TigerReportsSupports.getInstance()
						        .getDiscordBot()
						        .onCommand(e.getTextChannel(), message.substring(14), u);
				}
			}
		}
	}

}
