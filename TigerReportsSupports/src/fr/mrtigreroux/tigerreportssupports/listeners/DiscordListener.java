package fr.mrtigreroux.tigerreportssupports.listeners;

import fr.mrtigreroux.tigerreportssupports.TigerReportsSupports;
import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.events.Event;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.EventListener;

/**
 * @author MrTigreroux
 */

public class DiscordListener implements EventListener {

	@Override
	public void onEvent(Event event) {
		if(event instanceof MessageReceivedEvent) {
			MessageReceivedEvent e = (MessageReceivedEvent) event;
			if(!e.getChannelType().equals(ChannelType.TEXT) || e.getAuthor().equals(e.getJDA().getSelfUser())) return;
			
			String message = e.getMessage().getContentRaw().toLowerCase();
			if(message.startsWith("/tigerreports ")) TigerReportsSupports.getDiscordBot().onCommand(e.getTextChannel(), message.toLowerCase().substring(14), e.getAuthor().getAsMention());
		}
	}

}
