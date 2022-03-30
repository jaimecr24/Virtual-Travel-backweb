package com.backweb.shared;

import com.backweb.reserva.infrastructure.ReservaOutputDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.serialization.Deserializer;

public class ObjectDeserializer implements Deserializer<ReservaOutputDto> {

    @Override
    public ReservaOutputDto deserialize(String s, byte[] bytes) {
        ObjectMapper mapper = new ObjectMapper();
        ReservaOutputDto reservaOutputDto = null;
        try {
            reservaOutputDto = mapper.readValue(bytes, ReservaOutputDto.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return reservaOutputDto;
    }
}
