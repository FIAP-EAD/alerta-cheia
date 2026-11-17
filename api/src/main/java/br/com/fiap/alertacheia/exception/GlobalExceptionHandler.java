package br.com.fiap.alertacheia.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Converte excecoes em respostas JSON padronizadas, com o status HTTP correto.
 * Boa pratica de seguranca: nunca devolve stack trace ou detalhes internos ao cliente.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroResponse> validacao(MethodArgumentNotValidException ex) {
        Map<String, String> campos = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(e -> campos.putIfAbsent(e.getField(), e.getDefaultMessage()));
        var body = new ErroResponse(LocalDateTime.now(), 400, "Bad Request", "Dados de entrada invalidos", campos);
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
    public ResponseEntity<ErroResponse> jsonInvalido(Exception ex) {
        return erro(HttpStatus.BAD_REQUEST, "Requisicao mal formatada ou com valor de tipo invalido");
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErroResponse> negocio(BusinessException ex) {
        return erro(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ErroResponse> naoAutorizado(UnauthorizedException ex) {
        return erro(HttpStatus.UNAUTHORIZED, ex.getMessage());
    }

    @ExceptionHandler({ForbiddenException.class, AccessDeniedException.class})
    public ResponseEntity<ErroResponse> proibido(RuntimeException ex) {
        return erro(HttpStatus.FORBIDDEN, "Voce nao tem permissao para esta acao");
    }

    @ExceptionHandler({NotFoundException.class, NoResourceFoundException.class})
    public ResponseEntity<ErroResponse> naoEncontrado(Exception ex) {
        String msg = ex instanceof NotFoundException ? ex.getMessage() : "Rota nao encontrada";
        return erro(HttpStatus.NOT_FOUND, msg);
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ErroResponse> conflito(ConflictException ex) {
        return erro(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErroResponse> integridade(DataIntegrityViolationException ex) {
        return erro(HttpStatus.CONFLICT, "Operacao viola uma regra do banco (registro duplicado ou com vinculos)");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErroResponse> generico(Exception ex) {
        log.error("Erro inesperado", ex); // detalhe fica so no log do servidor
        return erro(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno. Tente novamente mais tarde.");
    }

    private ResponseEntity<ErroResponse> erro(HttpStatus status, String mensagem) {
        return ResponseEntity.status(status).body(ErroResponse.of(status.value(), status.getReasonPhrase(), mensagem));
    }
}
