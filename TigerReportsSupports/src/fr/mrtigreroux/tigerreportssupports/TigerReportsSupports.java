package fr.mrtigreroux.tigerreportssupports;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import fr.mrtigreroux.tigerreports.utils.ConfigUtils;
import fr.mrtigreroux.tigerreportssupports.bots.DiscordBot;
import fr.mrtigreroux.tigerreportssupports.config.ConfigFile;
import fr.mrtigreroux.tigerreportssupports.listeners.ReportListener;

/**
 * @author MrTigreroux
 */

public class TigerReportsSupports extends JavaPlugin {

	private static TigerReportsSupports instance;
	public static DiscordBot discordBot = null;
	
	public static void load() {
		for(ConfigFile configFiles : ConfigFile.values()) configFiles.load();
	}
	
	@Override
	public void onEnable() {
		instance = this;
		
		PluginManager pm = Bukkit.getPluginManager();
		if(!pm.getPlugin("TigerReports").isEnabled()) {
			Logger logger = Bukkit.getLogger();
			logger.log(Level.SEVERE, "------------------------------------------------------");
			logger.log(Level.SEVERE, "[TigerReportsSupports] Plugin TigerReports must be installed.");
			logger.log(Level.SEVERE, "You can download it here:");
			logger.log(Level.SEVERE, "https://www.spigotmc.org/resources/tigerreports.25773/");
			logger.log(Level.SEVERE, "------------------------------------------------------");
			pm.disablePlugin(this);
			return;
		}
		
		load();
		pm.registerEvents(new ReportListener(), this);
		
		PluginDescriptionFile desc = getDescription();
		if(!desc.getName().equals("TigerReportsSupports") || desc.getAuthors().size() > 1 || !desc.getAuthors().contains("MrTigreroux")) {
			Logger logger = Bukkit.getLogger();
			logger.log(Level.SEVERE, "------------------------------------------------------");
			if(ConfigUtils.getInfoLanguage().equalsIgnoreCase("English")) {
				logger.log(Level.SEVERE, "[TigerReportsSupports] File plugin.yml has been edited");
				logger.log(Level.SEVERE, "without authorization.");
			} else {
				logger.log(Level.SEVERE, "[TigerReportsSupports] Le fichier plugin.yml a ete modifie");
				logger.log(Level.SEVERE, "sans autorisation.");
			}
			logger.log(Level.SEVERE, "------------------------------------------------------");
			Bukkit.shutdown();
		}
		
		if(ConfigFile.CONFIG.get().getBoolean("Config.Discord.Enabled")) {
			discordBot = new DiscordBot();
			discordBot.connect();
		}
	}
	
	@Override
	public void onDisable() {
		if(discordBot != null) discordBot.disconnect();
	}
	
	public static TigerReportsSupports getInstance() {
		return instance;
	}
	
	public static DiscordBot getDiscordBot() {
		return discordBot;
	}
	
}
