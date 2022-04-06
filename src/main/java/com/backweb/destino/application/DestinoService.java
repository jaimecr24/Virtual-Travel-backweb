package com.backweb.destino.application;

import com.backweb.destino.domain.Destino;
import com.backweb.destino.infrastructure.DestinoInputDto;

import java.util.List;

public interface DestinoService {
    int ID_LENGTH=3;
    List<Destino> findAll();
    Destino findById(String id);
    List<Destino> findByDestino(String destino);
    Destino add(DestinoInputDto inputDto);
    Destino patch(String id, DestinoInputDto inputDto);
    void del(String id);
}
