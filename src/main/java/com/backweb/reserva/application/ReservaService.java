package com.backweb.reserva.application;

import com.backweb.reserva.domain.Reserva;
import com.backweb.reserva.infrastructure.ReservaDisponibleOutputDto;
import com.backweb.reserva.infrastructure.ReservaInputDto;
import com.backweb.reserva.infrastructure.ReservaOutputDto;

import java.util.List;

public interface ReservaService {
    List<Reserva> findAll();
    Reserva findById(long id);
    List<ReservaDisponibleOutputDto> findDisponible(String destino, String fechaInferior, String fechaSuperior, String horaInferior, String horaSuperior);
    ReservaOutputDto add(ReservaInputDto inputDto);
    Reserva put(long id, Reserva reserva);
    void del(long id);
}
