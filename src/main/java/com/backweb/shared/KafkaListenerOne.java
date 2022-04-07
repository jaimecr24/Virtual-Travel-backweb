package com.backweb.shared;

import com.backweb.autobus.application.AutobusService;
import com.backweb.autobus.domain.Autobus;
import com.backweb.destino.application.DestinoService;
import com.backweb.reserva.application.ReservaService;
import com.backweb.reserva.infrastructure.ReservaOutputDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.PartitionOffset;
import org.springframework.kafka.annotation.TopicPartition;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

@Component
public class KafkaListenerOne {

    @Value(value="${server.port}")
    String port;

    @Autowired
    DestinoService destinoService;

    @Autowired
    ReservaService reservaService;

    @Autowired
    AutobusService autobusService;

    @Autowired
    SimpleDateFormat sdf1, sdf2, sdf3;

    @KafkaListener(topics = "reservas", groupId = "backweb", topicPartitions = {
            @TopicPartition(topic = "reservas", partitionOffsets = { @PartitionOffset(partition = "1", initialOffset = "0")} )
    })
    public void listenReservasWeb(ReservaOutputDto outputDto) {
        System.out.println("Backweb ("+port+"): Recibido mensaje en partición 1: " + outputDto.toString());
        if (reservaService.add(outputDto)==null) {
            System.out.println("Reserva ya existente o rechazada por falta de sitio");
        } else {
            System.out.println("Reserva añadida en la base de datos local, con identificador "+outputDto.getIdentificador());
        }
    }

    @KafkaListener(topics = "comandos", groupId = "backweb", containerFactory = "listenerStringFactory", topicPartitions = {
            @TopicPartition(topic = "comandos", partitionOffsets = { @PartitionOffset(partition = "0", initialOffset = "0")} )
    })
    public void listenComandos(String comando) throws ParseException {
        System.out.println("Backweb ("+port+"): Recibido mensaje en partición 0: " + comando);
        // Ex: UPDATE:VAL0204222000:04
        int i = comando.indexOf(":",0);
        int j = comando.lastIndexOf(":");
        String cmd = comando.substring(0,i);
        String identificador = comando.substring(i+1, j);
        String last = comando.substring(j+1);
        int longitudId = destinoService.ID_LENGTH;
        String dst = identificador.substring(0, longitudId);
        Date fecha = sdf3.parse(identificador.substring(longitudId, longitudId+6));
        Float hora = Float.parseFloat(identificador.substring(longitudId+6, longitudId+8));
        int plazas = Integer.parseInt(last);
        Autobus bus = autobusService.setPlazas(identificador.substring(0,autobusService.ID_LENGTH), plazas);
        System.out.println("Backweb ("+port+"): Actualizadas plazas del autobús "
                +dst+sdf3.format(bus.getFecha())
                +String.format("%02d",bus.getHoraSalida().intValue())
                +" a "+bus.getPlazasLibres()+" plaza(s)");
    }
}
