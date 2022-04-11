package com.backweb;

import com.backweb.autobus.application.AutobusService;
import com.backweb.autobus.infrastructure.AutobusInputDto;
import com.backweb.destino.application.DestinoService;
import com.backweb.destino.infrastructure.DestinoInputDto;
import com.backweb.reserva.application.ReservaService;
import com.backweb.reserva.infrastructure.ReservaDisponibleOutputDto;
import com.backweb.reserva.infrastructure.ReservaInputDto;
import com.backweb.reserva.infrastructure.ReservaOutputDto;
import com.backweb.shared.NotFoundException;
import com.backweb.shared.UnprocesableException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import javax.transaction.Transactional;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ReservaServiceImplTest {

    @Autowired
    ReservaService reservaService;

    @Autowired
    AutobusService autobusService;

    @Autowired
    DestinoService destinoService;

    @Autowired
    SimpleDateFormat sdf3;

    private final String idDestino1 = "VAL";
    private final String nombreDestino1 = "Valencia";
    private final String fechaStr = "010522";
    private final Float horaSalida = 12F;
    private final String email1 = "email1@email.com";

    @BeforeAll
    void starting() throws ParseException {

        assertTrue(reservaService.findAll().isEmpty());
        assertTrue(autobusService.findAll().isEmpty());
        assertTrue(destinoService.findAll().isEmpty());
        // Añadimos un destino
        destinoService.add(new DestinoInputDto(idDestino1, nombreDestino1));
        // Añadimos dos autobuses a ese destino, cada uno con dos plazas libres.
        Date fecha = sdf3.parse(fechaStr);
        AutobusInputDto inputDto1 = new AutobusInputDto(idDestino1, fecha, horaSalida,2);
        AutobusInputDto inputDto2 = new AutobusInputDto(idDestino1, fecha, horaSalida+1,2);
        autobusService.add(inputDto1);
        autobusService.add(inputDto2);
    }

    @Test
    void testFindAll() {
        assertTrue(reservaService.findAll().isEmpty());
    }

    @Test
    void testFindById() {
        // Búsqueda de un elemento inexistente
        assertThrows(NotFoundException.class, () -> reservaService.findById(99999L));
    }

    @Test
    void testFindByIdentificador() {
        // Búsqueda elemento inexistente
        assertThrows(NotFoundException.class, () -> reservaService.findByIdentificador("UAUAUAUA"));
    }

    // Datos necesarios para las pruebas de findDisponible y findReservas
    private final String fechaInf = "01012022";
    private final String fechaSup = "01122022";
    private final String horaInf = "00";
    private final String horaSup = "23";
    private final String fechaStr2 = "020522"; // Un día después de fechaStr

    @Test
    @Transactional
    void testFindDisponible() throws ParseException {
        String destino = "Lugo";
        List<ReservaDisponibleOutputDto> listDisp;
        Date fecha = sdf3.parse(fechaStr);

        // Añadimos una reserva en cada autobús
        ReservaOutputDto rsv1 = reservaService.add(new ReservaInputDto(idDestino1,"nombre1","apellido1","111111",email1,fecha,horaSalida));
        ReservaOutputDto rsv2 = reservaService.add(new ReservaInputDto(idDestino1,"nombre1","apellido1","111111",email1,fecha,horaSalida+1));

        // Fecha con formato incorrecto
        assertThrows(UnprocesableException.class, () -> reservaService.findDisponible(nombreDestino1, "aa122022", null, null, null));

        // Destino inexistente
        assertTrue(reservaService.findDisponible(destino, fechaInf, fechaSup, horaInf, horaSup).isEmpty());

        // Búsqueda con plazas libres (quedaba 1 plaza en cada autobús)
        listDisp = reservaService.findDisponible(nombreDestino1, fechaInf, fechaSup, horaInf, horaSup);
        assertEquals(2,listDisp.size());

        // Búsqueda poniendo en fechaInf el límite superior
        listDisp = reservaService.findDisponible(nombreDestino1, fechaSup, null, null, null);
        assertTrue(listDisp.isEmpty());

        // Búsqueda con fechaSup, horaInf y horaSup == null
        listDisp = reservaService.findDisponible(nombreDestino1, fechaInf, null, null, null);
        assertEquals(2,listDisp.size()); // Comprobamos además que la eliminación ha aumentado el número de plazas.

        // Búsqueda en un rango menor de horas para dejar fuera uno de los autobuses
        String horaStr = String.format("%02d",horaSalida.intValue());
        listDisp = reservaService.findDisponible(nombreDestino1, fechaInf, fechaSup, horaStr, horaStr);
        assertEquals(1,listDisp.size());
        assertEquals(horaSalida, listDisp.get(0).getHoraReserva());  // Comprobamos que la hora en el resultado es la misma.

        // Eliminamos las reservas creadas
        reservaService.del(rsv1.getIdReserva());
        reservaService.del(rsv2.getIdReserva());
    }

    @Test
    @Transactional
    void testFindReservas() throws ParseException {
        // Añadimos una reserva en cada autobús
        Date fecha = sdf3.parse(fechaStr);
        ReservaOutputDto rsv1 = reservaService.add(new ReservaInputDto(idDestino1,"nombre1","apellido1","111111",email1,fecha,horaSalida));
        ReservaOutputDto rsv2 = reservaService.add(new ReservaInputDto(idDestino1,"nombre1","apellido1","111111",email1,fecha,horaSalida+1));

        List<ReservaOutputDto> res = reservaService.findReservas(nombreDestino1, fechaInf, fechaSup, horaInf, horaSup);
        assertEquals(2, res.size());
        assertTrue(reservaService.findReservas("Guadalajara","12122022",null,null,null).isEmpty());
        // Fecha con formato incorrecto
        assertThrows(UnprocesableException.class, () -> reservaService.findReservas(nombreDestino1, "aa122022", null, null, null));

        // Eliminamos las reservas creadas
        reservaService.del(rsv1.getIdReserva());
        reservaService.del(rsv2.getIdReserva());
    }

    @Test
    @Transactional
    void testAddDelInputDto() throws ParseException {
        Date fecha = sdf3.parse(fechaStr);
        ReservaInputDto inputDto = new ReservaInputDto(idDestino1,"nombre1","apellido1","111111",email1,fecha,horaSalida);
        // Añadimos una reserva en el primer autobus.
        ReservaOutputDto rsv1 = reservaService.add(inputDto);
        assertEquals(1, reservaService.findAll().size());
        assertEquals("ACEPTADA", rsv1.getStatus());
        // La borramos
        reservaService.del(rsv1.getIdReserva());
        // Ponemos a 0 las plazas libres
        String idbus = autobusService.getIdBus(idDestino1,fecha,horaSalida);
        autobusService.setPlazas(idbus,0);
        // Quedan 0 plazas: la reserva se almacena con status RECHAZADA
        ReservaOutputDto rsv2 = reservaService.add(inputDto);
        assertEquals("RECHAZADA", rsv2.getStatus());
        assertEquals(1, reservaService.findAll().size());
        // Borramos la reserva
        reservaService.del(rsv2.getIdReserva());
    }

    @Test
    void testAddOutputDto() throws ParseException {
        // Añadimos una reserva
        Date fecha = sdf3.parse(fechaStr);
        ReservaInputDto inputDto = new ReservaInputDto(idDestino1,"nombre1","apellido1","111111",email1,fecha,horaSalida);
        // Añadimos una reserva en el primer autobus.
        ReservaOutputDto rsv1 = reservaService.add(inputDto);
        // Si intentamos añadir una reserva ya existente, devuelve null
        ReservaOutputDto rsvDto = new ReservaOutputDto(
                rsv1.getIdReserva(), rsv1.getIdentificador(), nombreDestino1,
                "nombre1", "apellido1", "11111", email1, fechaStr, horaSalida, "ACEPTADA");
        ReservaOutputDto res = reservaService.add(rsvDto);
        assertNull(res);
        //Sino la añade y devuelve el ReservaOutputDto
        reservaService.del(rsv1.getIdReserva());
        ReservaOutputDto rsv2 = reservaService.add(rsvDto);
        assertNotNull(rsv2);
        // La borramos
        reservaService.del(rsv2.getIdReserva());
    }

    @AfterAll
    void cleaning() throws ParseException {
        Date fecha = sdf3.parse(fechaStr);
        autobusService.del(autobusService.getIdBus(idDestino1, fecha, horaSalida));
        autobusService.del(autobusService.getIdBus(idDestino1, fecha, horaSalida+1));
        destinoService.del(idDestino1);
        assertTrue(reservaService.findAll().isEmpty());
        assertTrue(autobusService.findAll().isEmpty());
        assertTrue(destinoService.findAll().isEmpty());
    }

}

