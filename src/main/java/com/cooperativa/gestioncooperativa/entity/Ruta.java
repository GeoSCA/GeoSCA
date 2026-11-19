package com.cooperativa.gestioncooperativa.entity;

import java.util.LinkedHashSet;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.JoinTable;
import jakarta.persistence.FetchType;
import lombok.Data;

@Entity
@Table(name = "ruta")
@Data
public class Ruta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_ruta")
    private Long idRuta;

    private String nombre;

    private String origen;

    private String destino;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_parroquia_origen")
    private Parroquia parroquiaOrigen;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_parroquia_destino")
    private Parroquia parroquiaDestino;

    private String paradasIntermedias;

    @ManyToMany
    @JoinTable(name = "ruta_parada",
            joinColumns = @JoinColumn(name = "id_ruta"),
            inverseJoinColumns = @JoinColumn(name = "id_parroquia"))
    private Set<Parroquia> paradas = new LinkedHashSet<>();

    private Double distanciaKm;

    private String descripcion;

    private String estado;

    @ManyToOne
    @JoinColumn(name = "id_cooperativa")
    private Cooperativa cooperativa;
}
