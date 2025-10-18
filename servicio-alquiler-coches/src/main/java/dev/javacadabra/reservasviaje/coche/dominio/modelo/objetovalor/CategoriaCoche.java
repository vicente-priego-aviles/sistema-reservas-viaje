package dev.javacadabra.reservasviaje.coche.dominio.modelo.objetovalor;

import org.jmolecules.ddd.annotation.ValueObject;

@ValueObject
public enum CategoriaCoche {
    ECONOMICO,
    COMPACTO,
    SEDAN,
    SUV,
    PREMIUM
}
