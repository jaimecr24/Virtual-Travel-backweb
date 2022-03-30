package com.backweb.shared;

import com.backweb.reserva.infrastructure.ReservaOutputDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class KafkaListenerOne {

    @Autowired
    KafkaMessageProducer kafkaMessageProducer;

    //@Value(value="${server.port}")
    String port="8080";

    @KafkaListener(topics = "reservas", groupId = "mygroup")
    public void listenTopic1(ReservaOutputDto reserva) {
        System.out.println("App in port "+port+":");
        System.out.println("Recieved message of reservas in listener: " + reserva.toString());
    }
/*
    @KafkaListener(topics = "topic2", groupId = "mygroup")
    public void listenTopic2(String message) {
        System.out.println("App in port "+port+":");
        System.out.println("Recieved message of topic2 in listener: "+message);
    }

 */
}
