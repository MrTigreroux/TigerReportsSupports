package fr.mrtigreroux.tigerreportssupports;

import java.util.function.Consumer;

import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import fr.mrtigreroux.tigerreports.TigerReports;
import fr.mrtigreroux.tigerreports.logs.Logger;
import fr.mrtigreroux.tigerreports.tasks.ResultCallback;
import fr.mrtigreroux.tigerreports.utils.ConfigUtils;
import fr.mrtigreroux.tigerreports.utils.MessageUtils;
import fr.mrtigreroux.tigerreports.utils.WebUtils;
import fr.mrtigreroux.tigerreportssupports.bots.DiscordBot;
import fr.mrtigreroux.tigerreportssupports.config.ConfigFile;
import fr.mrtigreroux.tigerreportssupports.listeners.ReportListener;

/**
 * @author MrTigreroux
 */

public class TigerReportsSupports extends JavaPlugin {
    
    private static final String SPIGOTMC_RESOURCE_ID = "54612";
    
    private static TigerReportsSupports instance;
    
    private DiscordBot discordBot = null;
    private Consumer<Boolean> trLoadUnloadListener = (loaded) -> {
        Logger.MAIN.info(() -> "TigerReportsSupports: trLoadUnloadListener: loaded = " + loaded);
        if (loaded) {
            postLoad(TigerReports.getInstance());
        } else {
            unload();
        }
    };
    
    public TigerReportsSupports() {}
    
    @Override
    public void onEnable() {
        instance = this;
        
        load();
        
        PluginDescriptionFile desc = getDescription();
        if (
            !desc.getName().equals("TigerReportsSupports")
                    || desc.getAuthors().size() != 1
                    || !desc.getAuthors().contains("MrTigreroux")
        ) {
            Logger.CONFIG.error(
                    ConfigUtils.getInfoMessage(
                            "The file plugin.yml has been edited without authorization.",
                            "Le fichier plugin.yml a ete modifie sans autorisation."
                    )
            );
            Bukkit.shutdown();
            return;
        }
    }
    
    public void load() {
        PluginManager pm = Bukkit.getPluginManager();
        if (!pm.isPluginEnabled("TigerReports")) {
            java.util.logging.Logger logger = Bukkit.getLogger();
            logger.severe(MessageUtils.LINE);
            logger.severe("[TigerReportsSupports] The plugin TigerReports must be installed.");
            logger.severe("You can download it here:");
            logger.severe("https://www.spigotmc.org/resources/tigerreports.25773/");
            logger.severe(MessageUtils.LINE);
            pm.disablePlugin(this);
            return;
        }
        
        for (ConfigFile configFiles : ConfigFile.values()) {
            configFiles.load(this);
        }
        Logger.MAIN.info(() -> "TigerReportsSupports: load()");
        
        TigerReports.getInstance().addAndNotifyLoadUnloadListener(trLoadUnloadListener);
    }
    
    private void postLoad(TigerReports tr) {
        Logger.MAIN.info(() -> "TigerReportsSupports: postLoad()");
        WebUtils.checkNewVersion(this, tr, SPIGOTMC_RESOURCE_ID, new ResultCallback<String>() {
            
            @Override
            public void onResultReceived(String newVersion) {
                if (ConfigFile.CONFIG.get().getBoolean("Config.Discord.Enabled")) {
                    discordBot = new DiscordBot(TigerReportsSupports.this, tr.getVaultManager());
                    discordBot.connect(newVersion);
                    Bukkit.getPluginManager()
                            .registerEvents(new ReportListener(discordBot, tr.getBungeeManager()), TigerReportsSupports.this);
                }
            }
            
        });
    }
    
    public static TigerReportsSupports getInstance() {
        return instance;
    }
    
    public DiscordBot getDiscordBot() {
        return discordBot;
    }
    
    public void removeDiscordBot() {
        discordBot = null;
    }
    
    @Override
    public void onDisable() {
        unload();
        TigerReports.getInstance().removeLoadUnloadListener(trLoadUnloadListener);
    }
    
    public void unload() {
        Logger.MAIN.info(() -> "TigerReportsSupports: unload()");
        HandlerList.unregisterAll(this); // Unregister all event listeners.
        
        if (discordBot != null) {
            discordBot.disconnect();
            discordBot = null;
        }
    }
    
}
