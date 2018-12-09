package com.cit.notifier.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Base64.Encoder;
import java.util.Collections;
import java.util.List;

@Service
@PropertySource("classpath:application.properties")
public class NotifierService {

    @Autowired
    public NotifierService(){ }

    @Value("${mqtt.broker.host}")
    public static String mqttBroker = "tcp://iot.eclipse.org:1883";//"tcp://ec2-52-91-20-33.compute-1.amazonaws.com:1883";//

    @Value("${mqtt.topic.name}")
    public static String mqttTopic = "validation.alerts.bravo";

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private MqttPublish publisher;

    private List<MqttPublish> list = new ArrayList<>();

    public void publish(String alert){
        if (findAvailablepublisher()){
            publisher.publish(mqttTopic,alert);
        }else if (list.size()<20){
            publisher = MqttPublish.createInstance();
            list.add(publisher);
            publisher.process(mqttBroker,mqttTopic,alert);
        }else{
            findSmallestList();
            publisher.addToList(alert);
            //sort the list by number of messages and add to the one with least messages
        }
    }


    private void findSmallestList(){
        Collections.sort(list);
        publisher = list.get(0);
    }

    private boolean findAvailablepublisher(){
        boolean found = false;
        log.info(String.valueOf(list.size()));
        for (MqttPublish mqttPublish:list) {
            if (mqttPublish.isPublishAvailable()){
                if (log.isDebugEnabled()){
                    log.debug("found a publisher");
                }
                publisher = mqttPublish;
                found = true;
                break;
            }
        }
        return found;
    }
    // prototype objects, need to clear if they are not connected, how to handle?
    private void cleanList(){
        for (MqttPublish mqttPublish:list){
            if (!mqttPublish.isConnected()){
                mqttPublish = null;
                list.remove(mqttPublish);
            }
        }
    }
}