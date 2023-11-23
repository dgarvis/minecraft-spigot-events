package dev.garvis.mcesspigot;

import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.LongSerializer;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
        System.out.println( "Hello World!" );

	String server = "kafka:9092";

	// HACK
	ClassLoader original = Thread.currentThread().getContextClassLoader();
	Thread.currentThread().setContextClassLoader(null);
	
	// create Producer properties
	Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, server);
	props.put(ProducerConfig.CLIENT_ID_CONFIG, "KafkaExampleProducer");
	props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

	// create the producer
        KafkaProducer<String, String> producer = new KafkaProducer<>(props);

	// create a producer record
        ProducerRecord<String, String> producerRecord =
	    new ProducerRecord<>("first_topic", "hello world");

	Thread.currentThread().setContextClassLoader(original); // HACK
	
	// send data - asynchronous
        producer.send(producerRecord);

        // flush data - synchronous
        producer.flush();
        
        // flush and close producer
        producer.close();
    }
}
