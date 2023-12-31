package dev.garvis.mcesspigot;

import dev.garvis.mcesspigot.KafkaManagerV2;
import dev.garvis.mcesspigot.PlayerListener;
import dev.garvis.mcesspigot.BlockListener;
import dev.garvis.mcesspigot.InventoryListener;

import org.bukkit.Bukkit;
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
