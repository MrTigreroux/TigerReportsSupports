package fr.mrtigreroux.tigerreportssupports.config;

import java.io.File;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import fr.mrtigreroux.tigerreports.logs.Logger;
import fr.mrtigreroux.tigerreports.utils.ConfigUtils;
import fr.mrtigreroux.tigerreports.utils.MessageUtils;
import fr.mrtigreroux.tigerreportssupports.TigerReportsSupports;

/**
 * @author MrTigreroux
 */

public enum ConfigFile {
    
    CONFIG,
    MESSAGES;
    
    private File file = null;
    private FileConfiguration config = null;
    
    ConfigFile() {}
    
    public void load(TigerReportsSupports trs) {
        file = new File(trs.getDataFolder(), toString().toLowerCase() + ".yml");
        if (!file.exists())
            reset();
        config = YamlConfiguration.loadConfiguration(file);
        
        try {
            Reader defaultConfigStream =
                    new InputStreamReader(trs.getResource(file.getName()), "UTF8");
            YamlConfiguration defaultConfig =
                    YamlConfiguration.loadConfiguration(defaultConfigStream);
            config.setDefaults(defaultConfig);
        } catch (UnsupportedEncodingException ex) {
            ex.printStackTrace();
        }
    }
    
    public FileConfiguration get() {
        return config;
    }
    
    public void save(TigerReportsSupports trs) {
        try {
            get().save(file);
        } catch (Exception ex) {
            load(trs);
        }
    }
    
    public void reset() {
        TigerReportsSupports.getInstance().saveResource(file.getName(), false);
        Logger logger = Logger.CONFIG;
        logger.warn(() -> MessageUtils.LINE);
        logger.warn(
                () -> this != CONFIG && ConfigUtils.getInfoLanguage().equalsIgnoreCase("English") ? "The file " + file.getName() + " has been reset." : "Le fichier " + file.getName() + " a ete reinitialise."
        );
        logger.warn(() -> MessageUtils.LINE);
    }
    
}
