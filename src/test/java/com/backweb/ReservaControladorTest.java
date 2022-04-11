package com.backweb;

import com.backweb.autobus.application.AutobusService;
import com.backweb.autobus.infrastructure.AutobusInputDto;
import com.backweb.destino.application.DestinoService;
import com.backweb.destino.infrastructure.DestinoInputDto;
import com.backweb.reserva.application.ReservaService;
import com.backweb.reserva.domain.Reserva;
import com.backweb.reserva.infrastructure.ReservaDisponibleOutputDto;
import com.backweb.reserva.infrastructure.ReservaInputDto;
import com.backweb.reserva.infrastructure.ReservaOutputDto;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import javax.transaction.Transactional;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ReservaControladorTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ReservaService reservaService;

    @Autowired
    private DestinoService destinoService;

    @Autowired
    private AutobusService autobusService;

    @Autowired
    private SimpleDateFormat sdf2;

    private String idDestino="VAL";
    private final String nombreDestino="Valencia";
    private final String fechaStr="01052022";
    private final Float hora = 12F;
    private String email="email@email.com";
    private String idBus;

    @BeforeAll
    void starting() throws Exception {
        Date fecha = sdf2.parse(fechaStr);
        destinoService.add(new DestinoInputDto(idDestino,nombreDestino));
        idBus = autobusService.add(new AutobusInputDto(idDestino, fecha, hora, 2)).getId();
    }

    @Test
    @DisplayName("Testing POST reserva")
    void testAddReserva() throws Exception {
        Date fecha = sdf2.parse(fechaStr);
        ReservaInputDto inputDto = new ReservaInputDto(idDestino,"nombre2","apellido","111111",email,fecha,hora);
        Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();
        MvcResult res = mockMvc.perform(post("/api/v0/reserva/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(gson.toJson(inputDto)))
                .andExpect(status().isOk()).andReturn();
        String contenido = res.getResponse().getContentAsString();
        ReservaOutputDto outDto = new ObjectMapper().readValue(contenido, new TypeReference<>() {	}); // Use TypeReference to map the List.
        Assertions.assertEquals(nombreDestino, outDto.getCiudadDestino());
        reservaService.del(outDto.getIdReserva()); // La eliminamos
        assertEquals(0, reservaService.findAll().size());
    }

    @Test
    @DisplayName("Testing GET plazas")
    void testGetPlazasLibres() throws Exception {
        MvcResult res = mockMvc.perform(get("/api/v0/disponible/"+nombreDestino)
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("fechaInferior",fechaStr))
                .andExpect(status().isOk()).andReturn();
        String contenido = res.getResponse().getContentAsString();
        List<ReservaDisponibleOutputDto> lista = new ObjectMapper().readValue(contenido, new TypeReference<>() {	}); // Use TypeReference to map the List.
        assertFalse(lista.isEmpty());
        Assertions.assertEquals(2, lista.get(0).getPlazasLibres());
        res = mockMvc.perform(get("/api/v0/disponible/"+"Guadalajara")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("fechaInferior",fechaStr))
                .andExpect(status().isOk()).andReturn();
        contenido = res.getResponse().getContentAsString();
        lista = new ObjectMapper().readValue(contenido, new TypeReference<>() {	});
        assertTrue(lista.isEmpty());
    }

    @AfterAll
    void cleaning() {
        List<Reserva> res = reservaService.findAll();
        for (Reserva e:res) reservaService.del(e.getIdReserva());
        autobusService.del(idBus);
        destinoService.del(idDestino);
    }
}
