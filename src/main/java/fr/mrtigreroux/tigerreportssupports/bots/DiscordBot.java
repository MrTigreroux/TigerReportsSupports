package fr.mrtigreroux.tigerreportssupports.bots;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.slf4j.LoggerFactory;

import fr.mrtigreroux.tigerreports.logs.Logger;
import fr.mrtigreroux.tigerreports.managers.VaultManager;
import fr.mrtigreroux.tigerreports.objects.reports.Report;
import fr.mrtigreroux.tigerreports.utils.ConfigUtils;
import fr.mrtigreroux.tigerreports.utils.MessageUtils;
import fr.mrtigreroux.tigerreportssupports.TigerReportsSupports;
import fr.mrtigreroux.tigerreportssupports.config.ConfigFile;
import fr.mrtigreroux.tigerreportssupports.listeners.DiscordListener;
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

/**
 * @author MrTigreroux
 */

public class DiscordBot {

	private static final Logger LOGGER = Logger.fromClass(DiscordBot.class,
	        TigerReportsSupports.getInstance().getName());

	private static final String DEFAULT_THUMBNAIL = "https://i.imgur.com/3NDcs3t.png";

	private JDA bot;
	private TextChannel c;
	private final TigerReportsSupports trs;
	private final VaultManager vm;

	public DiscordBot(TigerReportsSupports trs, VaultManager vm) {
		this.trs = trs;
		this.vm = vm;
	}

	public void connect(String newVersion) {
		try {
			ch.qos.logback.classic.Logger logger = (ch.qos.logback.classic.Logger) LoggerFactory
			        .getLogger("net.dv8tion.jda");
			logger.setLevel(ch.qos.logback.classic.Level.WARN);
		} catch (Exception ignored) {}

		String token = ConfigFile.CONFIG.get().getString("Config.Discord.Token");
		if (token == null || token.isEmpty()) {
			Logger.CONFIG.error(ConfigUtils.getInfoMessage("The Discord Bot token is not configured.",
			        "Le token du bot Discord n'est pas configure"));
			return;
		}
		Bukkit.getScheduler().runTaskAsynchronously(trs, () -> {
			try {
				bot = JDABuilder.createDefault(token).addEventListeners(new DiscordListener(DiscordBot.this)).build();
				bot.awaitReady();

				Bukkit.getScheduler().runTask(trs, () -> {
					updateChannel();
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
				});
			} catch (Exception ex) {
				LOGGER.error(ConfigUtils.getInfoMessage("An error has occurred with Discord:",
				        "Une erreur est survenue avec Discord:"), ex);
				return;
			}
		});
	}

