package com.br.pokefichas.commons.dto;

import com.br.pokefichas.commons.page.Pageable;
import com.br.pokefichas.commons.page.Sort;
import com.querydsl.core.types.Predicate;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

@Schema(description = "Parâmetros de paginação e ordenação")
public class PageRequest {

    @Schema(description = "Deslocamento inicial", example = "0", defaultValue = "0")
    @Min(0)
    private int offset = 0;

    @Schema(description = "Quantidade de registros por página", example = "20", defaultValue = "20")
    @Min(1)
    @Max(1000)
    private int limit = 20;

    @Schema(description = "Campo para ordenação (ex: nome:asc)", example = "nome:asc")
    private String sort;

    @Schema(description = "Pular contagem total para melhor performance", defaultValue = "false")
    private boolean skipCount = false;

    @Schema(hidden = true)
    private transient Predicate filterPredicate;

    public int getOffset() { return offset; }
    public void setOffset(int offset) { this.offset = offset; }
    public int getLimit() { return limit; }
    public void setLimit(int limit) { this.limit = limit; }
    public String getSort() { return sort; }
    public void setSort(String sort) { this.sort = sort; }
    public boolean isSkipCount() { return skipCount; }
    public void setSkipCount(boolean skipCount) { this.skipCount = skipCount; }
    public Predicate getFilterPredicate() { return filterPredicate; }
    public void setFilterPredicate(Predicate filterPredicate) { this.filterPredicate = filterPredicate; }
    public boolean hasFilters() { return filterPredicate != null; }

    public Pageable toPageable(Sort defaultSort) {
        return Pageable.of(offset, limit, defaultSort, skipCount);
    }

    public Pageable toPageable() {
        return Pageable.of(offset, limit, null, skipCount);
    }
}
