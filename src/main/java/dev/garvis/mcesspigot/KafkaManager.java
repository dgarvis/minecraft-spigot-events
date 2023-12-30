package dev.garvis.mcesspigot;

import org.apache.kafka.clients.producer.*;
import org.apache.kafka.clients.consumer.*;
//import org.apache.kafka.common.serialization.LongSerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.errors.TimeoutException;

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

    // Used to old messages created before connecting to the server.
    private LinkedList<String> messageBacklog = new LinkedList<String>();
    // Number of messages to hold before dropping the oldest.
    static final int MAX_BACKLOG_MESSAGES = 100000;

    private String topic = "";

    public KafkaManager() {
	this.producer = null;
    }
    
    public boolean connect(String serverAddress, String clientId, String topic) {
	if (serverAddress.isEmpty() || topic.isEmpty()) {
	    return false;
	}

	this.topic = topic;
	
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
	props.put("max.poll.records", 50);
	try {
	    props.put("key.deserializer",
		      Class.forName("org.apache.kafka.common.serialization.StringDeserializer"));
	    props.put("value.deserializer",
		      Class.forName("org.apache.kafka.common.serialization.StringDeserializer"));
	} catch(Exception e) {
	    return false;
	}
	
	this.consumer = new KafkaConsumer<>(props);
	this.consumer.subscribe(List.of(this.topic));

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

    /** 
     * Will attempt to send messages from the backlog.
     */
    public void processBacklog() {
	if (this.producer == null) return;
	if (this.messageBacklog.size() <= 0) return;

	for(String message = this.messageBacklog.removeFirst();
	    message != null;
	    message = this.messageBacklog.removeFirst()) {
	    try {
		this.producer.send(new ProducerRecord<>(this.topic, message));
	    } catch (TimeoutException e) {
		this.messageBacklog.addFirst(message);
		break; // break out of our loop
	    }
	}
    }

    /**
     * Will send a message into the kafka topic.
     * 
     * @param message The message to send.
     * @return True if the message was sent to the kafka queue, 
     *         and false if added to the backlog queue.
     */
    public boolean sendMessage(String message) {

	if (this.producer == null /*|| this.producer.*/) {
	    this.messageBacklog.add(message);
	    System.out.println("Not connected to kafka, added message to backlog: " + message);
	    if (this.messageBacklog.size() > MAX_BACKLOG_MESSAGES) {
		String removedMessage = this.messageBacklog.removeFirst();
		System.out.println("Backlog size exceeded, message removed: " + removedMessage);
	    }
	    
	    return false;
	}
	
	// create a producer record
        ProducerRecord<String, String> producerRecord =
	    new ProducerRecord<>(this.topic, message);

	// TODO - this should probably always added to a local message queue or database
	//        and then have a different thread that sends the messages from that.
	//        Though not sure I really need this to be that High Availablitiy.
	// send data
	try {
	    this.producer.send(producerRecord);
	} catch (TimeoutException e) {
	    this.messageBacklog.add(message);
	    System.out.println("Kafka timeout, added message to backlog: " + message);
	    return false;
	}

	//System.out.println("Message Sent: " + message);
	
	return true;
    }

    /**
     * Takes a map and converts it into a json string to send as a message.
     *
     * @param m The map to send.
     * @return True if the message was sent, False if added to the backlog.
     */
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

	// make sure we are connected
	if (this.consumer == null) {
	    // TODO - sholud probably add some delay.
	    return r;
	}

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
