package com.cit.notifier;

import com.cit.notifier.service.NotifierService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;


@RunWith(SpringRunner.class)
@SpringBootTest
public class NotifierApplicationTests {

    int delay = 50;


    @Value("${mqtt.broker.host}")
    public static String mqttBroker = "tcp://iot.eclipse.org:1883";//"tcp://ec2-52-91-20-33.compute-1.amazonaws.com:1883";//

    @Autowired
    public NotifierService notifier;

	@Test
	public void contextLoads() {
	}

	@Test
    public void serviceTest(){
	    notifier.publish("test");
    }


    @Test
    public void multiPublishTest(){
        for (int j=0; j<40;j++) {
            serviceTest();
        }
        try {
            Thread.sleep(delay);
        } catch (Exception e){
            System.out.println("Could not sleep main");
        }
    }


    @Test
    public void multiPublishTestDelay(){
        try{
            multiPublishTest();
            Thread.sleep(delay);
            multiPublishTest();
        }catch(Exception e){
            System.out.println("Could not sleep");
        }
    }
}
