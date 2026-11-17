package br.com.fiap.alertacheia.model;

import jakarta.persistence.*;

@Entity
@Table(name = "TB_ABRIGO")
public class Abrigo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nome;

    @Column(nullable = false, length = 200)
    private String endereco;

    @Column(nullable = false)
    private Integer capacidade;

    @Column(name = "vagas_ocupadas", nullable = false)
    private Integer vagasOcupadas = 0;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_bairro", nullable = false)
    private Bairro bairro;

    public Abrigo() {}

    public Abrigo(String nome, String endereco, Integer capacidade, Integer vagasOcupadas, Bairro bairro) {
        this.nome = nome;
        this.endereco = endereco;
        this.capacidade = capacidade;
        this.vagasOcupadas = vagasOcupadas;
        this.bairro = bairro;
    }

    public int vagasLivres() {
        return capacidade - vagasOcupadas;
    }

    public Long getId() { return id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getEndereco() { return endereco; }
    public void setEndereco(String endereco) { this.endereco = endereco; }
    public Integer getCapacidade() { return capacidade; }
    public void setCapacidade(Integer capacidade) { this.capacidade = capacidade; }
    public Integer getVagasOcupadas() { return vagasOcupadas; }
    public void setVagasOcupadas(Integer vagasOcupadas) { this.vagasOcupadas = vagasOcupadas; }
    public Bairro getBairro() { return bairro; }
    public void setBairro(Bairro bairro) { this.bairro = bairro; }
}
