package com.cit.notifier.service;

import com.cit.notifier.config.GenerateId;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

@Scope(value="prototype", proxyMode= ScopedProxyMode.TARGET_CLASS)
public class MqttPublish implements IMqttPublish, Comparable<MqttPublish> {

    /**
     * Member Vars
     */
    private String topic = null;
    private static final String ENCODING = "UTF-8";
    private String name;
    private String clientId = null;
    private String mqttBroker;
    private MqttAsyncClient client;
    private MemoryPersistence memoryPersistence;
    private IMqttToken connectToken;
    private String message =null;
    private String userContext = "default";
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private boolean available;
    private List<String> messageList = new ArrayList<>();

    /**
     * Getters/setters
     */
    public String getUserContext() {
        return userContext;
    }

    public void setUserContext(String userContext) {
        this.userContext = userContext;
    }

    public String getClientId() { return clientId; }

    public void setClientId(String clientId) { this.clientId = clientId; }

    public List<String> getMessageList() {
        return messageList;
    }

    /**
     * Constructors
     */
    MqttPublish() { }
    public MqttPublish(String name) { this.name = name; }

    /**
     * Factory method - create an instance of a publisher.
     *
     * @return - new instance.
     */
    public static MqttPublish createInstance()
    {
        return new MqttPublish();
    }

    /**
     * Initialize a connection with the given MQTT Broker
     */
    @Override
    public void connect(String broker)
    {
        mqttBroker = broker;
        try {
            MqttConnectOptions options = new MqttConnectOptions();
            options.setCleanSession(true);
            memoryPersistence = new MemoryPersistence();
            if (this.clientId == null){
                this.clientId = GenerateId.generateClientId();
                if (log.isDebugEnabled()){
                    log.debug("Had to set clientID using generateClient");
                }
            }
            if (log.isInfoEnabled()) {
                log.info(this.clientId);
            }
            client = new MqttAsyncClient(broker, this.clientId, memoryPersistence);
            client.setCallback(this);
            connectToken = client.connect(options, null, this);
            //connectToken.waitForCompletion();
        } catch (MqttException e) {
            log.error("Threw an Exception in MqttPublish::connect, full stack trace follows:", e);
        }
    }

    /**
     * Boolean to check if connection to broker still exists
     */
    @Override
    public boolean isConnected() {
        return (client != null) && (client.isConnected());
    }

    /**
     * Set the topic, try to connect, will rely on callbacks to publish
     *
     * @param mqttBroker the broker we use
     * @param mqttTopic the topic to publish on
     * @param mqttMessage the message to send
     */

    public void process(String mqttBroker, String mqttTopic, String mqttMessage){
        connect(mqttBroker);
        this.topic = mqttTopic;
        this.message = mqttMessage;
    }

    /**
     * Called if connection is lost
     */
    @Override
    public void connectionLost(Throwable cause) {
        // The MQTT client lost the connection
        log.error("Threw an Exception in MqttPublish::connectionLost, full stack trace follows:",cause);
        clientId = null;
        connect(mqttBroker);
    }

    /**
     * Called if on success, used to verify connection completes
     *
     * @param asyncActionToken deliverytoken, verification from broker
     */
    @Override
    public void onSuccess(IMqttToken asyncActionToken) {
        if (asyncActionToken.equals(connectToken)) {
            if ((this.topic != null) && (this.message != null)){
                publish(this.topic,this.message);
            }

            if (log.isInfoEnabled()) {
                log.info("Connection made");
            }
        }
    }

    /**
     * used to check if delivery is complete and instance is connected
     */
    public boolean isPublishAvailable(){
        return isConnected() && available;
    }

    /**
     * Publish a message on the given topic to the MQTT Broker
     *
     * @param strMqttTopic MQTT Topic to publish to
     * @param strMessage Message to publish
     */
    @Override
    public MessageActionListener publish(final String strMqttTopic, final String strMessage)
    {
        this.topic = strMqttTopic;
        byte[] bytesStrMessage;
        available = false;
        try {
            bytesStrMessage = strMessage.getBytes(ENCODING);
            MqttMessage message;
            message = new MqttMessage(bytesStrMessage);
            MessageActionListener actionListener = new MessageActionListener(strMqttTopic, strMessage, userContext);
            client.publish(strMqttTopic, message, userContext,	actionListener);
            return actionListener;
        } catch (UnsupportedEncodingException e) {
            log.error("Threw an UnsupportedEncodingException in MqttPublish::publish, full stack trace follows:",e);
            return null;
        } catch (MqttException e) {
            log.error(String.format("Client %s Threw an MqttException in MqttPublish::publish, full stack trace follows:",clientId),e);
            return null;
        }
    }

    /**
     * Terminate the MQTT client connection.
     */
    @Override
    public void terminate()
    {
        try {
            this.client.disconnect();
        }
        catch (MqttException e) {
            log.error("Threw an MqttException in MqttPublish::terminate, full stack trace follows:",e);
        }
    }

    /**
     * Called if delivery fails
     *
     * @param asyncActionToken token, verification from broker
     */
    @Override
    public void onFailure(IMqttToken asyncActionToken, Throwable exception)
    {
        if (asyncActionToken.equals(connectToken)){
            log.error("Connection refused, check broker");
        }
        log.error("Threw an Exception in MqttPublish::onFailure, full stack trace follows:",exception);
    }

    /**
     * MessageArrived handle broker communicaions
     *
     * @param topic the topic for the MQTT message
     * @param message the message that will be sent
     */
    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception
    {
        if (!topic.equals(this.topic)) {
            return;
        }
        String messageText = new String(message.getPayload(), ENCODING);
        if (log.isInfoEnabled()) {
            log.info("%s received %s: %s", name, topic, messageText);
        }
        String[] keyValue = messageText.split(":");
        if (keyValue.length != 3) {
            return;
        }
    }

    /**
     * Called if delivery is verified
     *
     * @param token deliverytoken, verification from broker
     */
    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        if (log.isInfoEnabled()) {
            log.info("delivery complete");
        }
        available = true;
        if (messageList.size()>0){
            String text = messageList.remove(0);
            publish(topic,text);
        }
    }

    /**
     * Add a message to the queue
     */
    public void addToList(String alert){
        messageList.add(alert);
    }

    /**
     * Used to check which instance has the least messages queued
     *
     * @param other the instance to compare to
     */
    @Override
    public int compareTo(MqttPublish other) {
        if (this.getMessageList().size() < other.getMessageList().size()) {
            return -1;
        }
        if (this.getMessageList().size() == other.getMessageList().size()) {
            return 0;
        }
        return 1;
    }
}
