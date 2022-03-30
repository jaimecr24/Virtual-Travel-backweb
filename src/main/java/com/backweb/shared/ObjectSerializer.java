package com.backweb.shared;

import com.backweb.reserva.infrastructure.ReservaOutputDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.serialization.Serializer;

import java.io.IOException;
import java.util.Map;

public class ObjectSerializer implements Serializer<ReservaOutputDto> {

    //TODO:Provisional: estas clases se asignan al kafkaTemplate general en el archivo properties
    // Hay que definir una kafka consumer y producer propios para esta clase
    @Override
    public byte[] serialize(String topic, ReservaOutputDto data) {
        byte[] serializedBytes = null;
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            serializedBytes = objectMapper.writeValueAsString(data).getBytes();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return serializedBytes;
    }

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {

    }

    @Override
    public void close() {

    }

}