package fr.mrtigreroux.tigerreportssupports.managers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Bukkit;

import fr.mrtigreroux.tigerreports.utils.ConfigUtils;
import fr.mrtigreroux.tigerreportssupports.TigerReportsSupports;

/**
 * @author MrTigreroux
 */

public class WebManager {

	private final TigerReportsSupports main;
	private String newVersion = null;
	
	public WebManager(TigerReportsSupports main) {
		this.main = main;
	}
	
	public String getNewVersion() {
		return newVersion;
	}
	
	private String sendQuery(String url, String data) throws UnsupportedEncodingException, IOException {
		HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
		if(data != null) {
			connection.setDoOutput(true);
			connection.setRequestMethod("POST");
			connection.getOutputStream().write(data.getBytes("UTF-8"));
		}
		return new BufferedReader(new InputStreamReader(connection.getInputStream())).readLine();
	}
	
	public void initialize() {
		try {
			newVersion = sendQuery("https://api.spigotmc.org/legacy/update.php?resource=54612", null);
			if(main.getDescription().getVersion().equals(newVersion)) newVersion = null;
			else {
				Logger logger = Bukkit.getLogger();
				logger.log(Level.WARNING, "------------------------------------------------------");
				if(ConfigUtils.getInfoLanguage().equalsIgnoreCase("English")) {
					logger.log(Level.WARNING, "[TigerReportsSupports] The plugin has been updated.");
					logger.log(Level.WARNING, "The new version "+newVersion+" is available on:");
				} else {
					logger.log(Level.WARNING, "[TigerReportsSupports] Le plugin a ete mis a jour.");
					logger.log(Level.WARNING, "La nouvelle version "+newVersion+" est disponible ici:");
				}
				logger.log(Level.WARNING, "https://www.spigotmc.org/resources/tigerreportssupports.54612/");
				logger.log(Level.WARNING, "------------------------------------------------------");
			}
		} catch (Exception ignored) {}
	}
	
}
