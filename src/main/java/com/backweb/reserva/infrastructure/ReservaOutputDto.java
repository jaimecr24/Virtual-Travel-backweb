package com.backweb.reserva.infrastructure;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@Data
public class ReservaOutputDto {
    private long idReserva;
    private String ciudadDestino;
    private String nombre;
    private String apellido;
    private String telefono;
    private String email;
    private String fechaReserva;
    private Float horaReserva;
    private String status; // Necesario para el backweb


}
