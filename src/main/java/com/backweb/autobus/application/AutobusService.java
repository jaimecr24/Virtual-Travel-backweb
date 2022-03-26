package com.backweb.autobus.application;

import com.backweb.autobus.domain.Autobus;
import com.backweb.autobus.infrastructure.AutobusInputDto;

import java.util.List;

public interface AutobusService {
    List<Autobus> findAll();
    Autobus findById(long id);
    Autobus add(AutobusInputDto inputDto);
    Autobus put(long id, AutobusInputDto inputDto);
    Autobus decPlazas(long id);
    Autobus incPlazas(long id);
    void del(long id);
}
