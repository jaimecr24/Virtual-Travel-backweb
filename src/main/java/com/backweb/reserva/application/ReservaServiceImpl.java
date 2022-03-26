package com.backweb.reserva.application;

import com.backweb.autobus.application.AutobusService;
import com.backweb.autobus.domain.Autobus;
import com.backweb.destino.application.DestinoService;
import com.backweb.destino.domain.Destino;
import com.backweb.reserva.domain.Reserva;
import com.backweb.reserva.infrastructure.ReservaInputDto;
import com.backweb.reserva.infrastructure.ReservaOutputDto;
import com.backweb.reserva.infrastructure.ReservaRepo;
import com.backweb.shared.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class ReservaServiceImpl implements ReservaService {

    @Autowired
    ReservaRepo reservaRepo;

    @Autowired
    DestinoService destinoService;

    @Autowired
    AutobusService autobusService;

    private SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");

    @Override
    public List<Reserva> findAll() {
        return reservaRepo.findAll();
    }

    @Override
    public Reserva findById(long id) {
        return reservaRepo.findById(id).orElseThrow(()->new NotFoundException("Reserva "+id+" no encontrada."));
    }

    @Override
    public ReservaOutputDto add(ReservaInputDto inputDto) throws NotFoundException {
        // Crea un objeto Reserva con los datos indicados en inputDto
        // El status de la reserva será ACEPTADA/RECHAZADA según las plazas libres y las ya aceptadas para ese autobús.
        Reserva rsv = this.toReserva(inputDto);
        Autobus bus = rsv.getAutobus();
        rsv.setStatus((bus.getPlazasLibres() - numReservasAceptadas(bus) > 0) ? Reserva.STATUS.ACEPTADA : Reserva.STATUS.RECHAZADA);
        rsv.setFechaRegistro(new Date());
        reservaRepo.save(rsv);
        return this.toOutputDto(rsv);
    }

    private long numReservasAceptadas(Autobus bus) {
        return bus.getReservas().stream().filter(e -> e.getStatus() == Reserva.STATUS.ACEPTADA).count();
    }

    @Override
    public Reserva put(long id, Reserva reserva) {
        return null;
    }

    @Override
    public void del(long id) {
        // Incrementamos el número de plazas disponibles en el autobús
        // y eliminamos la reserva.
        Reserva rsv = this.findById(id);
        Autobus bus = rsv.getAutobus();
        autobusService.incPlazas(bus.getIdAutobus());
        reservaRepo.delete(rsv);
    }

    public Reserva toReserva(ReservaInputDto inputDto) throws NotFoundException {
        // Creamos el objeto
        Reserva rsv = new Reserva();
        // Buscamos el objeto Destino
        Destino dst = destinoService.findById(inputDto.getIdDestino());
        // Recuperamos el autobús con el día y la hora indicadas
        List<Autobus> autobuses = dst.getAutobuses();
        Optional<Autobus> myBus =
                autobuses.stream().filter(e ->
                        sdf.format(e.getFecha()).equals(sdf.format(inputDto.getFechaReserva()))
                                && Objects.equals(e.getHoraSalida(), inputDto.getHoraSalida())).findFirst();
        if (myBus.isEmpty()) throw new NotFoundException("No hay ningún autobús el "+inputDto.getFechaReserva()+" a las "+inputDto.getHoraSalida());
        // Asignamos los campos del objeto Reserva
        rsv.setNombre(inputDto.getNombre());
        rsv.setApellido(inputDto.getApellido());
        rsv.setEmail(inputDto.getEmail());
        rsv.setTelefono(inputDto.getTelefono());
        rsv.setAutobus(myBus.get());
        // fechaReserva e idReserva se completan en el momento de añadir la reserva a la bd.
        return rsv;
    }

    public ReservaOutputDto toOutputDto(Reserva rsv) {
        ReservaOutputDto outDto = new ReservaOutputDto();
        outDto.setCiudadDestino(rsv.getAutobus().getDestino().getNombreDestino());
        outDto.setNombre(rsv.getNombre());
        outDto.setApellido(rsv.getApellido());
        outDto.setEmail(rsv.getEmail());
        outDto.setTelefono(rsv.getTelefono());
        outDto.setFechaReserva(rsv.getAutobus().getFecha());
        outDto.setHoraReserva(rsv.getAutobus().getHoraSalida());
        switch (rsv.getStatus()) {
            case ACEPTADA: outDto.setStatus("ACEPTADA"); break;
            case RECHAZADA: outDto.setStatus("RECHAZADA"); break;
            case CONFIRMADA: outDto.setStatus("CONFIRMADA"); break;
            default: outDto.setStatus("INDEFINIDA");
        }
        return outDto;
    }
}
