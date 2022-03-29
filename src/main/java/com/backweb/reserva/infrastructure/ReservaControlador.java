package com.backweb.reserva.infrastructure;

import com.backweb.reserva.application.ReservaService;
import com.backweb.reserva.domain.Reserva;
import lombok.extern.slf4j.Slf4j;
import org.simplejavamail.api.email.Email;
import org.simplejavamail.api.mailer.AsyncResponse;
import org.simplejavamail.api.mailer.Mailer;
import org.simplejavamail.api.mailer.config.TransportStrategy;
import org.simplejavamail.email.EmailBuilder;
import org.simplejavamail.mailer.MailerBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

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

    String token="";

    private SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
    private Mailer mailer = MailerBuilder
            .withSMTPServer("smtp.mailtrap.io", 2525, "401dd4926d850f", "738ee9ea1b7e39")
            .withTransportStrategy(TransportStrategy.SMTP).buildMailer();

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
                outDto.getCiudadDestino()+" "+outDto.getFechaReserva()+" "+
                String.format("%02dH ",outDto.getHoraReserva().intValue())+
                outDto.getNombre()+" "+outDto.getApellido()+
                " status: "+outDto.getStatus());

        if (Objects.equals(outDto.getStatus(), "ACEPTADA")) {
            sendMessage(outDto);
            return new ResponseEntity<>(outDto,HttpStatus.OK);
        }
        return new ResponseEntity<>(outDto,HttpStatus.NOT_ACCEPTABLE);
        // Falta mensaje al back de la empresa
        // y listener para escuchar mensaje de confirmación.
    }

    @GetMapping("disponible/{destino}")
    public ResponseEntity<List<ReservaDisponibleOutputDto>> getPlazasLibres(
            @PathVariable String destino,
            @RequestParam(name="fechaInferior") String fechaInferior,
            @RequestParam(name="fechaSuperior", required = false) String fechaSuperior,
            @RequestParam(name="horaInferior", required = false) String horaInferior,
            @RequestParam(name="horaSuperior", required = false) String horaSuperior)
    {
        return new ResponseEntity<>(reservaService.findDisponible(destino,fechaInferior,fechaSuperior,horaInferior,horaSuperior),HttpStatus.OK);
    }

    // Llama a backempresa con el usuario y contraseña para obtener un token válido, que se almacena en this.token
    @PostMapping("login")
    public ResponseEntity<Void> login(@RequestHeader("user") String user, @RequestHeader("password") String pwd){
        String newToken = "";
        HttpHeaders headers = new HttpHeaders();
        headers.set("user",user);
        headers.set("password",pwd);
        HttpEntity<Object> request = new HttpEntity<>(headers);
        ResponseEntity<String> response = new RestTemplate().exchange(
                "http://localhost:8081/api/v0/token",
                HttpMethod.POST,
                request,
                String.class);
        if (response.getStatusCode()==HttpStatus.OK) {
            this.token = response.getBody();
            return new ResponseEntity<>(HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.FORBIDDEN);
    }

    @GetMapping("reserva/{ciudadDestino}")
    public ResponseEntity<ReservaOutputDto> getReservas(@PathVariable String ciudadDestino)
    {
        //TODO:Implementa seguridad, llamando al servidor de la empresa al endpoint ‘checkSeguridad’.
        // Devolverá las reservas realizadas en la web.
        // Antes de llamar a este endpoint se debe llamar a /login con usuario y contraseña válidos
        // Llamada a backempresa para comprobar el token. Si es válido se piden las reservas
        return null;
    }

    private void sendMessage(ReservaOutputDto outDto){

        Email email = EmailBuilder.startingBlank()
                .from("From", "backweb@vtravel.com")
                .to("To", outDto.getEmail())
                .withSubject("RESERVA "+outDto.getCiudadDestino())
                .withPlainText("Se ha recibido su reserva: \nDestino: "+outDto.getCiudadDestino()+
                        "\nFecha: "+outDto.getFechaReserva()+
                        "\nHora: "+outDto.getHoraReserva()+
                        "\nIdentificador: "+outDto.getIdReserva()+
                        "\n\nGracias por confiar en Virtual-Travel")
                .buildEmail();

        AsyncResponse response = mailer.sendMail(email,true); // True for async message

        assert response != null;
        response.onSuccess(()->System.out.println("Message sent"));
    }
}
