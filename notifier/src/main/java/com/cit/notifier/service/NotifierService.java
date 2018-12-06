package com.cit.notifier.service;

import com.cit.notifier.model.MqttPublish;
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
        if (publisherAvailable()){
            publisher.publish(mqttTopic,alert);
        }else if (list.size()<20){
            publisher = MqttPublish.createInstance();
            generateClientId();
            list.add(publisher);
            publisher.process(mqttBroker,mqttTopic,alert);
        }else{
            findSmallestList();
            publisher.addToList(alert);
            //sort the list by number of messages and add to the one with least messages
        }
    }

    private void generateClientId(){
        String generatedString = generateSafeToken();
        log.info(String.format("new client ID is: %s",generatedString));
        publisher.setClientId(generatedString);
    }

    private void findSmallestList(){
        Collections.sort(list);
        publisher = list.get(0);
    }

    private boolean publisherAvailable(){
        boolean found = false;
        log.info(String.valueOf(list.size()));
        for (MqttPublish i:list) {
            if (i.isPublishAvailable()){
                if (log.isDebugEnabled()){
                    log.debug("found a publisher");
                }
                publisher = i;
                found = true;
                break;
            }
        }
        /*for (int i=0 ; i<list.size() ;i++){
            if (list.get(i).isPublishAvailable()){
            if (log.isDebugEnabled()){
                log.debug("found a publisher");
            }
            publisher = list.get(i);
            found = true;
            break;
        }
    }*/
        return found;
    }

    private String generateSafeToken() {
        SecureRandom random = new SecureRandom();
        byte bytes[] = new byte[15];
        random.nextBytes(bytes);
        Encoder encoder = Base64.getUrlEncoder().withoutPadding();
        return encoder.encodeToString(bytes);
    }
}