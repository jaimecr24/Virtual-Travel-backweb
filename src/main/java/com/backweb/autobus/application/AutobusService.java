package com.backweb.autobus.application;

import com.backweb.autobus.domain.Autobus;
import com.backweb.autobus.infrastructure.AutobusInputDto;

import java.util.Date;
import java.util.List;

public interface AutobusService {
    List<Autobus> findAll();
    Autobus findById(long id);
    Autobus add(AutobusInputDto inputDto);
    Autobus put(long id, AutobusInputDto inputDto);
    Autobus setPlazas(String destino, Date fecha, Float hora, int plazas);
    void del(long id);
}
