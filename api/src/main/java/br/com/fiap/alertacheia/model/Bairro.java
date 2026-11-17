package br.com.fiap.alertacheia.model;

import jakarta.persistence.*;

@Entity
@Table(name = "TB_BAIRRO")
public class Bairro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nome;

    @Column(nullable = false, length = 100)
    private String cidade;

    @Enumerated(EnumType.STRING)
    @Column(name = "nivel_risco", nullable = false, length = 10)
    private NivelRisco nivelRisco;

    public Bairro() {}

    public Bairro(String nome, String cidade, NivelRisco nivelRisco) {
        this.nome = nome;
        this.cidade = cidade;
        this.nivelRisco = nivelRisco;
    }

    public Long getId() { return id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getCidade() { return cidade; }
    public void setCidade(String cidade) { this.cidade = cidade; }
    public NivelRisco getNivelRisco() { return nivelRisco; }
    public void setNivelRisco(NivelRisco nivelRisco) { this.nivelRisco = nivelRisco; }
}
