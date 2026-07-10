package com.br.pokefichas.domain.core.pokemon.usecase;

import com.br.pokefichas.domain.core.pokemon.dto.CustomPokemonMoveResponse;
import com.br.pokefichas.domain.core.pokemon.dto.CustomPokemonResponse;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.text.Normalizer;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Service
public class SupabasePokemonCatalogUseCase {

    private static final TypeReference<List<Map<String, Object>>> ROW_LIST_TYPE = new TypeReference<>() {
    };
    private static final Duration CACHE_TTL = Duration.ofMinutes(5);

    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;
    private final String supabaseUrl;
    private final String anonKey;
    private final String pokemonTable;
    private final String moveTable;
    private final int limit;

    private Instant cachedAt;
    private List<CustomPokemonResponse> cache = List.of();

    public SupabasePokemonCatalogUseCase(final ObjectMapper objectMapper,
                                         @Value("${app.supabase.url:}") final String supabaseUrl,
                                         @Value("${app.supabase.anon-key:}") final String anonKey,
                                         @Value("${app.supabase.pokemon-table:pokemons}") final String pokemonTable,
                                         @Value("${app.supabase.move-table:moves}") final String moveTable,
                                         @Value("${app.supabase.limit:2000}") final int limit) {
        this.objectMapper = objectMapper;
        this.supabaseUrl = trimTrailingSlash(supabaseUrl);
        this.anonKey = anonKey;
        this.pokemonTable = pokemonTable;
        this.moveTable = moveTable;
        this.limit = limit;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(8))
                .build();
    }

    public List<CustomPokemonResponse> search(final String term) {
        final String normalizedTerm = normalize(term);
        return loadCatalog().stream()
                .filter(pokemon -> normalizedTerm.isBlank() || normalize(pokemon.name()).contains(normalizedTerm))
                .limit(50)
                .toList();
    }

    public Optional<CustomPokemonResponse> findByName(final String name) {
        final String normalizedName = normalize(name);
        if (normalizedName.isBlank()) {
            return Optional.empty();
        }

        return loadCatalog().stream()
                .filter(pokemon -> normalize(pokemon.name()).equals(normalizedName))
                .findFirst();
    }

    private List<CustomPokemonResponse> loadCatalog() {
        if (!isConfigured()) {
            return List.of();
        }
        if (cachedAt != null && cachedAt.plus(CACHE_TTL).isAfter(Instant.now())) {
            return cache;
        }

        try {
            final List<Map<String, Object>> pokemonRows = fetchRows(pokemonTable, limit);
            final Map<String, CustomPokemonMoveResponse> movesByName = fetchRows(moveTable, 5000).stream()
                    .filter(this::isApproved)
                    .map(this::moveFromMap)
                    .filter(move -> !move.name().isBlank())
                    .collect(LinkedHashMap::new, (map, move) -> map.put(normalize(move.name()), move), Map::putAll);

            cache = pokemonRows.stream()
                    .filter(this::isApproved)
                    .map(row -> toPokemon(row, movesByName))
                    .filter(pokemon -> !pokemon.name().isBlank())
                    .toList();
            cachedAt = Instant.now();
            return cache;
        } catch (final Exception ignored) {
            return cache;
        }
    }

    private List<Map<String, Object>> fetchRows(final String table, final int tableLimit) throws Exception {
        if (table == null || table.isBlank()) {
            return List.of();
        }

        final URI uri = UriComponentsBuilder
                .fromUriString(supabaseUrl)
                .pathSegment("rest", "v1", table)
                .queryParam("select", "*")
                .queryParam("limit", tableLimit)
                .build()
                .toUri();

        final HttpRequest request = HttpRequest.newBuilder(uri)
                .timeout(Duration.ofSeconds(12))
                .header("apikey", anonKey)
                .header("Authorization", "Bearer " + anonKey)
                .header("Accept", "application/json")
                .GET()
                .build();

        final HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            return List.of();
        }

        return objectMapper.readValue(response.body(), ROW_LIST_TYPE);
    }

    private boolean isConfigured() {
        return !supabaseUrl.isBlank() && !anonKey.isBlank() && !pokemonTable.isBlank();
    }

    private CustomPokemonResponse toPokemon(final Map<String, Object> row,
                                            final Map<String, CustomPokemonMoveResponse> movesByName) {
        final String name = firstString(row, "name", "nome", "slug", "species", "especie", "url_slug");
        final String sprite = firstString(row, "sprite", "sprite_url", "image", "image_url", "artwork", "icon", "imagem", "sprite_shiny");
        final List<String> types = mergeStrings(
                stringList(firstPresent(row, "types", "tipos", "type", "tipo")),
                stringList(firstPresent(row, "tipo1")),
                stringList(firstPresent(row, "tipo2"))
        );
        final List<String> abilities = mergeStrings(
                stringList(firstPresent(row, "abilities", "habilidades", "ability", "habilidade")),
                stringList(firstPresent(row, "habilidade1")),
                stringList(firstPresent(row, "habilidade2")),
                stringList(firstPresent(row, "habilidade_oculta")),
                stringList(firstPresent(row, "habilidade_mega"))
        );
        final List<CustomPokemonMoveResponse> moves = mergeMoves(movesByName,
                moveList(firstPresent(row, "moves", "movimentos", "move_list", "movelist")),
                moveList(firstPresent(row, "moves_level")),
                moveList(firstPresent(row, "moves_tm")),
                moveList(firstPresent(row, "moves_egg")),
                moveList(firstPresent(row, "moves_tutor"))
        );
        final Map<String, Integer> stats = stats(row);

        return new CustomPokemonResponse(name, sprite, types, abilities, moves, stats);
    }

    @SafeVarargs
    private final List<String> mergeStrings(final List<String>... values) {
        final Map<String, String> byKey = new LinkedHashMap<>();
        for (final List<String> list : values) {
            list.stream()
                    .map(String::trim)
                    .filter(item -> !item.isBlank())
                    .forEach(item -> byKey.putIfAbsent(normalize(item), item));
        }
        return new ArrayList<>(byKey.values());
    }

    @SafeVarargs
    private final List<CustomPokemonMoveResponse> mergeMoves(final Map<String, CustomPokemonMoveResponse> movesByName,
                                                             final List<CustomPokemonMoveResponse>... values) {
        final Map<String, CustomPokemonMoveResponse> byKey = new LinkedHashMap<>();
        for (final List<CustomPokemonMoveResponse> list : values) {
            list.stream()
                    .filter(move -> !move.name().isBlank())
                    .map(move -> enrichMove(move, movesByName))
                    .forEach(move -> byKey.putIfAbsent(normalize(move.name()), move));
        }
        return new ArrayList<>(byKey.values());
    }

    private CustomPokemonMoveResponse enrichMove(final CustomPokemonMoveResponse move,
                                                 final Map<String, CustomPokemonMoveResponse> movesByName) {
        final CustomPokemonMoveResponse catalogMove = movesByName.get(normalize(move.name()));
        if (catalogMove == null) {
            return move;
        }

        return new CustomPokemonMoveResponse(
                firstNonBlank(move.name(), catalogMove.name()),
                firstNonBlank(move.category(), catalogMove.category()),
                firstNonBlank(move.type(), catalogMove.type()),
                firstNonBlank(move.style(), catalogMove.style()),
                move.power() != null ? move.power() : catalogMove.power(),
                move.accuracy() != null ? move.accuracy() : catalogMove.accuracy()
        );
    }

    @SuppressWarnings("unchecked")
    private List<CustomPokemonMoveResponse> moveList(final Object value) {
        if (value == null) {
            return List.of();
        }
        if (value instanceof Map<?, ?> map) {
            final Map<String, Object> typedMap = (Map<String, Object>) map;
            if (typedMap.containsKey("name") || typedMap.containsKey("nome") || typedMap.containsKey("move")) {
                return List.of(moveFromMap(typedMap));
            }
            return typedMap.values().stream()
                    .map(this::moveList)
                    .flatMap(Collection::stream)
                    .toList();
        }
        if (value instanceof Collection<?> collection) {
            return collection.stream()
                    .map(item -> {
                        if (item instanceof Map<?, ?> map) {
                            return moveFromMap((Map<String, Object>) map);
                        }
                        final String name = Objects.toString(item, "").trim();
                        return name.isBlank() ? null : new CustomPokemonMoveResponse(name, "", "", "", null, null);
                    })
                    .filter(Objects::nonNull)
                    .toList();
        }
        if (value instanceof String text) {
            return List.of(text.split("[,;\\n]")).stream()
                    .map(String::trim)
                    .filter(move -> !move.isBlank())
                    .map(move -> new CustomPokemonMoveResponse(move, "", "", "", null, null))
                    .toList();
        }

        return List.of();
    }

    private CustomPokemonMoveResponse moveFromMap(final Map<String, Object> move) {
        return new CustomPokemonMoveResponse(
                firstString(move, "name", "nome", "move"),
                firstString(move, "category", "categoria", "damage_class"),
                firstString(move, "type", "tipo", "tipo1"),
                firstString(move, "style", "contest", "contest_type", "contest_style", "estilo"),
                integer(firstPresent(move, "power", "poder", "base_power")),
                integer(firstPresent(move, "accuracy", "precisao", "precisão"))
        );
    }

    @SuppressWarnings("unchecked")
    private Map<String, Integer> stats(final Map<String, Object> row) {
        final Map<String, Integer> stats = new LinkedHashMap<>();
        final Object nestedStats = firstPresent(row, "stats", "status");
        if (nestedStats instanceof Map<?, ?> map) {
            ((Map<String, Object>) map).forEach((key, value) -> putStat(stats, key, value));
        }

        List.of("hp", "atk", "def", "satk", "sdef", "speed").forEach(key -> putStat(stats, key, firstPresent(row, key)));
        putStat(stats, "satk", firstPresent(row, "spa", "sp_atk", "special_attack", "special-attack"));
        putStat(stats, "sdef", firstPresent(row, "spd", "sp_def", "special_defense", "special-defense"));
        putStat(stats, "speed", firstPresent(row, "spe"));
        return stats;
    }

    private void putStat(final Map<String, Integer> stats, final String rawKey, final Object value) {
        final Integer number = integer(value);
        if (number == null) {
            return;
        }

        final String key = switch (normalize(rawKey)) {
            case "attack" -> "atk";
            case "defense" -> "def";
            case "specialattack", "special-attack", "spatk", "sp-atk", "spa" -> "satk";
            case "specialdefense", "special-defense", "spdef", "sp-def", "spd" -> "sdef";
            case "spe" -> "speed";
            default -> normalize(rawKey);
        };

        if (List.of("hp", "atk", "def", "satk", "sdef", "speed").contains(key)) {
            stats.put(key, number);
        }
    }

    private List<String> stringList(final Object value) {
        if (value == null) {
            return List.of();
        }
        if (value instanceof Collection<?> collection) {
            return collection.stream()
                    .map(item -> item instanceof Map<?, ?> map ? firstString(cast(map), "name", "nome", "label") : Objects.toString(item, ""))
                    .map(String::trim)
                    .filter(item -> !item.isBlank())
                    .toList();
        }
        if (value instanceof String text) {
            final String separator = text.contains(",") || text.contains(";") || text.contains("\n") ? "[,;\\n]" : "\\|";
            return List.of(text.split(separator)).stream()
                    .map(String::trim)
                    .filter(item -> !item.isBlank())
                    .toList();
        }

        return List.of();
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> cast(final Map<?, ?> map) {
        return (Map<String, Object>) map;
    }

    private String firstString(final Map<String, Object> row, final String... keys) {
        final Object value = firstPresent(row, keys);
        return value == null ? "" : Objects.toString(value, "").trim();
    }

    private String firstNonBlank(final String first, final String second) {
        return first != null && !first.isBlank() ? first : Optional.ofNullable(second).orElse("");
    }

    private boolean isApproved(final Map<String, Object> row) {
        final Object approved = row.get("approved");
        return !(approved instanceof Boolean value) || value;
    }

    private Object firstPresent(final Map<String, Object> row, final String... keys) {
        for (final String key : keys) {
            if (row.containsKey(key) && row.get(key) != null) {
                return row.get(key);
            }
        }
        return null;
    }

    private Integer integer(final Object value) {
        if (value instanceof Number number) {
            return number.intValue();
        }
        if (value instanceof String text && !text.isBlank()) {
            try {
                return Integer.parseInt(text.trim());
            } catch (final NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }

    private String normalize(final String value) {
        return Normalizer.normalize(Optional.ofNullable(value).orElse(""), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .trim()
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-+|-+$", "");
    }

    private String trimTrailingSlash(final String value) {
        return Optional.ofNullable(value).orElse("").replaceAll("/+$", "");
    }
}
