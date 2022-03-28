package com.backweb.autobus.domain;

import com.backweb.destino.domain.Destino;
import com.backweb.reserva.domain.Reserva;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@Entity
public class Autobus {

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long idAutobus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idDestino")
    private Destino destino;

    private Date fecha;
    private Float horaSalida;
    private int plazasLibres = 40;

    @OneToMany(mappedBy = "autobus")
    private List<Reserva> reservas;
}
