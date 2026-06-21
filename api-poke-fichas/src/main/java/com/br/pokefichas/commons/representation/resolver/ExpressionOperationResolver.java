package com.br.pokefichas.commons.representation.resolver;

import com.br.pokefichas.commons.representation.exception.RepresentationException;
import com.br.pokefichas.commons.representation.model.CustomFilterDescriptor;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.DateExpression;
import com.querydsl.core.types.dsl.DateTimeExpression;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.core.types.dsl.SimpleExpression;
import com.querydsl.core.types.dsl.StringExpression;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.Collection;
import java.util.List;
import java.util.Set;

@Component
public class ExpressionOperationResolver {

    private static final Set<String> VALID_BOOLEAN_LITERALS = Set.of("true", "false");

    public Predicate resolve(final SimpleExpression<?> expression, final String operator, final String rawValue) {
        final Class<?> type = expression.getType();

        if (expression instanceof StringExpression se) {
            return resolveString(se, operator, rawValue);
        }
        if (expression instanceof NumberExpression) {
            return resolveNumber(expression, operator, rawValue, type);
        }
        if (expression instanceof DateTimeExpression && OffsetDateTime.class.isAssignableFrom(type)) {
            @SuppressWarnings("unchecked")
            final DateTimeExpression<OffsetDateTime> dtExpr = (DateTimeExpression<OffsetDateTime>) expression;
            return resolveOffsetDateTime(dtExpr, operator, rawValue);
        }
        if (expression instanceof DateTimeExpression && LocalDateTime.class.isAssignableFrom(type)) {
            @SuppressWarnings("unchecked")
            final DateTimeExpression<LocalDateTime> dtExpr = (DateTimeExpression<LocalDateTime>) expression;
            return resolveLocalDateTime(dtExpr, operator, rawValue);
        }
        if (expression instanceof DateExpression && LocalDate.class.isAssignableFrom(type)) {
            @SuppressWarnings("unchecked")
            final DateExpression<LocalDate> dateExpr = (DateExpression<LocalDate>) expression;
            return resolveLocalDate(dateExpr, operator, rawValue);
        }
        if (Boolean.class.isAssignableFrom(type) || boolean.class.isAssignableFrom(type)) {
            return resolveBoolean(expression, operator, rawValue);
        }
        if (type.isEnum()) {
            return resolveEnum(expression, operator, rawValue, type);
        }

        throw new RepresentationException("Tipo nao suportado para filtro: " + type.getName());
    }

