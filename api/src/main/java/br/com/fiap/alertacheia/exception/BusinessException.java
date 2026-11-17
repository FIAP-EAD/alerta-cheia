package br.com.fiap.alertacheia.exception;

/** Regra de negocio violada (HTTP 400). */
public class BusinessException extends RuntimeException {
    public BusinessException(String message) {
        super(message);
    }
}
