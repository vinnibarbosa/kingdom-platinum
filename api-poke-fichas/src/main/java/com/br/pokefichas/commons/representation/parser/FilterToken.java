package com.br.pokefichas.commons.representation.parser;

public record FilterToken(FilterTokenType type, String value, int position) {

    @Override
    public String toString() {
        return type + "(" + value + ")@" + position;
    }

    public enum FilterTokenType {
        IDENTIFIER,
        STRING,
        NUMBER,
        EQ,
        NEQ,
        GT,
        GTE,
        LT,
        LTE,
        IN,
        LIKE,
        AND,
        OR,
        LPAREN,
        RPAREN,
        COMMA,
        EOF
    }
}
