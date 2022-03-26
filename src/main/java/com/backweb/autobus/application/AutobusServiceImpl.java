package com.backweb.autobus.application;

import com.backweb.autobus.domain.Autobus;
import com.backweb.autobus.infrastructure.AutobusInputDto;
import com.backweb.autobus.infrastructure.AutobusRepo;
import com.backweb.destino.application.DestinoService;
import com.backweb.destino.domain.Destino;
import com.backweb.shared.NotFoundException;
import com.backweb.shared.NotPlaceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AutobusServiceImpl implements AutobusService{

    @Autowired
    AutobusRepo autobusRepo;

    @Autowired
    DestinoService destinoService;

    @Override
    public List<Autobus> findAll() {
        return autobusRepo.findAll();
    }

    @Override
    public Autobus findById(long id) {
        return autobusRepo.findById(id).orElseThrow(()->new NotFoundException("El autobús "+id+" no existe"));
    }

    @Override
    public Autobus add(AutobusInputDto inputDto) {
        Destino ds = destinoService.findById(inputDto.getIdDestino());
        Autobus bus = this.toAutobus(inputDto, ds);
        autobusRepo.save(bus);
        return bus;
    }

    @Override
    public Autobus decPlazas(long id) {
        Autobus bus = this.findById(id);
        if (bus.getPlazasLibres()==0) throw new NotPlaceException("No hay plazas libres");
        bus.setPlazasLibres(bus.getPlazasLibres()-1);
        autobusRepo.save(bus);
        return bus;
    }

    @Override
    public Autobus incPlazas(long id) {
        Autobus bus = this.findById(id);
        bus.setPlazasLibres(bus.getPlazasLibres()+1);
        autobusRepo.save(bus);
        return bus;
    }

    @Override
    public Autobus put(long id, AutobusInputDto inputDto) {
        return null;
    }

    @Override
    public void del(long id) {
        Autobus bus = this.findById(id);
        autobusRepo.delete(bus);
    }

    public Autobus toAutobus(AutobusInputDto inputDto, Destino ds) {
        Autobus bus = new Autobus();
        bus.setDestino(ds);
        bus.setFecha(inputDto.getFecha());
        bus.setHoraSalida(inputDto.getHoraSalida());
        bus.setPlazasLibres(inputDto.getPlazasLibres());
        return bus;
    }
}
