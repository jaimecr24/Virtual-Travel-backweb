package com.backweb.autobus.application;

import com.backweb.autobus.domain.Autobus;
import com.backweb.autobus.infrastructure.AutobusInputDto;
import com.backweb.autobus.infrastructure.AutobusRepo;
import com.backweb.destino.application.DestinoService;
import com.backweb.destino.domain.Destino;
import com.backweb.shared.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class AutobusServiceImpl implements AutobusService{

    @Autowired
    AutobusRepo autobusRepo;

    @Autowired
    DestinoService destinoService;

    @Autowired
    SimpleDateFormat sdf1, sdf2, sdf3;

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
    public Autobus put(long id, AutobusInputDto inputDto) {
        return null;
    }

    @Override
    @Transactional
    public Autobus setPlazas(String key, Date fecha, Float hora, int plazas) {
        // Establece el número de plazas de un autobús y pone el campo actualizado a true.
        List<Destino> lstDestino = destinoService.findByKey(key);
        if (lstDestino.size()==0) throw new NotFoundException("Destino no encontrado");
        List<Autobus> lstBus = lstDestino.get(0).getAutobuses();
        Optional<Autobus> optBus = lstBus.stream().filter(e ->
                sdf3.format(fecha).equals(sdf3.format(e.getFecha()))
                        && Objects.equals(hora, e.getHoraSalida())).findFirst();
        Autobus bus = optBus.orElseThrow(()->new NotFoundException("Autobus no encontrado"));
        bus.setPlazasLibres(plazas);
        autobusRepo.save(bus);
        return bus;
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
