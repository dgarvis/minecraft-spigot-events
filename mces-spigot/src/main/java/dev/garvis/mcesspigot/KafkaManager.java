package dev.garvis.mcesspigot;

import org.apache.kafka.clients.producer.*;
import org.apache.kafka.clients.consumer.*;
//import org.apache.kafka.common.serialization.LongSerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.util.Properties;
import java.util.Date;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.LinkedList;
import java.time.Duration;


public class KafkaManager {

    private KafkaProducer<String, String> producer;
    private KafkaConsumer<String, String> consumer;

    public KafkaManager() {
	this.producer = null;
    }
    
    public boolean connect(String serverAddress, String clientId) {
	if (serverAddress.isEmpty()) {
	    return false;
	}
	
	return createProducer(serverAddress, clientId) && createConsumer(serverAddress, clientId);
    }

    private boolean createProducer(String serverAddress, String clientId) {
	// create Producer properties
	Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, serverAddress);
	props.put(ProducerConfig.CLIENT_ID_CONFIG, clientId.replaceAll("\\s", ""));
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

    private boolean createConsumer(String serverAddress, String clientId) {
	Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, serverAddress);
	props.put(ProducerConfig.CLIENT_ID_CONFIG, clientId.replaceAll("\\s", ""));
	props.put("group.id", clientId.replaceAll("\\s", ""));
	props.put("enable.auto.commit", "true");
	try {
	    props.put("key.deserializer",
		      Class.forName("org.apache.kafka.common.serialization.StringDeserializer"));
	    props.put("value.deserializer",
		      Class.forName("org.apache.kafka.common.serialization.StringDeserializer"));
	} catch(Exception e) {
	    return false;
	}
	
	this.consumer = new KafkaConsumer<>(props);
	this.consumer.subscribe(List.of("events"));

	return true;
    }

    public boolean disconnect() {
	if (this.producer != null) {
	    this.producer.close();
	    this.producer = null;
	}
	if (this.consumer != null) {
	    this.consumer.close();
	    this.consumer = null;
	}

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

    public boolean sendMessage(Map<String, Object> m) {
	ObjectMapper mapper = new ObjectMapper();
	String json;

	m.put("stamp", new Date().getTime() / 1000L);
	
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

    /**
     * Will return when their are messages or the time out has occured.
     * 
     * @param serverName this is the name of the current server, so these messages will
     *                   be ignored.
     * @param events this is a list of events to listen for.
     * @return A list of events, each event in in a map form.
     */
    public List<Map<String,Object>> getMessages(String serverName, List<String> events) {

	// Prepare the return
	List<Map<String,Object>> r = new LinkedList();

	for (int attempt = 0; r.isEmpty() && attempt < 5; attempt++) {
	    // Atempt to get messages
	    ConsumerRecords<String, String> records = this.consumer.poll(Duration.ofSeconds(5));
	
	    // Parse Messages
	    for (ConsumerRecord<String, String> record : records) {
		ObjectMapper mapper = new ObjectMapper();
		try {
		    Map<String, Object> m = mapper.readValue(record.value(), Map.class);
		    
		    // Skip message if we don't care about it.
		    if (!m.containsKey("eventType")) continue;
		    if (m.containsKey("server") && ((String)m.get("server")).equals(serverName)) continue;
		    if (!events.contains((String)m.get("eventType"))) continue;
			
			r.add(m);
			} catch (IOException e) {
			    System.out.print("Could not decode message: " + record.value());
			}
		}
	}
	
	return r;
    }
}
