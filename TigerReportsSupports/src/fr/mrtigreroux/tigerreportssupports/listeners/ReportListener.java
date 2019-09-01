package fr.mrtigreroux.tigerreportssupports.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import fr.mrtigreroux.tigerreports.events.NewReportEvent;
import fr.mrtigreroux.tigerreports.events.ProcessReportEvent;
import fr.mrtigreroux.tigerreports.events.ReportStatusChangeEvent;
import fr.mrtigreroux.tigerreportssupports.TigerReportsSupports;
import fr.mrtigreroux.tigerreportssupports.bots.DiscordBot;
import fr.mrtigreroux.tigerreportssupports.bots.Status;

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

	@EventHandler
	public void onReportStatusChange(ReportStatusChangeEvent e) {
		DiscordBot discordBot = TigerReportsSupports.getInstance().getDiscordBot();
		if(discordBot != null)
			discordBot.updateReportStatus(e.getReport(), Status.valueOf(e.getStatus().toUpperCase()));
	}
	
}
