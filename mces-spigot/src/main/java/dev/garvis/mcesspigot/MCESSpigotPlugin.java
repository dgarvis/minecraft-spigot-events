package dev.garvis.mcesspigot;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.configuration.file.FileConfiguration;

import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.LongSerializer;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;

public class MCESSpigotPlugin extends JavaPlugin {
    
    FileConfiguration config = getConfig();
    
    @Override
    public void onEnable() {
	Boolean connected = this.connectToKafka();
	if (connected)
	    getLogger().info("Connected to Kafka");
	else
	    getLogger().warning("Not connected to kafka, check plugin config.");
    }

    @Override
    public void onDisable() {
	getLogger().info("onDisable");
    }

    private boolean connectToKafka() {
	String server = config.getString("kafkaServer");

	server = "kafka:9092";
	if (server.isEmpty()) {
	    return false;
	}

	// create Producer properties
	Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, server);
	props.put(ProducerConfig.CLIENT_ID_CONFIG, "KafkaExampleProducer");
	//props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        //props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

	try {
	    props.put("key.serializer", Class.forName("org.apache.kafka.common.serialization.StringSerializer"));
	    props.put("value.serializer", Class.forName("org.apache.kafka.common.serialization.StringSerializer"));
	} catch(Exception e) {
	    getLogger().warning("sigh.");
	}
	
	// create the producer
        KafkaProducer<String, String> producer = new KafkaProducer<>(props);

	// create a producer record
        ProducerRecord<String, String> producerRecord =
	    new ProducerRecord<>("first_topic", "hello world");

	// send data - asynchronous
        producer.send(producerRecord);

        // flush data - synchronous
        producer.flush();
        
        // flush and close producer
        producer.close();

	return true;
    }
}
