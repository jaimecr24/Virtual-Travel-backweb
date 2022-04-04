package com.backweb.shared;

import com.backweb.reserva.infrastructure.ReservaOutputDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

@Component
public class KafkaMessageProducer {

    @Autowired
    private KafkaTemplate<String, ReservaOutputDto> kafkaTemplate1;
    // Toma el Bean kafkaReservaTemplate

    public void sendMessage(String topic, int partition, ReservaOutputDto outDto)
    {
        ListenableFuture<SendResult<String, ReservaOutputDto>> future = kafkaTemplate1.send(topic, partition, String.valueOf(outDto.getIdReserva()), outDto);
        future.addCallback(new ListenableFutureCallback<>() {
            @Override
            public void onSuccess(SendResult<String, ReservaOutputDto> result) {
                String topic = result.getProducerRecord().topic();
                System.out.println("Sent message=[" + outDto + "] in " + topic + " with offset=[" + result.getRecordMetadata().offset() + "]");
            }

            @Override
            public void onFailure(Throwable ex) {
                System.err.println("Unable to send message=[" + outDto + "] due to : " + ex.getMessage());
            }
        });
    }
}
