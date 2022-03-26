package com.backweb;

import com.backweb.autobus.application.AutobusService;
import com.backweb.autobus.infrastructure.AutobusInputDto;
import com.backweb.destino.application.DestinoService;
import com.backweb.destino.domain.Destino;
import com.backweb.destino.infrastructure.DestinoInputDto;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static java.sql.Date.valueOf;

@SpringBootApplication
public class BackwebApplication {

	public static void main(String[] args) {
		SpringApplication.run(BackwebApplication.class, args);
	}

	@Bean
	CommandLineRunner init(
			DestinoService destinoService,
			AutobusService autobusService)
	{
		return args ->
		{
			final int PLAZAS_LIBRES = 5; // Provisional.
			// Viajes a las ciudades indicadas para todos los días del mes 04 de 2022 a las horas indicadas.
			// En total se añadirán 4x4x30 = 480 registros a Autobus
			String[] destinos = {"Valencia","Madrid","Barcelona","Bilbao"};
			Float[] salidas = { 8f, 12f, 16f, 20f };
			String anyo = "2022";
			String mes = "04";
			DateTimeFormatter dtf = DateTimeFormatter.ofPattern("ddMMyyyy");
			DestinoInputDto dsInputDto = new DestinoInputDto();
			for (String destino:destinos) {
				dsInputDto.setNombre(destino);
				Destino ds = destinoService.add(dsInputDto);
				long idDestino = ds.getIdDestino();
				for (int i=1; i<=30; i++) {
					String dateInString = String.format("%02d",i)+mes+anyo;
					LocalDate fecha = LocalDate.parse(dateInString, dtf);
					for (Float hora:salidas) {
						AutobusInputDto busInputDto = new AutobusInputDto();
						busInputDto.setIdDestino(idDestino);
						busInputDto.setFecha(valueOf(fecha));
						busInputDto.setHoraSalida(hora);
						// Falta: plazas libres deberá tomarse del back de la empresa.
						busInputDto.setPlazasLibres(PLAZAS_LIBRES);
						autobusService.add(busInputDto);
					}
				}

			}
		};
	}

}