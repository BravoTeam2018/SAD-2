package com.cit.notifier.service;

import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.MqttCallback;

public interface IMqttPublish extends IMqttActionListener, MqttCallback {

    /**
     * Connect with the given MQTT Broker
     */
    void connect(String mqttBroker);

    /**
     * Check if there is a connection
     */
    boolean isConnected();

    /**
     * Publish a message on the given topic to the MQTT Broker
     *
     * @param strMqttTopic MQTT Topic to publish to
     * @param strMessage Message to publish
     */
    MessageActionListener publish(final String strMqttTopic, final String strMessage);

    /**
     * Terminate the connection from the MQTT Broker.
     */
    void terminate();

}
