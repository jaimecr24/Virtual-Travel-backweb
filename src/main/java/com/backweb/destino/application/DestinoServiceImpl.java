package com.backweb.destino.application;

import com.backweb.destino.domain.Destino;
import com.backweb.destino.infrastructure.DestinoInputDto;
import com.backweb.destino.infrastructure.DestinoRepo;
import com.backweb.shared.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class DestinoServiceImpl implements DestinoService{

    @Autowired
    DestinoRepo destinoRepo;

    @Override
    public List<Destino> findAll() {
        return destinoRepo.findAll();
    }

    @Override
    public Destino findById(long id) {
        return destinoRepo.findById(id).orElseThrow(()->new NotFoundException("Destino +"+id+" no encontrado"));
    }

    @Override
    public Destino add(DestinoInputDto inputDto) {
        // Crea un nuevo destino con la lista de autobuses vacía.
        Destino ds = this.toDestino(inputDto);
        destinoRepo.save(ds);
        return ds;
    }

    @Override
    public Destino put(long id, DestinoInputDto inputDto) {
        // Permite modificar el nombre de un destino.
        Destino ds = this.findById(id);
        ds.setNombreDestino(inputDto.getNombre());
        destinoRepo.save(ds);
        return ds;
    }

    @Override
    public void del(long id) {
        Destino ds = this.findById(id);
        destinoRepo.delete(ds);
    }

    public Destino toDestino(DestinoInputDto inputDto) {
        Destino ds = new Destino();
        ds.setNombreDestino(inputDto.getNombre());
        ds.setAutobuses(new ArrayList<>());
        return ds;
    }
}
