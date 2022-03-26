package com.backweb.reserva.infrastructure;

import com.backweb.reserva.application.ReservaService;
import com.backweb.reserva.domain.Reserva;
import com.backweb.shared.NotPlaceException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Slf4j
@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/v0")
public class ReservaControlador {

    @Autowired
    ReservaService reservaService;

    private SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");

    @GetMapping
    public ResponseEntity<List<Reserva>> findAll()
    {
        return new ResponseEntity<>(new ArrayList<Reserva>(), HttpStatus.OK);
    }

    @PostMapping("reserva")
    public ResponseEntity<ReservaOutputDto> add(@RequestBody ReservaInputDto inputDto)
    {
        // Realizamos reserva
        ReservaOutputDto outDto = reservaService.add(inputDto);
        log.trace(new Date().toString()+" Reserva: "+
                outDto.getCiudadDestino()+" "+sdf.format(outDto.getFechaReserva())+" "+
                String.format("%02dH ",outDto.getHoraReserva().intValue())+
                outDto.getNombre()+" "+outDto.getApellido()+
                " status: "+outDto.getStatus());

        if (Objects.equals(outDto.getStatus(), "ACEPTADA"))
            return new ResponseEntity<>(outDto,HttpStatus.OK);

        return new ResponseEntity<>(outDto,HttpStatus.NOT_ACCEPTABLE);
        // Falta enviar correo al usuario, enviar mensaje al back de la empresa
        // y listener para escuchar mensaje de confirmación.
    }
}
