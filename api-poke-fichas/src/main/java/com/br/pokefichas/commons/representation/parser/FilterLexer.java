package com.br.pokefichas.commons.representation.parser;

import com.br.pokefichas.commons.representation.exception.RepresentationException;
import com.br.pokefichas.commons.representation.parser.FilterToken.FilterTokenType;

import java.util.ArrayList;
import java.util.List;

final class FilterLexer {

    private final String input;
    private int pos;

    FilterLexer(final String input) {
        this.input = input;
        this.pos = 0;
    }

    List<FilterToken> tokenize() {
        final List<FilterToken> tokens = new ArrayList<>();
        while (pos < input.length()) {
            skipWhitespace();
            if (pos >= input.length()) {
                break;
            }

            final int start = pos;
            final char ch = input.charAt(pos);

            final FilterToken symbolToken = readSymbolToken(ch, start);
            if (symbolToken != null) {
                tokens.add(symbolToken);
                continue;
            }

            if (ch == '"' || ch == '\'') {
                tokens.add(readString(ch));
                continue;
            }

            if (Character.isDigit(ch) || (ch == '-' && Character.isDigit(peekAt(1)))) {
                tokens.add(readNumber(start));
                continue;
            }

            if (Character.isLetter(ch) || ch == '_') {
                tokens.add(readIdentifierOrKeyword(start));
                continue;
            }

            throw new RepresentationException("Caractere inesperado '" + ch + "' na posicao " + pos);
        }
        tokens.add(new FilterToken(FilterTokenType.EOF, "", pos));
        return tokens;
    }

    private FilterToken readString(final char quote) {
        final int start = pos;
        pos++;
        final StringBuilder sb = new StringBuilder();
        while (pos < input.length() && input.charAt(pos) != quote) {
            if (input.charAt(pos) == '\\' && pos + 1 < input.length()) {
                pos++;
                sb.append(input.charAt(pos));
                pos++;
                continue;
            }
            sb.append(input.charAt(pos));
            pos++;
        }
        if (pos >= input.length()) {
            throw new RepresentationException("String nao fechada na posicao " + start);
        }
        pos++;
        return new FilterToken(FilterTokenType.STRING, sb.toString(), start);
    }

    private FilterToken readNumber(final int start) {
        final StringBuilder sb = new StringBuilder();
        if (input.charAt(pos) == '-') {
            sb.append(input.charAt(pos));
            pos++;
        }
        while (pos < input.length() && (Character.isDigit(input.charAt(pos)) || input.charAt(pos) == '.')) {
            sb.append(input.charAt(pos++));
        }
        return new FilterToken(FilterTokenType.NUMBER, sb.toString(), start);
    }

    private FilterToken readIdentifierOrKeyword(final int start) {
        final StringBuilder sb = new StringBuilder();
        while (pos < input.length() && (Character.isLetterOrDigit(input.charAt(pos))
                || input.charAt(pos) == '_' || input.charAt(pos) == '.')) {
            sb.append(input.charAt(pos++));
        }
        final String word = sb.toString();
        return switch (word.toLowerCase()) {
            case "and" -> new FilterToken(FilterTokenType.AND, word, start);
            case "or" -> new FilterToken(FilterTokenType.OR, word, start);
            case "in" -> new FilterToken(FilterTokenType.IN, word, start);
            case "like" -> new FilterToken(FilterTokenType.LIKE, word, start);
            default -> new FilterToken(FilterTokenType.IDENTIFIER, word, start);
        };
    }

    private FilterToken readSymbolToken(final char ch, final int start) {
        if (ch == '(') {
            pos++;
            return new FilterToken(FilterTokenType.LPAREN, "(", start);
        }
        if (ch == ')') {
            pos++;
            return new FilterToken(FilterTokenType.RPAREN, ")", start);
        }
        if (ch == ',') {
            pos++;
            return new FilterToken(FilterTokenType.COMMA, ",", start);
        }
        if (ch == '!' && peek() == '=') {
            pos += 2;
            return new FilterToken(FilterTokenType.NEQ, "!=", start);
        }
        if (ch == '>' && peek() == '=') {
            pos += 2;
            return new FilterToken(FilterTokenType.GTE, ">=", start);
        }
        if (ch == '<' && peek() == '=') {
            pos += 2;
            return new FilterToken(FilterTokenType.LTE, "<=", start);
        }
        if (ch == '=') {
            pos++;
            return new FilterToken(FilterTokenType.EQ, "=", start);
        }
        if (ch == '>') {
            pos++;
            return new FilterToken(FilterTokenType.GT, ">", start);
        }
        if (ch == '<') {
            pos++;
            return new FilterToken(FilterTokenType.LT, "<", start);
        }
        return null;
    }

    private void skipWhitespace() {
        while (pos < input.length() && Character.isWhitespace(input.charAt(pos))) {
            pos++;
        }
    }

    private char peek() {
        return pos + 1 < input.length() ? input.charAt(pos + 1) : '\0';
    }

    private char peekAt(final int offset) {
        return pos + offset < input.length() ? input.charAt(pos + offset) : '\0';
    }
}
