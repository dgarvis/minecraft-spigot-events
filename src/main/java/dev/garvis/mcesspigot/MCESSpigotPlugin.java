package dev.garvis.mcesspigot;

import dev.garvis.mcesspigot.KafkaManager;
import dev.garvis.mcesspigot.PlayerListener;
import dev.garvis.mcesspigot.BlockListener;
import dev.garvis.mcesspigot.InventoryListener;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.ChatColor;

import java.util.List;
import java.util.Map;
import java.util.Arrays;

public class MCESSpigotPlugin extends JavaPlugin {
    
    private KafkaManager kafka = new KafkaManager();
    private String serverName;
    private boolean running = true;
    
    @Override
    public void onEnable() {
	this.saveDefaultConfig();

	FileConfiguration config = getConfig();
	this.serverName = config.getString("serverName");
	this.attemptToConnectToKafka();
	this.processBacklogMessages();
	
	//kafka.sendMessage("Hello");

	getServer().getPluginManager().registerEvents(new PlayerListener(serverName, kafka), this);
	getServer().getPluginManager().registerEvents(new BlockListener(serverName, kafka), this);
	getServer().getPluginManager().registerEvents(new InventoryListener(serverName, kafka), this);

	// https://www.spigotmc.org/threads/guide-threading-for-beginners.523773/
	Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
		processMessages();
	    });
    }

    private void processBacklogMessages() {
	if (!this.running) return;

	this.kafka.processBacklog();
	
	Bukkit.getScheduler().runTaskLaterAsynchronously(this, () -> {
		processBacklogMessages();
	    }, 20 * 60); // wait 60 second and try again. 20 ticks / second.
    }

    private void attemptToConnectToKafka() {
	if (!this.running) return;
	
	FileConfiguration config = getConfig();
	String kafkaServer = config.getString("kafkaServer");
	String kafkaTopic = config.getString("kafkaTopic");

	// Check we have a kafka server configured
	if (kafkaServer.isEmpty()) {
	    getLogger().warning("Kafka connection not configured.");
	    // Don't try to auto connect.
	    return;
	}

	// Try to connect to kafka
	try {
	    kafka.connect(kafkaServer, this.serverName, kafkaTopic); // using server name as client id.
	    getLogger().info("Connected to Kafka");
	} catch (Exception e) {
	    getLogger().warning("Not connected to kafka, check plugin config.");
	    Bukkit.getScheduler().runTaskLaterAsynchronously(this, () -> {
		    attemptToConnectToKafka();
		}, 20 * 60); // wait 60 second and try again. 20 ticks / second.
	}
    }

    private void processMessages() {
	if (!this.running) return;
	
	String[] events = {
	    "PLAYER_JOINED_SERVER",
	    "PLAYER_DISCONNECTED",
	    "CHAT_MESSAGE_PUBLISHED",
	    "DISCORD_VOICE_JOINED",
	    "DISCORD_VOICE_LEFT"
	};
	List<Map<String,Object>> messages = kafka.getMessages(this.serverName, Arrays.asList(events));

	Bukkit.getScheduler().runTask(this, () -> {
		for (Map<String, Object> message : messages) {
		    //if(message.containsKey("server") && message.get("server").equals(this.serverName)) continue;
		    
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

	Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
		processMessages();
	    });
    }

    @Override
    public void onDisable() {
	this.running = false;
	kafka.disconnect();
	getLogger().info("Spigot Events Disabled");
    }
}
