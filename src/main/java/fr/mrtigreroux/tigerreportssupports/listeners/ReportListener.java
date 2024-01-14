package fr.mrtigreroux.tigerreportssupports.listeners;

import java.util.Objects;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import fr.mrtigreroux.tigerreports.bungee.BungeeManager;
import fr.mrtigreroux.tigerreports.events.NewReportEvent;
import fr.mrtigreroux.tigerreports.events.ProcessReportEvent;
import fr.mrtigreroux.tigerreports.events.ReportStatusChangeEvent;
import fr.mrtigreroux.tigerreports.logs.Logger;
import fr.mrtigreroux.tigerreports.objects.reports.Report;
import fr.mrtigreroux.tigerreports.utils.ConfigUtils;
import fr.mrtigreroux.tigerreportssupports.TigerReportsSupports;
import fr.mrtigreroux.tigerreportssupports.bots.DiscordBot;
import fr.mrtigreroux.tigerreportssupports.config.ConfigFile;

/**
 * @author MrTigreroux
 */

public class ReportListener implements Listener {
    
    private static final Logger LOGGER =
            Logger.fromClass(ReportListener.class, TigerReportsSupports.getInstance().getName());
    
    private final DiscordBot discordBot;
    private final BungeeManager bm;
    
    public ReportListener(DiscordBot bot, BungeeManager bm) {
        this.discordBot = Objects.requireNonNull(bot);
        this.bm = bm;
    }
    
    @EventHandler
    public void onNewReport(NewReportEvent e) {
        String reportServerName = e.getServer();
        Logger.EVENTS.info(
                () -> "onNewReport(): id = " + e.getReport().getId() + ", server = " + e.getServer()
        );
        if (notify(reportServerName)) {
            discordBot.notifyReport(reportServerName, e.getReport());
        }
    }
    
    @EventHandler
    public void onProcessReport(ProcessReportEvent e) {
        Logger.EVENTS.info(
                () -> "onProcessReport(): id = " + e.getReport().getId() + ", staff = "
                        + e.getStaff() + ", bungee = " + e.isFromBungeeCord()
        );
        Report r = e.getReport();
        if (notify(!e.isFromBungeeCord()))
            discordBot.notifyProcessReport(r, e.getStaff());
    }
    
    @EventHandler
    public void onReportStatusChange(ReportStatusChangeEvent e) {
        Logger.EVENTS.info(
                () -> "onReportStatusChange(): id = " + e.getReport().getId() + ", bungee = "
                        + e.isFromBungeeCord()
        );
        Report r = e.getReport();
        if (notify(!e.isFromBungeeCord())) {
            discordBot.updateReportStatus(r);
        }
    }
    
    private boolean notify(String reportServerName) {
        LOGGER.info(
                () -> "notify(" + reportServerName + "): bm = " + bm + ", bm.getServerName() = "
                        + (bm != null ? bm.getServerName() : null)
        );
        return notify(bm.getServerName().equals(reportServerName));
    }
    
    private boolean notify(boolean isLocalEvent) {
        LOGGER.info(() -> "notify(" + isLocalEvent + ")");
        return !ConfigUtils
                .isEnabled(ConfigFile.CONFIG.get(), "Config.Discord.NotifyOnlyLocalReports")
                || isLocalEvent;
    }
    
}
