package com.backweb.reserva.infrastructure;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
public class ReservaOutputDto {
    private String ciudadDestino;
    private String nombre;
    private String apellido;
    private String telefono;
    private String email;
    private Date fechaReserva;
    private Float horaReserva;
    private String status; // Necesario para el backweb
}
