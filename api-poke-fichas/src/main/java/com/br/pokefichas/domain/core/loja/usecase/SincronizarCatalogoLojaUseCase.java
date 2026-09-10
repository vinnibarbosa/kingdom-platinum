package com.br.pokefichas.domain.core.loja.usecase;

import com.br.pokefichas.commons.exception.BusinessException;
import com.br.pokefichas.commons.organizacao.OrganizacaoContext;
import com.br.pokefichas.domain.core.loja.dto.ImportarCatalogoLojaRequest;
import com.br.pokefichas.domain.core.loja.dto.ImportarCatalogoLojaResponse;
import com.br.pokefichas.domain.core.loja.dto.LojaItemRequest;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.text.Normalizer;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SincronizarCatalogoLojaUseCase {

    private static final String CATALOG_URL =
            "https://raw.githubusercontent.com/alphx-r/kingdomplatinum/main/items.json";
    private static final Duration SYNC_INTERVAL = Duration.ofMinutes(2);

    private final RestClient restClient;
    private final GerenciarLojaItemUseCase gerenciarLojaItem;
    private final OrganizacaoContext organizacaoContext;
    private final Map<Long, Instant> ultimaSincronizacao = new ConcurrentHashMap<>();

    public SincronizarCatalogoLojaUseCase(final RestClient.Builder restClientBuilder,
                                          final GerenciarLojaItemUseCase gerenciarLojaItem,
                                          final OrganizacaoContext organizacaoContext) {
        this.restClient = restClientBuilder.build();
        this.gerenciarLojaItem = gerenciarLojaItem;
        this.organizacaoContext = organizacaoContext;
    }

    public synchronized ImportarCatalogoLojaResponse sincronizar() {
        final Long organizacaoId = organizacaoContext.getRequiredOrganizacaoId();
        final Instant ultima = ultimaSincronizacao.get(organizacaoId);
        if (ultima != null && ultima.plus(SYNC_INTERVAL).isAfter(Instant.now())) {
            return new ImportarCatalogoLojaResponse(0, 0, 0, 0);
        }

        final JsonNode payload;
        try {
            payload = restClient.get()
                    .uri(CATALOG_URL + "?v=" + System.currentTimeMillis())
                    .retrieve()
                    .body(JsonNode.class);
        } catch (final RuntimeException error) {
            throw new BusinessException("Nao foi possivel consultar o catalogo oficial da loja.", "STORE_CATALOG_UNAVAILABLE");
        }

        final List<LojaItemRequest> itens = converter(payload);
        if (itens.isEmpty()) {
            throw new BusinessException("O catalogo oficial retornou vazio.", "STORE_CATALOG_EMPTY");
        }

        final ImportarCatalogoLojaResponse response = gerenciarLojaItem.importarCatalogo(
                new ImportarCatalogoLojaRequest(itens)
        );
        ultimaSincronizacao.put(organizacaoId, Instant.now());
        return response;
    }

    private List<LojaItemRequest> converter(final JsonNode payload) {
        if (payload == null || !payload.isArray()) {
            return List.of();
        }
        final List<LojaItemRequest> itens = new ArrayList<>();
        int ordem = 0;
        for (final JsonNode row : payload) {
            final JsonNode enabled = row.path("flags").path("pokemart").get("enabled");
            final String nome = texto(row, "name", "nome");
            if (nome == null || enabled == null || !enabled.isBoolean() || !enabled.asBoolean()) {
                continue;
            }
            itens.add(new LojaItemRequest(
                    categoria(texto(row, "category", "categoria")),
                    slug(nome),
                    texto(row, "sprite", "icon", "icone", "image", "imagem"),
                    nome,
                    texto(row, "item_desc", "description", "descricao", "desc"),
                    numero(row, "price", "preco", "valor"),
                    true,
                    ordem++
            ));
        }
        return itens;
    }

    private String categoria(final String value) {
        final String normalized = normalize(value);
        return switch (normalized) {
            case "medicine" -> "Restauração HP / PP";
            case "status" -> "Restaurar status";
            case "standardballs", "specialballs", "pokeball" -> "Pokébolas";
            case "battleitems", "helditems", "holditems", "typeenhancement" -> "Itens de batalha";
            case "evolutionitems", "evolutionaryitems", "evolutionary" -> "Evolutionary";
            case "berries" -> "Berries";
            case "valuableitems", "treasure" -> "Treasure";
            case "keyitems", "traineritems" -> "Trainer itens (Keys)";
            case "allmachines" -> "TM / Pill case";
            default -> value == null || value.isBlank() ? "Itens de batalha" : value.trim();
        };
    }

    private BigDecimal numero(final JsonNode row, final String... fields) {
        for (final String field : fields) {
            final JsonNode value = row.get(field);
            if (value == null || value.isNull()) continue;
            try {
                final BigDecimal parsed = value.isNumber() ? value.decimalValue() : new BigDecimal(value.asText());
                return parsed.max(BigDecimal.ZERO);
            } catch (final NumberFormatException ignored) {
                // Tenta o proximo campo conhecido.
            }
        }
        return BigDecimal.ZERO;
    }

    private String texto(final JsonNode row, final String... fields) {
        for (final String field : fields) {
            final JsonNode value = row.get(field);
            if (value != null && value.isTextual() && !value.asText().isBlank()) {
                return value.asText().trim();
            }
        }
        return null;
    }

    private String slug(final String value) {
        return Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-+|-+$", "");
    }

    private String normalize(final String value) {
        return value == null ? "" : slug(value).replace("-", "");
    }
}
