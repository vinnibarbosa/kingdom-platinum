package com.br.pokefichas.domain.core.usuario.model.enums;

import com.br.pokefichas.commons.enums.EnumDescription;
import com.br.pokefichas.commons.enums.EnumTypeValue;
import jakarta.persistence.EnumeratedValue;

public enum Perfil implements EnumTypeValue<Perfil, String>, EnumDescription {

    ADMIN("A", "Administrador"),
    DONO("D", "Dono"),
    GERENTE("G", "Gerente"),
    OPERADOR("O", "Operador");

    @EnumeratedValue
    private final String value;
    private final String descricao;

    Perfil(final String value, final String descricao) {
        this.value = value;
        this.descricao = descricao;
    }

    @Override
    public String getDescription() {
        return descricao;
    }

    @Override
    public String toValue() {
        return value;
    }

    @Override
    public Perfil fromValue(final String value) {
        return EnumTypeValue.EnumTypeString.fromValue(Perfil.class, value);
    }

    @Override
    public String toString() {
        return value;
    }
}
