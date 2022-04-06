package com.backweb.reserva.application;

import com.backweb.reserva.domain.Reserva;
import com.backweb.reserva.infrastructure.ReservaDisponibleOutputDto;
import com.backweb.reserva.infrastructure.ReservaInputDto;
import com.backweb.reserva.infrastructure.ReservaOutputDto;

import java.util.List;

public interface ReservaService {
    List<Reserva> findAll();
    Reserva findById(long id);
    Reserva findByIdentificador(String identificador);
    List<ReservaDisponibleOutputDto> findDisponible(String destino, String fechaInferior, String fechaSuperior, String horaInferior, String horaSuperior);
    List<ReservaOutputDto> findReservas(String destino, String fechaInferior, String fechaSuperior, String horaInferior, String horaSuperior);
    ReservaOutputDto add(ReservaInputDto inputDto);
    ReservaOutputDto add(ReservaOutputDto outputDto);
    void del(long id);
}
