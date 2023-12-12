package dev.garvis.mcesspigot;

import dev.garvis.mcesspigot.KafkaManager;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.configuration.file.FileConfiguration;

public class MCESSpigotPlugin extends JavaPlugin {
    
    private FileConfiguration config = getConfig();
    private KafkaManager kafka = new KafkaManager();
    
    @Override
    public void onEnable() {
	String server = config.getString("kafkaServer");
	server = "kafka:9092";
	
	if (kafka.connect(server, "clientId"))
	    getLogger().info("Connected to Kafka");
	else
	    getLogger().warning("Not connected to kafka, check plugin config.");

	kafka.sendMessage("Hello");
    }

    @Override
    public void onDisable() {
	kafka.disconnect();
	getLogger().info("MCES Disabled");
    }
}