	private void updateChannel() {
		String channel = ConfigFile.CONFIG.get().getString("Config.Discord.Channel", "");
		if (!channel.isEmpty()) {
			c = bot.getTextChannelById(channel);
		}
		if (c == null) {
			List<TextChannel> channels = bot.getTextChannels();
			if (!channels.isEmpty()) {
				c = channels.get(0);
			} else {
				Logger.CONFIG.error(ConfigUtils.getInfoMessage(
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
		if (status != null && !status.isEmpty()) {
			bot.getPresence().setActivity(Activity.playing(status));
		}
	}

	private boolean canSendMessage() {
		if (!c.getGuild().getSelfMember().hasPermission(c, Permission.MESSAGE_WRITE)) {
			Logger.CONFIG.error(ConfigUtils.getInfoMessage(
			        "The Discord bot doesn't have the permission to send messages in channel #",
			        "Le bot Discord n'a pas la permission d'envoyer des messages dans le canal #") + c.getName());
			return false;
		}
		return true;
	}

	private void sendMessage(String message) {
		if (message != null && !message.isEmpty() && canSendMessage()) {
			c.sendMessage(message).queue();
		}
	}

	public void onCommand(TextChannel channel, String command, User u) {
		if (!ConfigFile.CONFIG.get()
		        .getStringList("Config.Discord.Managers")
		        .contains(u.getName() + "#" + u.getDiscriminator())) {
			if (canSendMessage()) {
				channel.sendMessage(ConfigFile.MESSAGES.get()
				        .getString("DiscordMessages.No-permission")
				        .replace("_User_", u.getAsMention())).queue();
			}
			return;
		}
		switch (command) {
		case "reload":
			trs.unload();
			Bukkit.getScheduler().runTaskLater(trs, new Runnable() {

				@Override
				public void run() {
					trs.load();
				}

			}, 20);
			break;
		case "stop":
			disconnect();
			break;
		default:
			if (canSendMessage()) {
				channel.sendMessage(ConfigFile.MESSAGES.get()
				        .getString("DiscordMessages.Invalid-command")
				        .replace("_User_", u.getAsMention())).queue();
			}
			break;
		}
	}

	public void notifyReport(String server, Report r) {
		if (!canSendMessage()) {
			return;
		}
		if (!c.getGuild().getSelfMember().hasPermission(c, Permission.MESSAGE_EMBED_LINKS)) {
			c.sendMessage((ConfigUtils.getInfoLanguage().equalsIgnoreCase("English")
			        ? "I can't send embeds in this channel. Please give me the permission."
			        : "Je ne peux pas envoyer des embeds dans ce canal. Merci de me donner la permission.")).queue();
			return;
		}

		EmbedBuilder alert = new EmbedBuilder();
		alert.setColor(Status.WAITING.getColor());

		String reporterName = r.getPlayerName(Report.ParticipantType.REPORTER, false, false, vm, null);
		String reportedName = r.getPlayerName(Report.ParticipantType.REPORTED, false, false, vm, null);

		String thumbnail = ConfigFile.CONFIG.get().getString("Config.Discord.Thumbnail", DEFAULT_THUMBNAIL);
		if (thumbnail == null || thumbnail.isEmpty()) {
			thumbnail = DEFAULT_THUMBNAIL;
		}

		if (!DEFAULT_THUMBNAIL.equals(thumbnail)) {
			alert.setFooter("TigerReportsSupports Discord bot", DEFAULT_THUMBNAIL);
			thumbnail = thumbnail.replace("_Reporter_", reporterName).replace("_Reported_", reportedName);
		}

		alert.setThumbnail(thumbnail);

		FileConfiguration messages = ConfigFile.MESSAGES.get();
		String path = "DiscordMessages.Alert.";
		alert.setAuthor(messages.getString(path + "Title").replace("_Id_", Integer.toString(r.getId())), null,
		        Status.WAITING.getIcon());
		alert.addField(messages.getString(path + "Status"),
		        fr.mrtigreroux.tigerreports.data.constants.Status.WAITING.getDisplayName(null).replaceAll("§.", ""),
		        false);
		boolean serverInfo = ConfigUtils.isEnabled(ConfigFile.CONFIG.get(), "Config.Discord.ServerInfo");
		if (serverInfo) {
			alert.addField(messages.getString(path + "Server"), MessageUtils.getServerName(server), true);
		}
		alert.addField(messages.getString(path + "Date"), r.getDate(), serverInfo);
		alert.addField(messages.getString(path + "Reporter"), reporterName, false);
		alert.addField(messages.getString(path + "Reported"), reportedName, true);
		alert.addField(messages.getString(path + "Reason"), r.getReason(false), false);

		String message = messages.getString(path + "Message");
		if (message != null && !message.isEmpty()) {
			c.sendMessage(message).setEmbeds(alert.build()).queue();
		} else {
			c.sendMessageEmbeds(alert.build()).queue();
		}
	}

	public void notifyProcessReport(Report r, String staff) {
		if (staff == null) {
			fr.mrtigreroux.tigerreports.objects.users.User processorStaff = r.getProcessorStaff();
			if (processorStaff != null) {
				staff = processorStaff.getName();
			}
		}
		sendMessage(ConfigFile.MESSAGES.get()
		        .getString("DiscordMessages.Report-processed")
		        .replace("_Id_", Integer.toString(r.getId()))
		        .replace("_Staff_", staff));
		updateReportStatus(r);
	}

	public void updateReportStatus(Report r) {
		try {
			Status status = Status.fromRawName(r.getStatus().getConfigName());
			String reportId = Integer.toString(r.getId());
			String configTitle = ConfigFile.MESSAGES.get().getString("DiscordMessages.Alert.Title");
			int idIndex = configTitle.indexOf("_Id_");

			List<Message> retrievedMessages = c.getHistory().retrievePast(100).complete();

			for (Message msg : retrievedMessages) {
				if (!msg.getAuthor().isBot()) {
					continue;
				}

				List<MessageEmbed> embeds = msg.getEmbeds();
				if (embeds == null || embeds.isEmpty()) {
					continue;
				}
				MessageEmbed alert = embeds.get(0);
				AuthorInfo author = alert.getAuthor();
				if (author == null) {
					continue;
				}

				String title = author.getName();
				if (title == null) {
					continue;
				}

				String id = title.substring(idIndex);
				if (configTitle.length() >= idIndex + 4) {
					id = id.replace(configTitle.substring(idIndex + 4, configTitle.length()), "");
				}

				if (id.equals(reportId)) {
					EmbedBuilder updatedAlert = new EmbedBuilder(alert);
					updatedAlert.setColor(status.getColor());
					List<Field> fields = updatedAlert.getFields();

					String statusField = r.getStatusWithDetails(vm)
					        .replace(ConfigUtils.getLineBreakSymbol(), " | ")
					        .replaceAll("§.", "");

					fields.set(0, new Field(ConfigFile.MESSAGES.get().getString("DiscordMessages.Alert.Status"),
					        statusField, false));
					updatedAlert.setAuthor(alert.getAuthor().getName(), null, status.getIcon());
					msg.editMessageEmbeds(updatedAlert.build()).queue();
					break;
				}
			}
		} catch (Exception ignored) {}
	}

	public void disconnect() {
		trs.removeDiscordBot();
		sendMessage(ConfigFile.MESSAGES.get().getString("DiscordMessages.Disconnected"));
		try {
			bot.shutdown();
		} catch (Exception ignored) {}
	}

}
