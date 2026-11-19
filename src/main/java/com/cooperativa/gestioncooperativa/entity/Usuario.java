package com.cooperativa.gestioncooperativa.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "usuario")
@Data
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario")
    private Long idUsuario;

    // Campos simples
    private String nombre;

    private String apellido;

    @Column(unique = true)
    private String correo;

    @Column(unique = true)
    private String username;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    private Boolean estado;

    // Relaciones
    @ManyToOne
    @JoinColumn(name = "id_rol")
    private Rol rol;

    @ManyToOne
    @JoinColumn(name = "id_cooperativa")
    private Cooperativa cooperativa; 
}