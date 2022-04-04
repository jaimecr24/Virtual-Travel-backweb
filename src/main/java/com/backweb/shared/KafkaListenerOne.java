package com.backweb.shared;

import com.backweb.reserva.application.ReservaService;
import com.backweb.reserva.infrastructure.ReservaOutputDto;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.PartitionOffset;
import org.springframework.kafka.annotation.TopicPartition;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class KafkaListenerOne {

    @Value(value="${server.port}")
    String port;

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Autowired
    ReservaService reservaService;

    //TODO: Cambiar esto añadiendo containerFactory = "listenerReservaFactory", y así eliminar la modificación
    // de la configuración por defecto.
    // Después falta configurar el productor (nueva clase KafkaProducerConfig)
    @KafkaListener(topics = "reservas", groupId = "backweb", topicPartitions = {
            @TopicPartition(topic = "reservas", partitionOffsets = { @PartitionOffset(partition = "1", initialOffset = "0")} )
    })
    public void listenReservasWeb(ReservaOutputDto outputDto) {
        System.out.println("Backweb ("+port+"): Recibido mensaje en partición 1: " + outputDto.toString());
        if (reservaService.add(outputDto)==null) {
            System.out.println("Reserva ya existente");
        } else {
            System.out.println("Reserva añadida en la base de datos local, con identificador "+outputDto.getIdentificador());
        }
    }

    @KafkaListener(topics = "comandos", groupId = "backweb", containerFactory = "listenerStringFactory", topicPartitions = {
            @TopicPartition(topic = "comandos", partitionOffsets = { @PartitionOffset(partition = "0", initialOffset = "0")} )
    })
    public void listenComandos(String comando) {
        System.out.println("Backweb ("+port+"): Recibido mensaje en partición 0: " + comando);
    }
}
