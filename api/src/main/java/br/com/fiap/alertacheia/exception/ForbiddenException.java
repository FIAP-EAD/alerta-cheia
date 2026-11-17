package br.com.fiap.alertacheia.exception;

/** Usuario autenticado sem permissao para o recurso (HTTP 403). */
public class ForbiddenException extends RuntimeException {
    public ForbiddenException(String message) {
        super(message);
    }
}
