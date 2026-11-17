package br.com.fiap.alertacheia.exception;

/** Conflito com o estado atual, ex.: email ja cadastrado (HTTP 409). */
public class ConflictException extends RuntimeException {
    public ConflictException(String message) {
        super(message);
    }
}
