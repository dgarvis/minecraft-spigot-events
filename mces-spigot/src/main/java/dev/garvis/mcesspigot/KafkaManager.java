package dev.garvis.mcesspigot;

import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.LongSerializer;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Map;


public class KafkaManager {

    private KafkaProducer<String, String> producer;

    public KafkaManager() {
	this.producer = null;
    }
    
    public boolean connect(String serverAddress, String clientId) {
	if (serverAddress.isEmpty()) {
	    return false;
	}

	// create Producer properties
	Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, serverAddress);
	props.put(ProducerConfig.CLIENT_ID_CONFIG, clientId);
	try {
	    props.put("key.serializer",
		      Class.forName("org.apache.kafka.common.serialization.StringSerializer"));
	    props.put("value.serializer",
		      Class.forName("org.apache.kafka.common.serialization.StringSerializer"));
	} catch(Exception e) {
	    return false;
	}
	
	// create the producer
	this.producer = new KafkaProducer<>(props);
	
	return true;
    }

    public boolean disconnect() {
	if (this.producer == null) return true;

	 this.producer.close();
	 this.producer = null;

	 return true;
    }

    public boolean sendMessage(String message) {

	if (this.producer == null) return false;
	
	// create a producer record
        ProducerRecord<String, String> producerRecord =
	    new ProducerRecord<>("events", message);

	// send data - asynchronous
        this.producer.send(producerRecord);

	System.out.println("Message Sent: " + message);
	
	return true;
    }

    public boolean sendMessage(Map<String, String> m) {
	ObjectMapper mapper = new ObjectMapper();
	String json;
	
	try {
	    json = mapper.writeValueAsString(m);
	} catch (JsonGenerationException e) {
	    return false;
	} catch (JsonMappingException e) {
	    return false;
	} catch (IOException e) {
	    return false;
	}

	return this.sendMessage(json);
    }
}
