package dev.garvis.mcesspigot;

import dev.garvis.mcesspigot.KafkaManager;
import dev.garvis.mcesspigot.PlayerListener;
import dev.garvis.mcesspigot.BlockListener;

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
    
    @Override
    public void onEnable() {
	this.saveDefaultConfig();

	FileConfiguration config = getConfig();
	String kafkaServer = config.getString("kafkaServer");
	this.serverName = config.getString("serverName");
	//kafkaServer = "kafka:9092";
	
	if (kafka.connect(kafkaServer, serverName)) // using server name as client id.
	    getLogger().info("Connected to Kafka");
	else
	    getLogger().warning("Not connected to kafka, check plugin config.");

	//kafka.sendMessage("Hello");

	getServer().getPluginManager().registerEvents(new PlayerListener(serverName, kafka), this);
	getServer().getPluginManager().registerEvents(new BlockListener(serverName, kafka), this);

	// https://www.spigotmc.org/threads/guide-threading-for-beginners.523773/
	Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
		processMessages();
	    });
    }

    private void processMessages() {
	String[] events = {
	    "PLAYER_JOINED_SERVER",
	    "PLAYER_DISCONNECTED",
	    "CHAT_MESSAGE_PUBLISHED"
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
		    }
		}
	    });

	Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
		processMessages();
	    });
    }

    @Override
    public void onDisable() {
	kafka.disconnect();
	getLogger().info("MCES Disabled");
    }
}
