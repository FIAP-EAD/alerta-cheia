package br.com.fiap.alertacheia.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "TB_RELATO")
public class Relato {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_bairro", nullable = false)
    private Bairro bairro;

    @Column(nullable = false, length = 1000)
    private String descricao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 12)
    private StatusRelato status = StatusRelato.ABERTO;

    @Column(name = "criado_em", nullable = false)
    private LocalDateTime criadoEm;

    @PrePersist
    void prePersist() {
        if (criadoEm == null) criadoEm = LocalDateTime.now();
    }

    public Relato() {}

    public Relato(Usuario usuario, Bairro bairro, String descricao) {
        this.usuario = usuario;
        this.bairro = bairro;
        this.descricao = descricao;
    }

    public Long getId() { return id; }
    public Usuario getUsuario() { return usuario; }
    public Bairro getBairro() { return bairro; }
    public String getDescricao() { return descricao; }
    public StatusRelato getStatus() { return status; }
    public void setStatus(StatusRelato status) { this.status = status; }
    public LocalDateTime getCriadoEm() { return criadoEm; }
}
