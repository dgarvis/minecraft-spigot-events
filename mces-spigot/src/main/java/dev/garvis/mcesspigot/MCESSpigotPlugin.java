package dev.garvis.mcesspigot;

import dev.garvis.mcesspigot.KafkaManager;
import dev.garvis.mcesspigot.PlayerListener;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.configuration.file.FileConfiguration;

public class MCESSpigotPlugin extends JavaPlugin {
    
    private KafkaManager kafka = new KafkaManager();
    
    @Override
    public void onEnable() {
	this.saveDefaultConfig();

	FileConfiguration config = getConfig();
	String kafkaServer = config.getString("kafkaServer");
	String serverName = config.getString("serverName");
	//kafkaServer = "kafka:9092";
	
	if (kafka.connect(kafkaServer, serverName)) // using server name as client id.
	    getLogger().info("Connected to Kafka");
	else
	    getLogger().warning("Not connected to kafka, check plugin config.");

	//kafka.sendMessage("Hello");

	getServer().getPluginManager().registerEvents(new PlayerListener(serverName, kafka), this);
    }

    @Override
    public void onDisable() {
	kafka.disconnect();
	getLogger().info("MCES Disabled");
    }
}
