package br.com.fiap.alertacheia.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "TB_SENSOR")
public class Sensor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 30)
    private String codigo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private TipoSensor tipo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_bairro", nullable = false)
    private Bairro bairro;

    @Column(name = "limite_alerta", nullable = false, precision = 8, scale = 2)
    private BigDecimal limiteAlerta;

    @Column(name = "limite_critico", nullable = false, precision = 8, scale = 2)
    private BigDecimal limiteCritico;

    @Column(nullable = false)
    private boolean ativo = true;

    public Sensor() {}

    public Sensor(String codigo, TipoSensor tipo, Bairro bairro, BigDecimal limiteAlerta, BigDecimal limiteCritico) {
        this.codigo = codigo;
        this.tipo = tipo;
        this.bairro = bairro;
        this.limiteAlerta = limiteAlerta;
        this.limiteCritico = limiteCritico;
    }

    /** Unidade de medida exibida nas mensagens de alerta. */
    public String unidade() {
        return tipo == TipoSensor.NIVEL_RIO ? "m" : "mm/h";
    }

    public Long getId() { return id; }
    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
    public TipoSensor getTipo() { return tipo; }
    public void setTipo(TipoSensor tipo) { this.tipo = tipo; }
    public Bairro getBairro() { return bairro; }
    public void setBairro(Bairro bairro) { this.bairro = bairro; }
    public BigDecimal getLimiteAlerta() { return limiteAlerta; }
    public void setLimiteAlerta(BigDecimal limiteAlerta) { this.limiteAlerta = limiteAlerta; }
    public BigDecimal getLimiteCritico() { return limiteCritico; }
    public void setLimiteCritico(BigDecimal limiteCritico) { this.limiteCritico = limiteCritico; }
    public boolean isAtivo() { return ativo; }
    public void setAtivo(boolean ativo) { this.ativo = ativo; }
}
