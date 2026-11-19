package com.cooperativa.gestioncooperativa.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String manejarValidacion(IllegalArgumentException exception, Model model) {
        model.addAttribute("codigo", 400);
        model.addAttribute("mensaje", exception.getMessage());
        return "error";
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public String manejarAccesoDenegado(AccessDeniedException exception, Model model) {
        model.addAttribute("codigo", 403);
        model.addAttribute("mensaje", "No tiene permisos para realizar esta operación.");
        return "error";
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public String manejarIntegridad(DataIntegrityViolationException exception, Model model) {
        model.addAttribute("codigo", 409);
        model.addAttribute("mensaje", "La operación no puede completarse porque existen datos relacionados o duplicados.");
        return "error";
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String manejarErrorGeneral(Exception exception, Model model) {
        LOGGER.error("Error inesperado en una solicitud web", exception);
        model.addAttribute("codigo", 500);
        model.addAttribute("mensaje", "Ocurrió un error inesperado. Intente nuevamente o contacte al administrador.");
        return "error";
    }
}
