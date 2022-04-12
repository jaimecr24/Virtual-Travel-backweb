package com.backweb.autobus.application;

import com.backweb.autobus.domain.Autobus;
import com.backweb.autobus.infrastructure.AutobusInputDto;

import java.util.Date;
import java.util.List;

public interface AutobusService {
    int ID_LENGTH=11;
    List<Autobus> findAll();
    Autobus findById(String id);
    Autobus add(AutobusInputDto inputDto);
    Autobus setPlazas(String id, int plazas);
    void del(String id);
    String getIdBus(String idDestino, Date fecha, Float hora);
}
