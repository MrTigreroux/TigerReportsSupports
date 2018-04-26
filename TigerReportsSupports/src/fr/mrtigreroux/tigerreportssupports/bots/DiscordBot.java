package fr.mrtigreroux.tigerreportssupports.bots;

import java.awt.Color;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Logger;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.TextChannel;
import fr.mrtigreroux.tigerreports.objects.Report;
import fr.mrtigreroux.tigerreports.utils.ConfigUtils;
import fr.mrtigreroux.tigerreportssupports.TigerReportsSupports;
import fr.mrtigreroux.tigerreportssupports.config.ConfigFile;
import fr.mrtigreroux.tigerreportssupports.listeners.DiscordListener;

/**
 * @author MrTigreroux
 */

public class DiscordBot {

	private JDA bot;
	private TextChannel c;
	
	public DiscordBot() {}
	
	public void connect() {
		try {
			Logger logger = (Logger) LoggerFactory.getLogger("net.dv8tion.jda");
			logger.setLevel(ch.qos.logback.classic.Level.WARN);
		} catch (Exception ignored) {}
		
		try {
			bot = new JDABuilder(AccountType.BOT).setToken(ConfigFile.CONFIG.get().getString("Config.Discord.Token")).addEventListener(new DiscordListener()).buildBlocking();
			setChannel();
			String newVersion = TigerReportsSupports.getWebManager().getNewVersion();
			if(newVersion != null) {
				boolean english = ConfigUtils.getInfoLanguage().equalsIgnoreCase("English");
				c.sendMessage(english ? "```\nThe plugin TigerReportsSupports has been updated.\nThe new version "+newVersion+" is available on:```\n__https://www.spigotmc.org/resources/tigerreportssupports.54612/__" : "```\nLe plugin TigerReportsSupports a �t� mis � jour.\nLa nouvelle version "+newVersion+" est disponible ici:```\n__https://www.spigotmc.org/resources/tigerreportssupports.54612/__").queue();
			}
		} catch (Exception e) {
			Bukkit.getLogger().log(Level.WARNING, ConfigUtils.getInfoMessage("An error has occurred with Discord:", "Une erreur est survenue avec Discord:"));
			e.printStackTrace();
		}
	}
	
	private void setChannel() {
		c = (c = bot.getTextChannelById(ConfigFile.CONFIG.get().getString("Config.Discord.Channel"))) != null ? c : bot.getTextChannels().get(0);
		sendMessage(ConfigFile.MESSAGES.get().getString("DiscordMessages.Connected").replace("_Channel_", c.getAsMention()));
	}
	
	private boolean canSendMessage() {
		if(!c.getGuild().getSelfMember().hasPermission(c, Permission.MESSAGE_WRITE)) {
			Bukkit.getLogger().log(Level.WARNING, ConfigUtils.getInfoMessage("The Discord bot doesn't have the permission to send messages in channel #", "Le bot Discord n'a pas la permission d'envoyer des messages dans le canal #")+c.getName());
			return false;
		}
		return true;
	}
	
	private void sendMessage(String message) {
		if(!message.isEmpty() && canSendMessage()) c.sendMessage(message).queue();
	}
	
	public void onCommand(TextChannel channel, String command, String user) {
		switch(command) {
			case "reload":
				TigerReportsSupports.load();
				if(!ConfigFile.CONFIG.get().getBoolean("Config.Discord.Enabled")) {
					disconnect();
					return;
				}
				setChannel();
				break;
			case "stop": disconnect(); break;
			default: if(canSendMessage()) channel.sendMessage(ConfigFile.MESSAGES.get().getString("DiscordMessages.Invalid-command").replace("_User_", user)).queue(); break;
		} 
	}
	
	public void notifyReport(String server, Report r) {
		if(!canSendMessage()) return;
		if(!c.getGuild().getSelfMember().hasPermission(c, Permission.MESSAGE_EMBED_LINKS)) {
			c.sendMessage((ConfigUtils.getInfoLanguage().equalsIgnoreCase("English") ? "I can't send embeds in this channel. Please give me the permission." : "Je ne peux pas envoyer des embeds dans ce canal. Merci de me donner la permission.")).queue();
			return;
		}
		
		EmbedBuilder alert = new EmbedBuilder();
		alert.setColor(Color.ORANGE);
		alert.setThumbnail("https://i.imgur.com/3NDcs3t.png");
		
		FileConfiguration messages = ConfigFile.MESSAGES.get();
		String path = "DiscordMessages.Alert.";
		alert.setAuthor(messages.getString(path+"Title").replace("_Id_", Integer.toString(r.getId())), null, "https://i.imgur.com/EXonLKM.png");
		alert.addField(messages.getString(path+"Server"), server, true);
		alert.addField(messages.getString(path+"Date"), r.getDate(), true);
		alert.addField(messages.getString(path+"Reporter"), r.getPlayerName("Reporter", false, false), true);
		alert.addField(messages.getString(path+"Reported"), r.getPlayerName("Reported", false, false), true);
		alert.addField(messages.getString(path+"Reason"), r.getReason(false), false);
		
		c.sendMessage(alert.build()).queue();
	}
	
	public void disconnect() {
		TigerReportsSupports.discordBot = null;
		sendMessage(ConfigFile.MESSAGES.get().getString("DiscordMessages.Disconnected"));
		bot.shutdown();
	}
	
}