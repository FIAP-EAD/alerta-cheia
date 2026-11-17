package br.com.fiap.alertacheia.exception;

/** Credenciais invalidas (HTTP 401). */
public class UnauthorizedException extends RuntimeException {
    public UnauthorizedException(String message) {
        super(message);
    }
}
