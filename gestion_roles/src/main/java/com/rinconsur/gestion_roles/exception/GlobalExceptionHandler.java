package com.rinconsur.gestion_roles.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import com.rinconsur.gestion_roles.dto.MensajeResponse;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<MensajeResponse> noEncontrado(RecursoNoEncontradoException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MensajeResponse(e.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<MensajeResponse> datosInvalidos(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(new MensajeResponse(e.getMessage()));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<MensajeResponse> cuerpoInvalido(HttpMessageNotReadableException e) {
        log.warn("Cuerpo de la peticion invalido: {}", e.getMessage());
        return ResponseEntity.badRequest().body(new MensajeResponse("El cuerpo de la petición no es válido."));
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<MensajeResponse> faltaParametro(MissingServletRequestParameterException e) {
        return ResponseEntity.badRequest().body(new MensajeResponse("Falta el parámetro: " + e.getParameterName()));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<MensajeResponse> tipoInvalido(MethodArgumentTypeMismatchException e) {
        return ResponseEntity.badRequest().body(new MensajeResponse("Valor inválido para el parámetro: " + e.getName()));
    }
}
