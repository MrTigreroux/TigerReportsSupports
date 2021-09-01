package fr.mrtigreroux.tigerreportssupports.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import fr.mrtigreroux.tigerreports.TigerReports;
import fr.mrtigreroux.tigerreports.events.NewReportEvent;
import fr.mrtigreroux.tigerreports.events.ProcessReportEvent;
import fr.mrtigreroux.tigerreports.events.ReportStatusChangeEvent;
import fr.mrtigreroux.tigerreports.utils.ConfigUtils;
import fr.mrtigreroux.tigerreportssupports.TigerReportsSupports;
import fr.mrtigreroux.tigerreportssupports.bots.DiscordBot;
import fr.mrtigreroux.tigerreportssupports.bots.Status;
import fr.mrtigreroux.tigerreportssupports.config.ConfigFile;

/**
 * @author MrTigreroux
 */

public class ReportListener implements Listener {

	private TigerReportsSupports trs;

	public ReportListener(TigerReportsSupports trs) {
		this.trs = trs;
	}

	@EventHandler
	public void onNewReport(NewReportEvent e) {
		DiscordBot discordBot = trs.getDiscordBot();
		String reportServerName = e.getServer();
		if (discordBot != null && notify(reportServerName))
			discordBot.notifyReport(reportServerName, e.getReport());
	}

	@EventHandler
	public void onProcessReport(ProcessReportEvent e) {
		DiscordBot discordBot = trs.getDiscordBot();
		if (discordBot != null)
			discordBot.notifyProcessReport(e.getReport(), e.getStaff());
	}

	@EventHandler
	public void onReportStatusChange(ReportStatusChangeEvent e) {
		DiscordBot discordBot = trs.getDiscordBot();
		if (discordBot != null)
			discordBot.updateReportStatus(e.getReport(), Status.valueOf(e.getStatus().toUpperCase()));
	}

	private boolean notify(String reportServerName) {
		return !ConfigUtils.isEnabled(ConfigFile.CONFIG.get(), "Config.Discord.NotifyOnlyLocalReports")
		        || TigerReports.getInstance().getBungeeManager().getServerName().equals(reportServerName);
	}

}
