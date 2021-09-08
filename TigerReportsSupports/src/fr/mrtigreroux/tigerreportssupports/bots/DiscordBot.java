package fr.mrtigreroux.tigerreportssupports.bots;

import java.util.List;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Logger;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.MessageEmbed.AuthorInfo;
import net.dv8tion.jda.api.entities.MessageEmbed.Field;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import fr.mrtigreroux.tigerreports.objects.Report;
import fr.mrtigreroux.tigerreports.utils.ConfigUtils;
import fr.mrtigreroux.tigerreports.utils.MessageUtils;
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
			bot = JDABuilder.createDefault(ConfigFile.CONFIG.get().getString("Config.Discord.Token"))
			        .addEventListeners(new DiscordListener())
			        .build();
			Bukkit.getScheduler().runTaskAsynchronously(TigerReportsSupports.getInstance(), new Runnable() {

				@Override
				public void run() {
					try {
						bot.awaitReady();
					} catch (InterruptedException ex) {
						Bukkit.getLogger()
						        .log(Level.SEVERE, ConfigUtils.getInfoMessage("An error has occurred with Discord:",
						                "Une erreur est survenue avec Discord:"), ex);
					}
					updateChannel();
					String newVersion = TigerReportsSupports.getInstance().getWebManager().getNewVersion();
					if (newVersion != null) {
						boolean english = ConfigUtils.getInfoLanguage().equalsIgnoreCase("English");
						c.sendMessage(english
						        ? "```\nThe plugin TigerReportsSupports has been updated.\nThe new version "
						                + newVersion
						                + " is available on:```\n__https://www.spigotmc.org/resources/tigerreportssupports.54612/__"
						        : "```\nLe plugin TigerReportsSupports a été mis à jour.\nLa nouvelle version "
						                + newVersion
						                + " est disponible ici:```\n__https://www.spigotmc.org/resources/tigerreportssupports.54612/__")
						        .queue();
					}

					updatePlayingStatus();
				}

			});

		} catch (Exception ex) {
			Bukkit.getLogger()
			        .log(Level.SEVERE, ConfigUtils.getInfoMessage("An error has occurred with Discord:",
			                "Une erreur est survenue avec Discord:"), ex);
		}
	}

	private void updateChannel() {
		String channel = ConfigFile.CONFIG.get().getString("Config.Discord.Channel", "");
		if (!channel.isEmpty())
			c = bot.getTextChannelById(channel);
		if (c == null) {
			List<TextChannel> channels = bot.getTextChannels();
			if (!channels.isEmpty()) {
				c = channels.get(0);
			} else {
				Bukkit.getLogger()
				        .severe(ConfigUtils.getInfoMessage(
				                "The Discord bot could not find any text channel on the Discord server.",
				                "Le bot Discord n'a pas pu trouver un seul canal de texte sur le serveur Discord."));
				return;
			}
		}
		sendMessage(ConfigFile.MESSAGES.get()
		        .getString("DiscordMessages.Connected")
		        .replace("_Channel_", c.getAsMention()));
	}

	private void updatePlayingStatus() {
		String status = ConfigFile.CONFIG.get().getString("Config.Discord.PlayingStatus", "");
		if (status != null && !status.isEmpty())
			bot.getPresence().setActivity(Activity.playing(status));
	}

	private boolean canSendMessage() {
		if (!c.getGuild().getSelfMember().hasPermission(c, Permission.MESSAGE_WRITE)) {
			Bukkit.getLogger()
			        .warning(ConfigUtils.getInfoMessage(
			                "The Discord bot doesn't have the permission to send messages in channel #",
			                "Le bot Discord n'a pas la permission d'envoyer des messages dans le canal #")
			                + c.getName());
			return false;
		}
		return true;
	}

	private void sendMessage(String message) {
		if (message != null && !message.isEmpty() && canSendMessage())
			c.sendMessage(message).queue();
	}

	public void onCommand(TextChannel channel, String command, User u) {
		if (!ConfigFile.CONFIG.get()
		        .getStringList("Config.Discord.Managers")
		        .contains(u.getName() + "#" + u.getDiscriminator())) {
			if (canSendMessage())
				channel.sendMessage(ConfigFile.MESSAGES.get()
				        .getString("DiscordMessages.No-permission")
				        .replace("_User_", u.getAsMention())).queue();
			return;
		}
		switch (command) {
		case "reload":
			TigerReportsSupports.load();
			if (!ConfigFile.CONFIG.get().getBoolean("Config.Discord.Enabled")) {
				disconnect();
				return;
			}
			updateChannel();
			updatePlayingStatus();
			break;
		case "stop":
			disconnect();
			break;
		default:
			if (canSendMessage())
				channel.sendMessage(ConfigFile.MESSAGES.get()
				        .getString("DiscordMessages.Invalid-command")
				        .replace("_User_", u.getAsMention())).queue();
			break;
		}
	}

	public void notifyReport(String server, Report r) {
		if (!canSendMessage())
			return;
		if (!c.getGuild().getSelfMember().hasPermission(c, Permission.MESSAGE_EMBED_LINKS)) {
			c.sendMessage((ConfigUtils.getInfoLanguage().equalsIgnoreCase("English")
			        ? "I can't send embeds in this channel. Please give me the permission."
			        : "Je ne peux pas envoyer des embeds dans ce canal. Merci de me donner la permission.")).queue();
			return;
		}

		EmbedBuilder alert = new EmbedBuilder();
		alert.setColor(Status.WAITING.getColor());

		String reporterName = r.getPlayerName("Reporter", false, false);
		String reportedName = r.getPlayerName("Reported", false, false);

		String defaultThumbnail = "https://i.imgur.com/3NDcs3t.png";
		String thumbnail = ConfigFile.CONFIG.get().getString("Config.Discord.Thumbnail", defaultThumbnail);

		if (!defaultThumbnail.equals(thumbnail)) {
			alert.setFooter("TigerReportsSupports Discord bot", defaultThumbnail);
			thumbnail = thumbnail.replace("_Reporter_", reporterName).replace("_Reported_", reportedName);
		}

		alert.setThumbnail(thumbnail);

		FileConfiguration messages = ConfigFile.MESSAGES.get();
		String path = "DiscordMessages.Alert.";
		alert.setAuthor(messages.getString(path + "Title").replace("_Id_", Integer.toString(r.getId())), null,
		        Status.WAITING.getIcon());
		alert.addField(messages.getString(path + "Status"),
		        fr.mrtigreroux.tigerreports.data.constants.Status.WAITING.getWord(null).replaceAll("§.", ""), false);
		boolean serverInfo = ConfigUtils.isEnabled(ConfigFile.CONFIG.get(), "Config.Discord.ServerInfo");
		if (serverInfo)
			alert.addField(messages.getString(path + "Server"), MessageUtils.getServerName(server), true);
		alert.addField(messages.getString(path + "Date"), r.getDate(), serverInfo);
		alert.addField(messages.getString(path + "Reporter"), reporterName, false);
		alert.addField(messages.getString(path + "Reported"), reportedName, true);
		alert.addField(messages.getString(path + "Reason"), r.getReason(false), false);

		String message = messages.getString(path + "Message");
		if (message != null && !message.isEmpty()) {
			c.sendMessage(message).embed(alert.build()).queue();
		} else {
			c.sendMessage(alert.build()).queue();
		}
	}

	public void notifyProcessReport(Report r, String staff) {
		sendMessage(ConfigFile.MESSAGES.get()
		        .getString("DiscordMessages.Report-processed")
		        .replace("_Id_", Integer.toString(r.getId()))
		        .replace("_Staff_", staff != null ? staff : r.getProcessor()));
		updateReportStatus(r, Status.DONE);
	}

	public void updateReportStatus(Report r, Status status) {
		try {
			String reportId = Integer.toString(r.getId());
			String configTitle = ConfigFile.MESSAGES.get().getString("DiscordMessages.Alert.Title");
			int idIndex = configTitle.indexOf("_Id_");

			List<Message> retrievedMessages = c.getHistory().retrievePast(100).complete();

			for (Message msg : retrievedMessages) {
				if (!msg.getAuthor().isBot())
					continue;

				List<MessageEmbed> embeds = msg.getEmbeds();
				if (embeds == null || embeds.isEmpty())
					continue;
				MessageEmbed alert = embeds.get(0);
				AuthorInfo author = alert.getAuthor();
				if (author == null)
					continue;

				String title = author.getName();
				if (title == null)
					continue;

				String id = title.substring(idIndex);
				if (configTitle.length() >= idIndex + 4)
					id = id.replace(configTitle.substring(idIndex + 4, configTitle.length()), "");

				if (id.equals(reportId)) {
					EmbedBuilder updatedAlert = new EmbedBuilder(alert);
					updatedAlert.setColor(status.getColor());
					List<Field> fields = updatedAlert.getFields();
					fields.set(0, new Field(ConfigFile.MESSAGES.get().getString("DiscordMessages.Alert.Status"),
					        r.getStatus().getWord(r.getProcessor()).replaceAll("§.", ""), false));
					updatedAlert.setAuthor(alert.getAuthor().getName(), null, status.getIcon());
					msg.editMessage(updatedAlert.build()).queue();
					break;
				}
			}
		} catch (Exception ignored) {}
	}

	public void disconnect() {
		TigerReportsSupports.getInstance().removeDiscordBot();
		sendMessage(ConfigFile.MESSAGES.get().getString("DiscordMessages.Disconnected"));
		try {
			bot.shutdown();
		} catch (Exception ignored) {}
	}

}
