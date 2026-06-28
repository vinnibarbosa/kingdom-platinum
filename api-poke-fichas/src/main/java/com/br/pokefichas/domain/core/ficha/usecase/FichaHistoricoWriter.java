package com.br.pokefichas.domain.core.ficha.usecase;

import com.br.pokefichas.domain.core.ficha.dto.FichaResponse;
import com.br.pokefichas.domain.core.ficha.model.FichaHistorico;
import com.br.pokefichas.domain.core.ficha.repository.FichaCommand;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Component
public class FichaHistoricoWriter {

    private static final Set<String> IGNORED_FIELDS = Set.of(
            "id", "idOrganizacao", "createdAt", "createdBy", "updatedAt", "updatedBy"
    );
    private static final int MAX_VALUE_LENGTH = 500;

    private final FichaCommand command;
    private final ObjectMapper objectMapper;

    public FichaHistoricoWriter(final FichaCommand command, final ObjectMapper objectMapper) {
        this.command = command;
        this.objectMapper = objectMapper;
    }

    public void recordCreation(final FichaResponse ficha) {
        final String lote = UUID.randomUUID().toString();
        command.saveHistoricos(List.of(build(
                ficha.id(),
                ficha.idOrganizacao(),
                lote,
                "ADICIONADO",
                "ficha",
                null,
                ficha.nome()
        )));
    }

    public void recordUpdate(final FichaResponse before, final FichaResponse after) {
        recordUpdate(before, after, false);
    }

    public void recordUpdateWithoutContext(final FichaResponse before, final FichaResponse after) {
        recordUpdate(before, after, true);
    }

    private void recordUpdate(final FichaResponse before,
                              final FichaResponse after,
                              final boolean withoutContext) {
        final List<Change> changes = new ArrayList<>();
        compare("", objectMapper.valueToTree(before), objectMapper.valueToTree(after), changes);
        if (changes.isEmpty()) {
            return;
        }

        final String lote = UUID.randomUUID().toString();
        final List<FichaHistorico> historicos = changes.stream()
                .map(change -> build(
                        after.id(),
                        after.idOrganizacao(),
                        lote,
                        change.action(),
                        change.field(),
                        change.before(),
                        change.after()
                ))
                .toList();
        if (withoutContext) {
            command.saveHistoricosWithoutContext(historicos);
            return;
        }
        command.saveHistoricos(historicos);
    }

    private void compare(final String path,
                         final JsonNode before,
                         final JsonNode after,
                         final List<Change> changes) {
        if (same(before, after)) {
            return;
        }
        if (empty(before)) {
            changes.add(new Change("ADICIONADO", printablePath(path), null, format(after)));
            return;
        }
        if (empty(after)) {
            changes.add(new Change("REMOVIDO", printablePath(path), format(before), null));
            return;
        }
        if (before.isObject() && after.isObject()) {
            final Set<String> fields = new HashSet<>();
            fields.addAll(before.propertyNames());
            fields.addAll(after.propertyNames());
            fields.stream()
                    .filter(field -> !IGNORED_FIELDS.contains(field))
                    .sorted()
                    .forEach(field -> compare(child(path, field), before.get(field), after.get(field), changes));
            return;
        }
        if (before.isArray() && after.isArray()) {
            final int size = Math.max(before.size(), after.size());
            for (int index = 0; index < size; index++) {
                compare(path + "[" + index + "]", before.get(index), after.get(index), changes);
            }
            return;
        }
        changes.add(new Change("ALTERADO", printablePath(path), format(before), format(after)));
    }

    private boolean same(final JsonNode before, final JsonNode after) {
        if (empty(before) && empty(after)) {
            return true;
        }
        if (before == null || after == null) {
            return false;
        }
        if (before.isNumber() && after.isNumber()) {
            return numericValue(before).compareTo(numericValue(after)) == 0;
        }
        return before.equals(after);
    }

    private BigDecimal numericValue(final JsonNode node) {
        return new BigDecimal(node.asText()).stripTrailingZeros();
    }

    private boolean empty(final JsonNode node) {
        return node == null
                || node.isMissingNode()
                || node.isNull()
                || node.isTextual() && node.asText().isBlank();
    }

    private String child(final String path, final String field) {
        return path.isBlank() ? field : path + "." + field;
    }

    private String printablePath(final String path) {
        return path == null || path.isBlank() ? "ficha" : path;
    }

    private String format(final JsonNode node) {
        if (empty(node)) {
            return null;
        }
        if (node.isTextual()) {
            final String value = node.asText();
            return value.startsWith("data:image/") ? "[imagem]" : truncate(value);
        }
        if (node.isObject()) {
            for (String identity : List.of("nome", "apelido", "especie", "descricao")) {
                final JsonNode value = node.get(identity);
                if (!empty(value) && value.isValueNode()) {
                    return truncate(value.asText());
                }
            }
        }
        if (node.isArray()) {
            return node.size() + (node.size() == 1 ? " item" : " itens");
        }
        return truncate(node.isValueNode() ? node.asText() : node.toString());
    }

    private String truncate(final String value) {
        if (value == null || value.length() <= MAX_VALUE_LENGTH) {
            return value;
        }
        return value.substring(0, MAX_VALUE_LENGTH - 1) + "…";
    }

    private FichaHistorico build(final Long idFicha,
                                  final Long idOrganizacao,
                                  final String lote,
                                  final String action,
                                  final String field,
                                  final String before,
                                  final String after) {
        return FichaHistorico.Builder.create()
                .idOrganizacao(idOrganizacao)
                .idFicha(idFicha)
                .lote(lote)
                .acao(action)
                .campo(field)
                .valorAnterior(before)
                .valorNovo(after)
                .build();
    }

    private record Change(String action, String field, String before, String after) {
    }
}
