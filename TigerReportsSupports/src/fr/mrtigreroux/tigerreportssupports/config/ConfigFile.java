package fr.mrtigreroux.tigerreportssupports.config;

import java.io.File;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import fr.mrtigreroux.tigerreportssupports.TigerReportsSupports;
import fr.mrtigreroux.tigerreports.utils.ConfigUtils;
import fr.mrtigreroux.tigerreports.utils.MessageUtils;

/**
 * @author MrTigreroux
 */

public enum ConfigFile {

	CONFIG, MESSAGES;
	
	private File file = null;
	private FileConfiguration config = null;
	
	ConfigFile() {}
	
	public void load() {
		file = new File("plugins/TigerReportsSupports", toString().toLowerCase()+".yml");
		if(!file.exists())
			reset();
		config = YamlConfiguration.loadConfiguration(file);
		
		try {
			Reader defaultConfigStream = new InputStreamReader(TigerReportsSupports.getInstance().getResource(file.getName()), "UTF8");
			YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(defaultConfigStream);
			config.setDefaults(defaultConfig);
		} catch (UnsupportedEncodingException ex) {
			ex.printStackTrace();
		}
	}
	
	public FileConfiguration get() {
		return config;
	}
	
	public void save() {
		try {
			get().save(file);
		} catch (Exception ex) {
			load();
		}
	}
	
	public void reset() {
		TigerReportsSupports.getInstance().saveResource(file.getName(), false);
		Bukkit.getLogger().warning(MessageUtils.LINE);
		Bukkit.getLogger().warning("[TigerReportsSupports] "+(this != CONFIG && ConfigUtils.getInfoLanguage().equalsIgnoreCase("English") ? "The file "+file.getName()+" has been reset." : "Le fichier "+file.getName()+" a ete reinitialise."));
		Bukkit.getLogger().warning(MessageUtils.LINE);
	}
	
}
