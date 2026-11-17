package br.com.fiap.alertacheia.exception;

/** Recurso nao encontrado (HTTP 404). */
public class NotFoundException extends RuntimeException {
    public NotFoundException(String message) {
        super(message);
    }
}
