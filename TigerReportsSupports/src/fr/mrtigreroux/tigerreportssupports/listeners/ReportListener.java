package fr.mrtigreroux.tigerreportssupports.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import fr.mrtigreroux.tigerreports.events.NewReportEvent;
import fr.mrtigreroux.tigerreportssupports.TigerReportsSupports;
import fr.mrtigreroux.tigerreportssupports.bots.DiscordBot;

/**
 * @author MrTigreroux
 */

public class ReportListener implements Listener {

	@EventHandler
	public void onReport(NewReportEvent e) {
		DiscordBot discordBot = TigerReportsSupports.getDiscordBot();
		if(discordBot != null) discordBot.notifyReport(e.getServer(), e.getReport());
	}
	
}
