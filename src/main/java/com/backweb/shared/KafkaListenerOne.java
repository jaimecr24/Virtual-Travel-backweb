package com.backweb.shared;

import com.backweb.reserva.infrastructure.ReservaOutputDto;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.PartitionOffset;
import org.springframework.kafka.annotation.TopicPartition;
import org.springframework.stereotype.Component;

@Component
public class KafkaListenerOne {

    //@Value(value="${server.port}")
    String port="8080";

    @KafkaListener(topics = "reservas", groupId = "mygroup", topicPartitions = {
            @TopicPartition(topic = "reservas", partitionOffsets = { @PartitionOffset(partition = "1", initialOffset = "0")} )
    })
    public void listenTopic1(ReservaOutputDto reserva) {
        System.out.println("Backweb: Recibido mensaje en partición 1: " + reserva.toString());
    }
/*
    @KafkaListener(topics = "topic2", groupId = "mygroup")
    public void listenTopic2(String message) {
        System.out.println("App in port "+port+":");
        System.out.println("Recieved message of topic2 in listener: "+message);
    }

 */
}
