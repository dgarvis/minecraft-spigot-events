package dev.garvis.mcesspigot;

import dev.garvis.mcesspigot.KafkaManagerV2;
import dev.garvis.mcesspigot.PlayerListener;
import dev.garvis.mcesspigot.BlockListener;
import dev.garvis.mcesspigot.InventoryListener;
import dev.garvis.mcesspigot.SetupCommand;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Statistic;
import org.bukkit.entity.EntityType;
import org.bukkit.Material;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.ChatColor;

import java.util.List;
import java.util.LinkedList;
import java.util.Map;
import java.util.Arrays;

public class MCESSpigotPlugin extends JavaPlugin {
    
    private KafkaManagerV2 kafka = new KafkaManagerV2();
    //private String serverName;
    private boolean running = true;
    
    @Override
    public void onEnable() {
	this.saveDefaultConfig();
	this.attemptToConnectToKafka();

	getServer().getPluginManager().registerEvents(new PlayerListener(kafka), this);
	getServer().getPluginManager().registerEvents(new BlockListener(kafka), this);
	getServer().getPluginManager().registerEvents(new InventoryListener(kafka), this);

	getCommand("setupspigotevents").
	    setExecutor(new SetupCommand((String name, String broker, String topic) -> {
			FileConfiguration config = getConfig();
			config.set("kafkaServer", broker);
			config.set("kafkaTopic", topic);
			config.set("serverName", name);

			saveConfig();
	    }));

	// TODO - make run every hour.
	statsEvent();
    }

    private void statsEvent() {
	for (OfflinePlayer player : Bukkit.getOfflinePlayers()) {
	    KafkaManagerV2.Message e = this.kafka.new Message();
	    e.put("eventType", "PLAYER_STATS");
	    e.put("playerName", player.getName());
	    e.put("playerUUID", player.getUniqueId().toString());
	    for (Statistic stat : Statistic.values()) {
		try {
		    switch (stat) {
		    case DROP:
		    case PICKUP:
		    case MINE_BLOCK:
		    case USE_ITEM:
		    case BREAK_ITEM:
		    case CRAFT_ITEM:
			for (Material mat : Material.values()) {
			    int v = 0;
			    try {
				v =  player.getStatistic(stat, mat);
			    } catch (IllegalArgumentException ex) {}
			    e.put(stat.toString() + "_" + mat.toString(), v);
			}
			break;
		    case KILL_ENTITY:
		    case ENTITY_KILLED_BY:
			for (EntityType entity : EntityType.values()) {
			    int v = 0;
			    try {
				v =  player.getStatistic(stat, entity);
			    } catch (IllegalArgumentException ex) {}
			    e.put(stat.toString() + "_" + entity.toString(), v);
			}
			break;
		    default:
			e.put(stat.toString(), player.getStatistic(stat));
		    }
		} catch (IllegalArgumentException ex) {
		    this.getLogger().warning("Ignoring: " + stat.toString());
		}
	    }
	    this.kafka.sendMessage(e);
	}
    }

    private void attemptToConnectToKafka() {
	if (!this.running) return;

	final String[] events = {
	    "PLAYER_JOINED_SERVER",
	    "PLAYER_DISCONNECTED",
	    "CHAT_MESSAGE_PUBLISHED",
	    "DISCORD_VOICE_JOINED",
	    "DISCORD_VOICE_LEFT"
	};

	reloadConfig();
	FileConfiguration config = getConfig();
	String kafkaServer = config.getString("kafkaServer");
	String kafkaTopic = config.getString("kafkaTopic");
	String serverName = config.getString("serverName");

	// Check we have a kafka server configured
	if (kafkaServer.isEmpty()) {
	    getLogger().warning("Kafka connection not configured.");
	    Bukkit.getScheduler().runTaskLaterAsynchronously(this, () -> {
		    attemptToConnectToKafka();
		}, 20 * 60); // wait 60 second and try again. 20 ticks / second.
	    return;
	}

	// Try to connect to kafka
	try {
	    kafka.connect(serverName, kafkaServer, kafkaTopic,
			  new String[]{kafkaTopic}, events,
			  (LinkedList<KafkaManagerV2.Message> messages) -> {
			      /*for (KafkaManagerV2.Message message : messages) {
				  System.out.println(message);
				  }*/
			      this.processMessages(messages);
			  });
	    getLogger().info("Connected to Kafka");
	} catch (Exception e) {
	    getLogger().warning("Not connected to kafka, check plugin config.");
	    Bukkit.getScheduler().runTaskLaterAsynchronously(this, () -> {
		    attemptToConnectToKafka();
		}, 20 * 60); // wait 60 second and try again. 20 ticks / second.
	}
    }

    private void processMessages(LinkedList<KafkaManagerV2.Message> messages) {

	Bukkit.getScheduler().runTask(this, () -> {
		for (KafkaManagerV2.Message message : messages) {
		    System.out.println("Got Message: " + message.toString());

		    // https://www.spigotmc.org/threads/the-best-way-to-send-a-message-to-all-the-players.461507/
		    
		    switch ((String)message.get("eventType")) {
		    case "PLAYER_JOINED_SERVER":
			Bukkit.broadcastMessage(ChatColor.YELLOW + (String)message.get("playerName") +
						" joined server " + (String)message.get("server"));
			break;
		    case "PLAYER_DISCONNECTED":
			Bukkit.broadcastMessage(ChatColor.YELLOW + (String)message.get("playerName") +
						" disconnected from the network.");
			break;
		    case "CHAT_MESSAGE_PUBLISHED":
			Bukkit.broadcastMessage("<" + (String)message.get("playerName") + "> " +
						(String)message.get("message"));
			break;
		    case "DISCORD_VOICE_JOINED":
			Bukkit.broadcastMessage(ChatColor.WHITE + "["+(String)message.get("username")+"] " +
						ChatColor.LIGHT_PURPLE + "Joined the Discord Voice Chat.");
			break;
		    case "DISCORD_VOICE_LEFT":
			Bukkit.broadcastMessage(ChatColor.WHITE + "["+(String)message.get("username")+"] " +
						ChatColor.LIGHT_PURPLE + "Left the Discord Voice Chat.");
			break;
		    }
		}
	    });
    }

    @Override
    public void onDisable() {
	this.running = false;
	//kafka.disconnect();
	kafka.close();
	getLogger().info("Spigot Events Disabled");
    }
}