    @SuppressWarnings("unchecked")
    public Predicate resolveCustom(final CustomFilterDescriptor<?> descriptor,
                                   final String operator,
                                   final String rawValue) {
        if (!"=".equals(operator)) {
            throw new RepresentationException(
                    "Operador '" + operator + "' nao suportado para filtro customizado '"
                    + descriptor.getAlias() + "'");
        }
        final Object value = parseValue(descriptor.getType(), rawValue);
        return ((CustomFilterDescriptor<Object>) descriptor).resolve(value);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public Predicate resolveIn(final SimpleExpression<?> expression, final List<String> rawValues) {
        final Class<?> type = expression.getType();

        if (expression instanceof StringExpression se) {
            return se.in(rawValues);
        }
        if (expression instanceof NumberExpression numExpr) {
            final List<Number> numbers = rawValues.stream()
                    .map(v -> parseNumber(type, v))
                    .toList();
            return numExpr.in((Collection) numbers);
        }
        if (expression instanceof DateTimeExpression && OffsetDateTime.class.isAssignableFrom(type)) {
            final List<OffsetDateTime> vals = rawValues.stream()
                    .map(v -> parseOffsetDateTime(v))
                    .toList();
            return ((SimpleExpression<OffsetDateTime>) expression).in(vals);
        }
        if (expression instanceof DateTimeExpression && LocalDateTime.class.isAssignableFrom(type)) {
            final List<LocalDateTime> vals = rawValues.stream()
                    .map(v -> parseLocalDateTime(v))
                    .toList();
            return ((SimpleExpression<LocalDateTime>) expression).in(vals);
        }
        if (expression instanceof DateExpression && LocalDate.class.isAssignableFrom(type)) {
            final List<LocalDate> vals = rawValues.stream()
                    .map(v -> parseLocalDate(v))
                    .toList();
            return ((SimpleExpression<LocalDate>) expression).in(vals);
        }
        if (Boolean.class.isAssignableFrom(type) || boolean.class.isAssignableFrom(type)) {
            final List<Boolean> vals = rawValues.stream()
                    .map(v -> parseBoolean(v))
                    .toList();
            return ((SimpleExpression<Boolean>) expression).in(vals);
        }
        if (type.isEnum()) {
            final List<?> enums = rawValues.stream()
                    .map(v -> parseEnum(type, v))
                    .toList();
            return ((SimpleExpression) expression).in((Collection) enums);
        }

        throw new RepresentationException("Tipo nao suportado para operador 'in': " + type.getName());
    }

    private Predicate resolveString(final StringExpression se, final String operator, final String rawValue) {
        return switch (operator) {
            case "=" -> se.equalsIgnoreCase(rawValue);
            case "!=" -> se.equalsIgnoreCase(rawValue).not();
            case "like" -> rawValue.contains("%") ? se.likeIgnoreCase(rawValue) : se.containsIgnoreCase(rawValue);
            case ">" -> se.gt(rawValue);
            case ">=" -> se.goe(rawValue);
            case "<" -> se.lt(rawValue);
            case "<=" -> se.loe(rawValue);
            default -> throw new RepresentationException(
                    "Operador '" + operator + "' nao suportado para tipo String");
        };
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private Predicate resolveNumber(final SimpleExpression<?> expression, final String operator, final String rawValue,
                                    final Class<?> type) {
        final NumberExpression numExpr = (NumberExpression) expression;
        final Number val = parseNumber(type, rawValue);
        return switch (operator) {
            case "=" -> numExpr.eq(val);
            case "!=" -> numExpr.ne(val);
            case ">" -> numExpr.gt(val);
            case ">=" -> numExpr.goe(val);
            case "<" -> numExpr.lt(val);
            case "<=" -> numExpr.loe(val);
            default -> throw new RepresentationException(
                    "Operador '" + operator + "' nao suportado para tipo numerico");
        };
    }

    private Predicate resolveOffsetDateTime(final DateTimeExpression<OffsetDateTime> dtExpr, final String operator,
                                            final String rawValue) {
        final OffsetDateTime val = parseOffsetDateTime(rawValue);
        return switch (operator) {
            case "=" -> dtExpr.eq(val);
            case "!=" -> dtExpr.ne(val);
            case ">" -> dtExpr.after(val);
            case ">=" -> dtExpr.goe(val);
            case "<" -> dtExpr.before(val);
            case "<=" -> dtExpr.loe(val);
            default -> throw new RepresentationException(
                    "Operador '" + operator + "' nao suportado para tipo OffsetDateTime");
        };
    }

    private Predicate resolveLocalDate(final DateExpression<LocalDate> dateExpr, final String operator,
                                       final String rawValue) {
        final LocalDate val = parseLocalDate(rawValue);
        return switch (operator) {
            case "=" -> dateExpr.eq(val);
            case "!=" -> dateExpr.ne(val);
            case ">" -> dateExpr.after(val);
            case ">=" -> dateExpr.goe(val);
            case "<" -> dateExpr.before(val);
            case "<=" -> dateExpr.loe(val);
            default -> throw new RepresentationException(
                    "Operador '" + operator + "' nao suportado para tipo LocalDate");
        };
    }

    private Predicate resolveLocalDateTime(final DateTimeExpression<LocalDateTime> dtExpr, final String operator,
                                           final String rawValue) {
        final LocalDateTime val = parseLocalDateTime(rawValue);
        return switch (operator) {
            case "=" -> dtExpr.eq(val);
            case "!=" -> dtExpr.ne(val);
            case ">" -> dtExpr.after(val);
            case ">=" -> dtExpr.goe(val);
            case "<" -> dtExpr.before(val);
            case "<=" -> dtExpr.loe(val);
            default -> throw new RepresentationException(
                    "Operador '" + operator + "' nao suportado para tipo LocalDateTime");
        };
    }

    @SuppressWarnings("unchecked")
    private Predicate resolveBoolean(final SimpleExpression<?> expression, final String operator,
                                     final String rawValue) {
        final Boolean val = parseBoolean(rawValue);
        return switch (operator) {
            case "=" -> ((SimpleExpression<Boolean>) expression).eq(val);
            case "!=" -> ((SimpleExpression<Boolean>) expression).ne(val);
            default -> throw new RepresentationException(
                    "Operador '" + operator + "' nao suportado para tipo Boolean");
        };
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private Predicate resolveEnum(final SimpleExpression<?> expression, final String operator, final String rawValue,
                                  final Class<?> type) {
        final Enum<?> val = parseEnum(type, rawValue);
        return switch (operator) {
            case "=" -> ((SimpleExpression) expression).eq(val);
            case "!=" -> ((SimpleExpression) expression).ne(val);
            default -> throw new RepresentationException(
                    "Operador '" + operator + "' nao suportado para tipo Enum");
        };
    }

    public Object parseValue(final Class<?> type, final String rawValue) {
        if (String.class.isAssignableFrom(type)) {
            return rawValue;
        }
        if (Number.class.isAssignableFrom(type)
                || long.class.isAssignableFrom(type)
                || int.class.isAssignableFrom(type)
                || double.class.isAssignableFrom(type)
                || float.class.isAssignableFrom(type)) {
            return parseNumber(type, rawValue);
        }
        if (LocalDate.class.isAssignableFrom(type)) {
            return parseLocalDate(rawValue);
        }
        if (LocalDateTime.class.isAssignableFrom(type)) {
            return parseLocalDateTime(rawValue);
        }
        if (OffsetDateTime.class.isAssignableFrom(type)) {
            return parseOffsetDateTime(rawValue);
        }
        if (Boolean.class.isAssignableFrom(type) || boolean.class.isAssignableFrom(type)) {
            return parseBoolean(rawValue);
        }
        if (type.isEnum()) {
            return parseEnum(type, rawValue);
        }
        throw new RepresentationException("Tipo nao suportado para filtro customizado: " + type.getName());
    }

    private Number parseNumber(final Class<?> type, final String rawValue) {
        try {
            if (Long.class.isAssignableFrom(type) || long.class.isAssignableFrom(type)) {
                return Long.parseLong(rawValue);
            }
            if (Integer.class.isAssignableFrom(type) || int.class.isAssignableFrom(type)) {
                return Integer.parseInt(rawValue);
            }
            if (Double.class.isAssignableFrom(type) || double.class.isAssignableFrom(type)) {
                return Double.parseDouble(rawValue);
            }
            if (Float.class.isAssignableFrom(type) || float.class.isAssignableFrom(type)) {
                return Float.parseFloat(rawValue);
            }
            return new BigDecimal(rawValue);
        } catch (final NumberFormatException e) {
            throw new RepresentationException(
                    "Valor '" + rawValue + "' nao e um numero valido para o tipo " + type.getSimpleName());
        }
    }

    private LocalDate parseLocalDate(final String rawValue) {
        try {
            return LocalDate.parse(rawValue);
        } catch (final DateTimeParseException e) {
            throw new RepresentationException(
                    "Valor '" + rawValue + "' nao e uma data valida — formato esperado: YYYY-MM-DD");
        }
    }

    private LocalDateTime parseLocalDateTime(final String rawValue) {
        try {
            return LocalDateTime.parse(rawValue);
        } catch (final DateTimeParseException e) {
            throw new RepresentationException(
                    "Valor '" + rawValue + "' nao e uma data/hora valida — formato esperado: YYYY-MM-DDTHH:MM:SS");
        }
    }

    private OffsetDateTime parseOffsetDateTime(final String rawValue) {
        try {
            return OffsetDateTime.parse(rawValue);
        } catch (final DateTimeParseException e) {
            throw new RepresentationException(
                    "Valor '" + rawValue + "' nao e um OffsetDateTime valido — formato esperado: YYYY-MM-DDTHH:MM:SS+HH:MM");
        }
    }

    private Boolean parseBoolean(final String rawValue) {
        if (!VALID_BOOLEAN_LITERALS.contains(rawValue.toLowerCase())) {
            throw new RepresentationException(
                    "Valor '" + rawValue + "' nao e um booleano valido — use 'true' ou 'false'");
        }
        return Boolean.parseBoolean(rawValue);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private Enum<?> parseEnum(final Class<?> type, final String rawValue) {
        try {
            return Enum.valueOf((Class<? extends Enum>) type, rawValue);
        } catch (final IllegalArgumentException e) {
            throw new RepresentationException(
                    "Valor '" + rawValue + "' nao e valido para o enum " + type.getSimpleName());
        }
    }
}
