package fr.mrtigreroux.tigerreportssupports.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import fr.mrtigreroux.tigerreports.events.NewReportEvent;
import fr.mrtigreroux.tigerreports.events.ProcessReportEvent;
import fr.mrtigreroux.tigerreportssupports.TigerReportsSupports;
import fr.mrtigreroux.tigerreportssupports.bots.DiscordBot;

/**
 * @author MrTigreroux
 */

public class ReportListener implements Listener {

	@EventHandler
	public void onNewReport(NewReportEvent e) {
		DiscordBot discordBot = TigerReportsSupports.getInstance().getDiscordBot();
		if(discordBot != null)
			discordBot.notifyReport(e.getServer(), e.getReport());
	}

	@EventHandler
	public void onProcessReport(ProcessReportEvent e) {
		DiscordBot discordBot = TigerReportsSupports.getInstance().getDiscordBot();
		if(discordBot != null)
			discordBot.notifyProcessReport(e.getReport(), e.getStaff());
	}
	
}
