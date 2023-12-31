package dev.garvis.mcesspigot;

//import java.util.Map;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Date;

/**
 * Manages sending and reading messages from a Kafka service.
 * When the class is created, messages can be sent to the class, 
 * however they will not be sent to the kafka server until the
 * connected is called successfully.
 */
public class KafkaManagerV2 {

    /**
     * The structure of a message.
     */
    public class Message extends HashMap<String, Object> {}

    /**
     * Function to be called when a message is ready to be
     * processed.
     */
    @FunctionalInterface
    interface ConsumerCallback {
	// https://www.w3docs.com/snippets/java/how-to-pass-a-function-as-a-parameter-in-java.html
	void apply(LinkedList<Message> messages);
    }

    /**
     * Holds messages that need to be sent to the kafka.
     * When using, wrap in a `synchronized (messageSendQueue) { }`
     *
     * Note: these messages need to have the `serverName` key set.
     */
    protected LinkedList<Message> messageSendQueue;

    /**
     * Max number of messages to keep in the queue before dropping messages
     */
    protected int maxMessageSendQueue;
    
    /**
     * Establish a base class that can recieve messages.
     */
    public KafkaManagerV2() {
	this(100000);
    }

    /**
     * Establish a base class that can recieve message, and set
     * the max queue size.
     *
     * @param maxMessageQueueSize The max number of messages to allow
     *          in the queue.
     */
    public KafkaManagerV2(int maxMessageQueueSize) {
	this.maxMessageSendQueue = maxMessageQueueSize;
	this.messageSendQueue = new LinkedList<>();
    }

    /**
     * Add a message to the queue.
     * 
     * @param msg The message to add to the send queue.
     * @return True if their was space in the queue.
     */
    protected boolean addMessageToSendQueue(Message msg) {
	synchronized (this.messageSendQueue) {
	    if (this.messageSendQueue.size() > this.maxMessageSendQueue)
		return false;
	    this.messageSendQueue.addLast(msg);
	}
	return true;
    }

    /**
     * Get the next message to send. If the message was
     * sent successfully you should then call sentSuccess.
     *
     * @param the max number of messages to return for sending.
     * @return A list of messages.
     */
    protected LinkedList<Message> getMessagesToSend(int max) {
	LinkedList<Message> re = new LinkedList<>();
	synchronized (this.messageSendQueue) {
	    for (int i = 0; i < max && i < this.messageSendQueue.size(); i++)
		re.add(this.messageSendQueue.get(i));
	}
	return re;
    }

    /**
     * Removes the message from the queue, so the next message
     * can be gotten to send
     * 
     * @param amount The amount of messages that were sent. You should
     *    not use the same value as max, but instead the size of the 
     *    number of messages that were actually returned.
     */
    protected void sentSuccess(int amount) {
	synchronized (this.messageSendQueue) {
	    for (int i = 0; i < amount; i++)
		this.messageSendQueue.removeFirst();
	}
    }

    /** 
     * Connect to kafka, in send message only mode.
     *
     * @param name Name of the prodocer.
     * @param brokers The kafka brokers to connect to.
     * @param sendTopic topic to send the events to.
     * @return True if connected, otherwise false.
     */
    public boolean connect(String name, String brokers, String sendTopic) {
	return this.connect(name, brokers, sendTopic, null, null, null);
    }

    /**
     * Connect to kafka, in consume messages only mode.
     *
     * @param name Name of the consumer group.
     * @param brokers The kafka brokers to connect to.
     * @param consumeTopics A list of topics to listen on.
     * @param consumeEvents A list of events to consume. Checks for a field called `eventType`.
     * @param consumeCallback A function to call with a list of messages to consume.
     * @return True if connected, otherwise false.
     */
    public boolean connect(String name, String brokers,
			   String[] consumeTopics, String[] consumeEvents,
			   ConsumerCallback consumeCallback) {
	return this.connect(name, brokers, null, consumeTopics, consumeEvents, consumeCallback);
    }


    /**
     * Connect to kafka, with both consumer and producer.
     *
     * @param name Name of the consumer group and of the producer.
     * @param brokers The kafka brokers to connect to.
     * @param sendTopic topic to send the events to.
     * @param consumeTopics A list of topics to listen on.
     * @param consumeEvents A list of events to consume. Checks for a field called `eventType`.
     * @param consumeCallback A function to call with a list of messages to consume.
     * @return True if connected, otherwise false.
     */
    public boolean connect(String name, String brokers, String sendTopic,
			   String[] consumeTopics, String[] consumeEvents,
			   ConsumerCallback consumeCallback) {
	// TODO
	return false;
    }

    /**
     * Addes a message to the queue to be sent to the kafka stream.
     * This function will add / overwrite any existing `stamp` key 
     * and give it a value of the epoch time this function was called at.
     * Indirectly, this function will also set the `serverName` key, but
     * not until right before the message is sent to the server.
     *
     * @param msg The message to send to kafka.
     * @return True if their was space for the message in the queue.
     */
    public boolean sendMessage(Message msg) {
	msg.put("stamp", new Date().getTime() / 1000L);
	return this.addMessageToSendQueue(msg);
    }

    /**
     * Directly sends a message to the kafka topic.
     */
    protected void sendMessageRaw(String msg) {
    }
}
