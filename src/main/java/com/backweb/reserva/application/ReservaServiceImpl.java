package com.backweb.reserva.application;

import com.backweb.autobus.application.AutobusService;
import com.backweb.autobus.domain.Autobus;
import com.backweb.destino.application.DestinoService;
import com.backweb.destino.domain.Destino;
import com.backweb.reserva.domain.Reserva;
import com.backweb.reserva.infrastructure.ReservaDisponibleOutputDto;
import com.backweb.reserva.infrastructure.ReservaInputDto;
import com.backweb.reserva.infrastructure.ReservaOutputDto;
import com.backweb.reserva.infrastructure.ReservaRepo;
import com.backweb.shared.NotFoundException;
import com.backweb.shared.NotPlaceException;
import com.backweb.shared.UnprocesableException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReservaServiceImpl implements ReservaService {

    @Autowired
    ReservaRepo reservaRepo;

    @Autowired
    DestinoService destinoService;

    @Autowired
    AutobusService autobusService;

    @Autowired
    SimpleDateFormat sdf1, sdf2;

    @Override
    public List<Reserva> findAll() {
        return reservaRepo.findAll();
    }

    @Override
    public Reserva findById(long id) {
        return reservaRepo.findById(id).orElseThrow(()->new NotFoundException("Reserva "+id+" no encontrada."));
    }

    @Override
    public Reserva findByIdentificador(String identificador) {
        return reservaRepo.findByIdentificador(identificador).orElseThrow(()-> new NotFoundException("Identificador "+identificador+" no encontrado"));
    }

    @Override
    public List<ReservaDisponibleOutputDto> findDisponible(String destino, String fechaInferior, String fechaSuperior, String horaInferior, String horaSuperior) {
        // Obtiene todos los autobuses con plazas disponibles para el destino e intervalo especificado.
        // fechaSuperior, horaInferior y horaSuperior pueden ser null.
        // Formato de las fechas: ddMMyyyy || Formato de las horas: 00
        List<Destino> dstList = destinoService.findByDestino(destino);
        if (dstList.size()==0) return new ArrayList<>();
        Destino dst = dstList.get(0);
        List<Autobus> busList = dst.getAutobuses();
        try {
            Date fInf = sdf2.parse(fechaInferior);
            Date fSup = (fechaSuperior != null) ? sdf2.parse(fechaSuperior) : null;
            Float hInf = (horaInferior != null) ? Float.parseFloat(horaInferior) : 0F;
            Float hSup = (horaSuperior != null) ? Float.parseFloat(horaSuperior) : 24F;
            return busList.stream().filter(e ->
                    e.getFecha().compareTo(fInf) >= 0
                            && (fSup==null || e.getFecha().compareTo(fSup)<=0)
                            && e.getHoraSalida()>=hInf && e.getHoraSalida()<=hSup)
                    .map(ReservaDisponibleOutputDto::new).collect(Collectors.toList());
        } catch(ParseException e) {
            throw new UnprocesableException("Error en el formato de las fechas: "+e.getMessage());
        }
    }

    @Override
    public List<ReservaOutputDto> findReservas(String destino, String fechaInferior, String fechaSuperior, String horaInferior, String horaSuperior) {
        // Obtiene todas las reservas anotadas para un destino y un intervalo de fechas y horas (sin importar el status).
        // fechaSuperior, horaInferior y horaSuperior pueden ser null.
        // Formato de las fechas: ddMMyyyy || Formato de las horas: 00
        List<Destino> dstList = destinoService.findByDestino(destino);
        if (dstList.size()==0) return new ArrayList<>();
        Destino dst = dstList.get(0);
        List<Autobus> busList = dst.getAutobuses();
        try {
            Date fInf = sdf2.parse(fechaInferior);
            Date fSup = (fechaSuperior != null) ? sdf2.parse(fechaSuperior) : null;
            Float hInf = (horaInferior != null) ? Float.parseFloat(horaInferior) : 0F;
            Float hSup = (horaSuperior != null) ? Float.parseFloat(horaSuperior) : 24F;
            Optional<List<Reserva>> reservas = busList.stream().filter(e ->
                            e.getFecha().compareTo(fInf) >= 0
                                    && (fSup==null || e.getFecha().compareTo(fSup)<=0)
                                    && e.getHoraSalida()>=hInf && e.getHoraSalida()<=hSup).map(Autobus::getReservas)
                    .reduce((l1,l2)-> { l1.addAll(l2); return l1; });
            return reservas.map(reservaList -> reservaList.stream().map(this::toOutputDto).collect(Collectors.toList()))
                    .orElseGet(ArrayList::new);
        } catch(ParseException e) {
            throw new UnprocesableException("Error en el formato de las fechas: "+e.getMessage());
        }
    }

    @Override
    public ReservaOutputDto add(ReservaInputDto inputDto) throws NotFoundException {
        // Crea un objeto Reserva con los datos indicados en inputDto
        // El status de la reserva será ACEPTADA/RECHAZADA según las plazas libres.
        Reserva rsv = this.toReserva(inputDto);
        Autobus bus = rsv.getAutobus();
        int plazas = bus.getPlazasLibres();
        if (plazas>0) {
            rsv.setStatus(Reserva.STATUS.ACEPTADA);
            rsv.setIdentificador(this.getIdentificadorReserva(bus));
            bus.setPlazasLibres(plazas-1); // Actualizamos plazas disponibles.
        }
        else rsv.setStatus(Reserva.STATUS.RECHAZADA);
        rsv.setFechaRegistro(new Date());
        reservaRepo.save(rsv);
        return this.toOutputDto(rsv);
    }

    @Override
    @Transactional
    public ReservaOutputDto add(ReservaOutputDto outputDto) throws NotFoundException, NotPlaceException {
        // Se llama a este método para actualizar la lista de reservas con las recibidas por kafka.
        // Por tanto conservamos el mismo identificador de la reserva y no enviamos mensaje a backempresa.
        // Si la reserva no existe y queda sitio libre crea una con los datos de outputDto
        // Si existe o no queda sitio libre devuelve null para indicarlo.
        Optional<Reserva> optRsv = reservaRepo.findByIdentificador(outputDto.getIdentificador());
        if (optRsv.isEmpty()) {
            Reserva rsv = this.toReserva(outputDto);
            rsv.setFechaRegistro(new Date());
            Autobus bus = rsv.getAutobus();
            int plazas = bus.getPlazasLibres();
            long aceptadas = this.numReservasAceptadas(bus);
            if (plazas==0) return null; // Si no hay plazas cualquier reserva llegada por kafka es ignorada
            // Pero si plazas > 0, sólo cambiamos el número de plazas cuando el total de plazas
            // menos las aceptadas sea igual (o mayor) a las plazas libres.
            if (plazas >= bus.getMaxPlazas()-aceptadas) rsv.getAutobus().setPlazasLibres(plazas-1);
            reservaRepo.save(rsv);
            return this.toOutputDto(rsv);
        }
        else return null;
    }

    @Transactional
    @Override
    public void del(long idReserva) {
        Reserva rsv = this.findById(idReserva);
        Autobus bus = rsv.getAutobus();
        int plazas = bus.getPlazasLibres();
        bus.setPlazasLibres(plazas+1);
        reservaRepo.delete(rsv);
    }

    private String getIdentificadorReserva(Autobus bus){
        // Obtiene el identificador de la próxima reserva en el bus, limitado a 99 reservas por bus
        // Si se necesitan más, debe cambiarse el formato del último elemento del identificador a %03d
        return bus.getId() + String.format("%02d",bus.getMaxPlazas()-bus.getPlazasLibres()+1);
    }

    private long numReservasAceptadas(Autobus bus) {
        return bus.getReservas().stream().filter(e ->
                e.getStatus() == Reserva.STATUS.ACEPTADA || e.getStatus() == Reserva.STATUS.CONFIRMADA).count();
    }

    public Reserva toReserva(ReservaInputDto inputDto) throws NotFoundException {
        // Creamos el objeto
        Reserva rsv = new Reserva();
        // Buscamos el objeto Destino
        Destino dst = destinoService.findById(inputDto.getIdDestino());
        // Recuperamos el autobús con el día y la hora indicadas
        String idBus = autobusService.getIdBus(inputDto.getIdDestino(),inputDto.getFechaReserva(),inputDto.getHoraSalida());
        Autobus bus = autobusService.findById(idBus);
        // Asignamos los campos del objeto Reserva
        rsv.setNombre(inputDto.getNombre());
        rsv.setApellido(inputDto.getApellido());
        rsv.setEmail(inputDto.getEmail());
        rsv.setTelefono(inputDto.getTelefono());
        rsv.setAutobus(bus);
        // fechaReserva e idReserva se completan en el momento de añadir la reserva a la bd.
        return rsv;
    }

    public Reserva toReserva(ReservaOutputDto outputDto) throws NotFoundException {
        // Crea una reserva con los datos de outputDto
        String idBus = this.getIdBus(outputDto.getIdentificador());
        Autobus bus = autobusService.findById(idBus);
        Reserva rsv = new Reserva();
        rsv.setNombre(outputDto.getNombre());
        rsv.setApellido(outputDto.getApellido());
        rsv.setEmail(outputDto.getEmail());
        rsv.setTelefono(outputDto.getTelefono());
        rsv.setAutobus(bus);
        rsv.setIdentificador(outputDto.getIdentificador());
        if (outputDto.getStatus()!=null) switch (outputDto.getStatus()) {
            case "ACEPTADA": rsv.setStatus(Reserva.STATUS.ACEPTADA); break;
            case "RECHAZADA": rsv.setStatus(Reserva.STATUS.RECHAZADA); break;
            case "CONFIRMADA": rsv.setStatus(Reserva.STATUS.CONFIRMADA); break;
            default: rsv.setStatus(null);
        }
        return rsv;
    }

    private String getIdBus(String identificadorReserva) {
        return identificadorReserva.substring(0, autobusService.ID_LENGTH);
    }

    public ReservaOutputDto toOutputDto(Reserva rsv) {
        ReservaOutputDto outDto = new ReservaOutputDto();
        if (rsv.getIdReserva()!=null) outDto.setIdReserva(rsv.getIdReserva());
        String id = rsv.getIdentificador();
        if (id!=null) {
            outDto.setIdentificador(id);
            String idDestino = rsv.getIdentificador().substring(0,3);
            Destino dst = destinoService.findById(idDestino);
            outDto.setCiudadDestino(dst.getNombreDestino());
        }
        outDto.setNombre(rsv.getNombre());
        outDto.setApellido(rsv.getApellido());
        outDto.setEmail(rsv.getEmail());
        outDto.setTelefono(rsv.getTelefono());
        outDto.setFechaReserva(sdf1.format(rsv.getAutobus().getFecha()));
        outDto.setHoraReserva(rsv.getAutobus().getHoraSalida());
        if (rsv.getStatus()!=null) switch (rsv.getStatus()) {
            case ACEPTADA: outDto.setStatus("ACEPTADA"); break;
            case RECHAZADA: outDto.setStatus("RECHAZADA"); break;
            case CONFIRMADA: outDto.setStatus("CONFIRMADA"); break;
            default: outDto.setStatus("INDEFINIDA");
        }
        return outDto;
    }
}
