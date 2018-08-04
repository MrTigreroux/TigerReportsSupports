package fr.mrtigreroux.tigerreportssupports;

import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import fr.mrtigreroux.tigerreports.utils.ConfigUtils;
import fr.mrtigreroux.tigerreports.utils.MessageUtils;
import fr.mrtigreroux.tigerreportssupports.bots.DiscordBot;
import fr.mrtigreroux.tigerreportssupports.config.ConfigFile;
import fr.mrtigreroux.tigerreportssupports.listeners.ReportListener;
import fr.mrtigreroux.tigerreportssupports.managers.WebManager;

/**
 * @author MrTigreroux
 */

public class TigerReportsSupports extends JavaPlugin {

	private static TigerReportsSupports instance;
	
	private WebManager webManager;
	private DiscordBot discordBot = null;
	
	public TigerReportsSupports() {}
	
	public static void load() {
		for(ConfigFile configFiles : ConfigFile.values())
			configFiles.load();
	}
	
	@Override
	public void onEnable() {
		instance = this;
		
		PluginManager pm = Bukkit.getPluginManager();
		if(!pm.getPlugin("TigerReports").isEnabled()) {
			Logger logger = Bukkit.getLogger();
			logger.severe(MessageUtils.LINE);
			logger.severe("[TigerReportsSupports] The plugin TigerReports must be installed.");
			logger.severe("You can download it here:");
			logger.severe("https://www.spigotmc.org/resources/tigerreports.25773/");
			logger.severe(MessageUtils.LINE);
			pm.disablePlugin(this);
			return;
		}
		
		load();
		pm.registerEvents(new ReportListener(), this);
		
		PluginDescriptionFile desc = getDescription();
		if(!desc.getName().equals("TigerReportsSupports") || desc.getAuthors().size() != 1 || !desc.getAuthors().contains("MrTigreroux")) {
			Logger logger = Bukkit.getLogger();
			logger.severe(MessageUtils.LINE);
			if(ConfigUtils.getInfoLanguage().equalsIgnoreCase("English")) {
				logger.severe("[TigerReportsSupports] The file plugin.yml has been edited");
				logger.severe("without authorization.");
			} else {
				logger.severe("[TigerReportsSupports] Le fichier plugin.yml a ete modifie");
				logger.severe("sans autorisation.");
			}
			logger.severe(MessageUtils.LINE);
			Bukkit.shutdown();
		}
		
		webManager = new WebManager(this);
		
		if(ConfigFile.CONFIG.get().getBoolean("Config.Discord.Enabled")) {
			discordBot = new DiscordBot();
			discordBot.connect();
		}
	}
	
	@Override
	public void onDisable() {
		if(discordBot != null)
			discordBot.disconnect();
	}
	
	public static TigerReportsSupports getInstance() {
		return instance;
	}
	
	public WebManager getWebManager() {
		return webManager;
	}
	
	public DiscordBot getDiscordBot() {
		return discordBot;
	}
	
	public void removeDiscordBot() {
		discordBot = null;
	}
	
}
