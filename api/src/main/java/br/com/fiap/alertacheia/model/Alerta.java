package br.com.fiap.alertacheia.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "TB_ALERTA")
public class Alerta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_bairro", nullable = false)
    private Bairro bairro;

    // 1 leitura gera no maximo 1 alerta (UNIQUE no banco)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_leitura", unique = true)
    private Leitura leitura;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Severidade severidade;

    @Column(nullable = false, length = 300)
    private String mensagem;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private StatusAlerta status = StatusAlerta.ATIVO;

    @Column(name = "criado_em", nullable = false)
    private LocalDateTime criadoEm;

    @PrePersist
    void prePersist() {
        if (criadoEm == null) criadoEm = LocalDateTime.now();
    }

    public Alerta() {}

    public Alerta(Bairro bairro, Leitura leitura, Severidade severidade, String mensagem) {
        this.bairro = bairro;
        this.leitura = leitura;
        this.severidade = severidade;
        this.mensagem = mensagem;
    }

    public Long getId() { return id; }
    public Bairro getBairro() { return bairro; }
    public Leitura getLeitura() { return leitura; }
    public Severidade getSeveridade() { return severidade; }
    public String getMensagem() { return mensagem; }
    public StatusAlerta getStatus() { return status; }
    public void setStatus(StatusAlerta status) { this.status = status; }
    public LocalDateTime getCriadoEm() { return criadoEm; }
}
