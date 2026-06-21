package com.br.pokefichas.commons.persistence;

import com.br.pokefichas.commons.entity.IEntity;
import com.br.pokefichas.commons.entity.OrgBaseEntity;
import com.br.pokefichas.commons.entity.TenantBaseEntity;
import com.br.pokefichas.commons.exception.UnauthorizedException;
import com.br.pokefichas.commons.organizacao.OrgScoped;
import com.br.pokefichas.commons.organizacao.OrganizacaoContext;
import com.br.pokefichas.commons.page.Page;
import com.br.pokefichas.commons.page.Pageable;
import com.br.pokefichas.commons.page.Sort;
import com.br.pokefichas.commons.tenant.TenantContext;
import com.br.pokefichas.commons.tenant.TenantScoped;
import com.br.pokefichas.commons.useraccess.UserAccess;
import com.mysema.commons.lang.CloseableIterator;
import com.querydsl.core.types.EntityPath;
import com.querydsl.core.types.FactoryExpression;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.core.types.dsl.PathBuilderFactory;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.JPQLTemplates;
import com.querydsl.jpa.hibernate.HibernateQuery;
import com.querydsl.jpa.impl.JPADeleteClause;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.querydsl.jpa.impl.JPAUpdateClause;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.LockModeType;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.PersistenceException;
import jakarta.validation.ValidationException;
import org.hibernate.Session;
import org.hibernate.exception.ConstraintViolationException;

import java.io.Serializable;
import java.sql.SQLException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class JpaRepository {

    private static final String FK_MESSAGE_DEFAULT =
            "Nao e possivel excluir este item, o mesmo esta em uso em outro registro";

    private static final String NOT_FOUND_MESSAGE =
            "Nao foi encontrado nenhum registro do tipo %s com o id %s";

    @PersistenceContext
    private final EntityManager em;
    private final PathBuilderFactory pathBuilderFactory;
    private final JPQLTemplates jpqlTemplate;
    private final JPAQueryFactory jpaQueryFactory;
    private final UserAccess userAccess;
    private final TenantContext tenantContext;
    private final OrganizacaoContext organizacaoContext;
    private final Map<Class<?>, AuditFieldMetadata> auditFieldCache = new java.util.concurrent.ConcurrentHashMap<>();

    public JpaRepository(final EntityManager em,
                         final PathBuilderFactory pathBuilderFactory,
                         final JPQLTemplates jpqlTemplate,
                         final UserAccess userAccess,
                         final TenantContext tenantContext,
                         final OrganizacaoContext organizacaoContext) {
        this.em = Objects.requireNonNull(em);
        this.pathBuilderFactory = Objects.requireNonNull(pathBuilderFactory);
        this.jpqlTemplate = Objects.requireNonNull(jpqlTemplate);
        this.userAccess = Objects.requireNonNull(userAccess);
        this.tenantContext = Objects.requireNonNull(tenantContext);
        this.organizacaoContext = Objects.requireNonNull(organizacaoContext);
        this.jpaQueryFactory = new JPAQueryFactory(jpqlTemplate, em);
    }

    public <T> Optional<T> findOptional(final Class<T> entityClass, final Serializable id) {
        if (id == null) {
            return Optional.empty();
        }
        if (isContextScopedEntity(entityClass)) {
            return findContextScopedOptional(entityClass, id);
        }
        return Optional.ofNullable(em.find(entityClass, id));
    }

    public <T> T findOrNull(final Class<T> entityClass, final Serializable id) {
        return findOptional(entityClass, id).orElse(null);
    }

    public <T> T getReference(final Class<T> entityClass, final Serializable id) {
        return find(entityClass, id);
    }

    public <T> T find(final Class<T> entityClass, final Serializable id) {
        if (isContextScopedEntity(entityClass)) {
            return checkIfFound(findContextScopedOptional(entityClass, id).orElse(null), entityClass.getSimpleName(), id);
        }
        return checkIfFound(enforceContextAccess(em.find(entityClass, id), true), entityClass.getSimpleName(), id);
    }

    public <T> T find(final Class<T> entityClass, final Serializable id, final LockModeType lock) {
        if (isContextScopedEntity(entityClass)) {
            final JPAQuery<T> scopedQuery = query(entityClass)
                    .where(buildIdPredicate(entityClass, id));
            if (lock != null) {
                scopedQuery.setLockMode(lock);
            }
            return checkIfFound(scopedQuery.fetchOne(), entityClass.getSimpleName(), id);
        }
        return checkIfFound(enforceContextAccess(em.find(entityClass, id, lock), true), entityClass.getSimpleName(), id);
    }

    private <T> T checkIfFound(final T entity, final String simpleName, final Serializable id) {
        return Optional.ofNullable(entity)
                .orElseThrow(() -> new com.br.pokefichas.commons.exception.EntityNotFoundException(
                        NOT_FOUND_MESSAGE.formatted(simpleName, id)
                ));
    }

    public void lock(final Object entity, final LockModeType type) {
        em.lock(entity, type);
    }

    public <T> Optional<T> findUniqueOptional(final Class<T> entityClass, final Predicate... where) {
        return Optional.ofNullable(query(entityClass).where(where).fetchOne());
    }

    public <T> Optional<T> findUniqueOptionalWithoutTenantFilter(final Class<T> entityClass, final Predicate... where) {
        return Optional.ofNullable(query(entityClass, true).where(where).fetchOne());
    }

    public <T> T findUnique(final Class<T> entityClass, final Predicate... where) {
        return findUniqueOptional(entityClass, where).orElse(null);
    }

    public <T> T findOne(final Class<T> entityClass, final Predicate... where) {
        return findOne(entityClass, null, where);
    }

    public <T> T findOne(final Class<T> entityClass, final Sort sort) {
        return findOne(entityClass, sort, (Predicate) null);
    }

    public <T> T findOne(final Class<T> entityClass, final Sort sort, final Predicate... where) {
        return applySorting(query(entityClass).where(where), sort).limit(1).fetchOne();
    }

    public <T extends IEntity<?>> T save(final T entity) {
        return save(entity, SaveOptions.defaults());
    }

    public record SaveOptions(boolean fireEvents, boolean detach) {
        public static SaveOptions defaults() { return new SaveOptions(true, false); }
        public static SaveOptions withDetach() { return new SaveOptions(true, true); }
        public static SaveOptions withoutEvents() { return new SaveOptions(false, false); }
    }

    public <T extends IEntity<?>> T save(final T entity, final SaveOptions options) {
        Objects.requireNonNull(entity, "Entity cannot be null");
        applyContextScope(entity);
        setAuditInfo(entity);
        final T savedEntity = entity.isNew() ? persistAndFlush(entity) : mergeAndFlush(entity);
        if (options.detach()) {
            em.detach(savedEntity);
        }
        return savedEntity;
    }

    private <T extends IEntity<?>> T persistAndFlush(final T entity) {
        em.persist(entity);
        em.flush();
        return entity;
    }

    private <T extends IEntity<?>> T mergeAndFlush(final T entity) {
        final T merged = em.merge(entity);
        em.flush();
        return merged;
    }

    public <T extends IEntity<?>> List<T> saveAll(final Collection<T> entities) {
        return Optional.ofNullable(entities)
                .map(Collection::stream)
                .orElse(java.util.stream.Stream.empty())
                .map(this::save)
                .toList();
    }

    public <T extends IEntity<?>> void remove(final Collection<T> entities) {
        Optional.ofNullable(entities).ifPresent(list -> list.forEach(this::remove));
    }

    public <T extends IEntity<?>> void remove(final T entity) {
        remove(entity, true);
    }

    public <T extends IEntity<?>> void remove(final T entity, final boolean fireEvents) {
        try {
            em.remove(entity);
            em.flush();
        } catch (final PersistenceException e) {
            handleRemoveException(e);
        }
    }

    public void clear() {
        em.clear();
    }

    public <T> List<T> findAll(final Class<T> entityClass, final Predicate... where) {
        return findAll(entityClass, (Sort) null, where);
    }

    public <T> List<T> findAllWithoutTenantFilter(final Class<T> entityClass, final Predicate... where) {
        return query(entityClass, true).where(where).fetch();
    }

    public <T> List<T> findAll(final Class<T> entityClass, final Sort sort, final Predicate... where) {
        return applySorting(query(entityClass).where(where), sort).fetch();
    }

    public <T> Page<T> findAll(final Class<T> entityClass, final Pageable pageable, final Predicate... where) {
        return findAll(entityClass, pageable, false, where);
    }

    public <T, R> Page<R> findAllProjection(final EntityPath<T> entityPath,
                                            final Class<T> entityClass,
                                            final FactoryExpression<R> projection,
                                            final Pageable pageable,
                                            final Predicate... where) {
        final JPAQuery<R> jpaQuery = new JPAQuery<>(em, jpqlTemplate)
                .select(projection)
                .from(entityPath)
                .where(where);
        final PathBuilder<T> pathBuilder = new PathBuilder<>(entityClass, entityPath.getMetadata());
        applyTenantPredicate(jpaQuery, entityClass, pathBuilder, false);
        applySorting(jpaQuery, pageable.getSort());

        final long limit = pageable.getLimit();
        final long offset = pageable.getOffset();

        if (pageable.isSkipCount()) {
            return handleSkipCountPagination(jpaQuery, pageable, limit, offset);
        }
        return handleRegularPagination(jpaQuery, pageable, limit, offset);
    }

    public <T> Page<T> findAll(final Class<T> entityClass, final Pageable pageable,
                               final Consumer<JPAQuery<T>> queryCustomizer, final Predicate... where) {
        return findAll(entityClass, pageable, false, queryCustomizer, where);
    }

    public <T> CloseableIterator<T> findAllIt(final Class<T> entityClass, final Sort sort, final Predicate... where) {
        return findAllIt(entityClass, sort, false, where);
    }

    public <T> CloseableIterator<T> findAllIt(final Class<T> entityClass, final Sort sort,
                                              final boolean skipContextFilter, final Predicate... where) {
        final HibernateQuery<T> hibernateQuery = hquery(entityClass, skipContextFilter).where(where);
        applySorting(hibernateQuery, sort);
        return hibernateQuery.iterate();
    }

    public <T> Page<T> findAll(final Class<T> entityClass, final Pageable pageable, final boolean skipContextFilter,
                               final Predicate... where) {
        return findAll(entityClass, pageable, skipContextFilter, null, where);
    }

    public <T> Page<T> findAll(final Class<T> entityClass, final Pageable pageable, final boolean skipContextFilter,
                               final Consumer<JPAQuery<T>> queryCustomizer, final Predicate... where) {
        final JPAQuery<T> jpaQuery = query(entityClass, skipContextFilter).where(where);
        Optional.ofNullable(queryCustomizer).ifPresent(customizer -> customizer.accept(jpaQuery));
        applySorting(jpaQuery, pageable.getSort());

        final long limit = pageable.getLimit();
        final long offset = pageable.getOffset();

        if (pageable.isSkipCount()) {
            return handleSkipCountPagination(jpaQuery, pageable, limit, offset);
        }
        return handleRegularPagination(jpaQuery, pageable, limit, offset);
    }

    private <T> Page<T> handleSkipCountPagination(final JPAQuery<T> query, final Pageable pageable,
                                                  final long limit, final long offset) {
        query.offset(offset).limit(limit + 1);
        final List<T> list = query.fetch();
        final int size = list.size();
        if (size > limit) {
            list.removeLast();
        }
        return new Page<>(list, pageable, offset + size);
    }

    private <T> Page<T> handleRegularPagination(final JPAQuery<T> query, final Pageable pageable,
                                                final long limit, final long offset) {
        final long total = query.fetchCount();
        final long finalOffset = switch ((int) Long.compare(offset, total)) {
            case 1 -> ((int) (total / limit)) * limit;
            case 0 -> Math.max(offset - limit, 0);
            default -> offset;
        };
        query.offset(finalOffset).limit(limit);
        return new Page<>(query.fetch(), pageable, total);
    }

    public <T> boolean exists(final Class<T> entityClass, final Predicate... where) {
        return query(entityClass).where(where).fetchCount() > 0;
    }

    public <T> boolean existsWithoutTenant(final Class<T> entityClass, final Predicate... where) {
        return query(entityClass, true).where(where).fetchCount() > 0;
    }

    public <T> boolean existsOptimized(final Class<T> entityClass, final Predicate... where) {
        return query(entityClass)
                .select(Expressions.ONE)
                .where(where)
                .limit(1)
                .fetchFirst() != null;
    }

    public <T> boolean notExists(final Class<T> entityClass, final Predicate... where) {
        return !exists(entityClass, where);
    }

    public <T> HibernateQuery<T> hquery(final Class<T> entityClass) {
        return hquery(entityClass, false);
    }

    public <T> HibernateQuery<T> hquery(final Class<T> entityClass, final boolean skipContextFilter) {
        final HibernateQuery<T> hibernateQuery = new HibernateQuery<>(em.unwrap(Session.class));
        final PathBuilder<T> entityPath = pathBuilderFactory.create(entityClass);
        hibernateQuery.from(entityPath);
        applyTenantPredicate(hibernateQuery, entityClass, entityPath, skipContextFilter);
        return hibernateQuery;
    }

    public <T> JPAQuery<T> query(final Class<T> entityClass) {
        return query(entityClass, false);
    }

    public <T> JPAQuery<T> query(final Class<T> entityClass, final boolean skipContextFilter) {
        final JPAQuery<T> jpaQuery = new JPAQuery<>(em, jpqlTemplate);
        final PathBuilder<T> entityPath = pathBuilderFactory.create(entityClass);
        jpaQuery.from(entityPath);
        applyTenantPredicate(jpaQuery, entityClass, entityPath, skipContextFilter);
        return jpaQuery;
    }

    public <T> JPAQuery<T> subQuery(final EntityPath<T> entity) {
        return jpaQueryFactory.selectFrom(entity);
    }

    public JPAQuery<?> subQuery(final EntityPath<?>... paths) {
        return jpaQueryFactory.from(paths);
    }

    public <T> long count(final Class<T> entityClass, final Predicate... where) {
        return query(entityClass).where(where).fetchCount();
    }

    public <T> JPQLQuery<T> applySorting(final JPQLQuery<T> query, final Sort sort) {
        Optional.ofNullable(sort).ifPresent(s -> s.forEach(query::orderBy));
        return query;
    }

    public <T> long update(final Class<T> entityClass, final Consumer<JPAUpdateClause> consumer) {
        final PathBuilder<T> entityPath = pathBuilderFactory.create(entityClass);
        final JPAUpdateClause clause = new JPAUpdateClause(em, entityPath, jpqlTemplate);
        consumer.accept(clause);
        applyTenantPredicate(clause, entityClass, entityPath);
        return clause.execute();
    }

    public <T> long update(final Class<T> entityClass, final BiConsumer<JPAUpdateClause, PathBuilder<T>> consumer) {
        final PathBuilder<T> entityPath = pathBuilderFactory.create(entityClass);
        final JPAUpdateClause clause = new JPAUpdateClause(em, entityPath, jpqlTemplate);
        consumer.accept(clause, entityPath);
        applyTenantPredicate(clause, entityClass, entityPath);
        return clause.execute();
    }

    public <T> long updateWithAudit(final Class<T> entityClass,
                                    final Consumer<JPAUpdateClause> consumer,
                                    final Predicate... predicates) {
        final PathBuilder<T> entityPath = pathBuilderFactory.create(entityClass);
        final JPAUpdateClause clause = new JPAUpdateClause(em, entityPath, jpqlTemplate);

        consumer.accept(clause);
        applyAuditUpdate(clause, entityClass, entityPath);
        applyTenantPredicate(clause, entityClass, entityPath);
        if (predicates != null && predicates.length > 0) {
            clause.where(predicates);
        }

        return clause.execute();
    }

    public <T> long delete(final Class<T> entityClass, final Predicate... predicates) {
        return delete(entityClass, query -> query.where(predicates));
    }

    public <T> long delete(final Class<T> entityClass, final Consumer<JPADeleteClause> consumer) {
        final PathBuilder<T> entityPath = pathBuilderFactory.create(entityClass);
        final JPADeleteClause clause = new JPADeleteClause(em, entityPath, jpqlTemplate);
        consumer.accept(clause);
        applyTenantPredicate(clause, entityClass, entityPath);
        try {
            return clause.execute();
        } catch (final PersistenceException e) {
            handleRemoveException(e);
            return -1;
        }
    }

    protected void handleRemoveException(final PersistenceException e) {
        switch (e.getCause()) {
            case ConstraintViolationException cve -> {
                if (cve.getCause() instanceof SQLException sqlException) {
                    final String sqlState = sqlException.getSQLState();
                    if ("23503".equals(sqlState) || "23000".equals(sqlState)) {
                throw new jakarta.validation.ValidationException(FK_MESSAGE_DEFAULT.trim());
                    }
                }
                throw e;
            }
            case null, default -> throw e;
        }
    }

    private <T extends IEntity<?>> void setAuditInfo(final T entity) {
        final String currentUser = userAccess.getUsername().orElse(null);
        final Instant now = Instant.now().truncatedTo(ChronoUnit.SECONDS);

        if (entity.isNew()) {
            entity.setCreatedBy(currentUser);
            entity.setCreatedAt(now);
        }

        entity.setUpdatedBy(currentUser);
        entity.setUpdatedAt(now);
    }

    private <T> void applyAuditUpdate(final JPAUpdateClause clause,
                                      final Class<T> entityClass,
                                      final PathBuilder<T> entityPath) {
        final Instant now = Instant.now().truncatedTo(ChronoUnit.SECONDS);
        final String currentUser = userAccess.getUsernameOrNull();
        final AuditFieldMetadata auditFieldMetadata = getAuditFieldMetadata(entityClass);

        if (auditFieldMetadata.hasUpdatedAt()) {
            clause.set(entityPath.getDateTime("updatedAt", Instant.class), now);
        }
        if (auditFieldMetadata.hasUpdatedBy()) {
            clause.set(entityPath.getString("updatedBy"), currentUser);
        }
    }

    private AuditFieldMetadata getAuditFieldMetadata(final Class<?> entityClass) {
        return auditFieldCache.computeIfAbsent(entityClass, this::resolveAuditFieldMetadata);
    }

    private AuditFieldMetadata resolveAuditFieldMetadata(final Class<?> entityClass) {
        return new AuditFieldMetadata(hasField(entityClass, "updatedAt"), hasField(entityClass, "updatedBy"));
    }

    private <T extends IEntity<?>> void applyContextScope(final T entity) {
        applyTenantScope(entity);
        applyOrgScope(entity);
    }

    private <T extends IEntity<?>> void applyTenantScope(final T entity) {
        if (!(entity instanceof TenantScoped tenantScoped)) {
            return;
        }

        final Long currentTenantId = tenantContext.getCurrentTenantId().orElse(null);
        final Long entityTenantId = tenantScoped.getIdEntidade();

        if (currentTenantId == null) {
            if (entityTenantId == null) {
                throw new com.br.pokefichas.commons.exception.ValidationException(
                        "idEntidade e obrigatorio para registros multi-tenant"
                );
            }
            return;
        }

        if (entityTenantId == null) {
            tenantScoped.setIdEntidade(currentTenantId);
            return;
        }

        if (!currentTenantId.equals(entityTenantId)) {
            throw new UnauthorizedException("Operacao fora do tenant atual");
        }
    }

    private <T extends IEntity<?>> void applyOrgScope(final T entity) {
        if (!(entity instanceof OrgScoped orgScoped)) {
            return;
        }

        final Long currentOrgId = organizacaoContext.getCurrentOrganizacaoId().orElse(null);
        final Long entityOrgId = orgScoped.getIdOrganizacao();

        if (currentOrgId == null) {
            if (entityOrgId == null) {
                throw new com.br.pokefichas.commons.exception.ValidationException(
                        "idOrganizacao e obrigatorio para registros multi-organizacao"
                );
            }
            return;
        }

        if (entityOrgId == null) {
            orgScoped.setIdOrganizacao(currentOrgId);
            return;
        }

        if (!currentOrgId.equals(entityOrgId)) {
            throw new UnauthorizedException("Operacao fora da organizacao atual");
        }
    }

    private <T> T enforceContextAccess(final T entity, final boolean throwWhenDenied) {
        final T afterTenant = enforceTenantAccess(entity, throwWhenDenied);
        if (afterTenant == null) {
            return null;
        }
        return enforceOrgAccess(afterTenant, throwWhenDenied);
    }

    private <T> T enforceTenantAccess(final T entity, final boolean throwWhenDenied) {
        if (!(entity instanceof TenantScoped tenantScoped)) {
            return entity;
        }

        final Long currentTenantId = tenantContext.getCurrentTenantId().orElse(null);
        if (currentTenantId == null || Objects.equals(currentTenantId, tenantScoped.getIdEntidade())) {
            return entity;
        }

        if (throwWhenDenied) {
            throw new com.br.pokefichas.commons.exception.EntityNotFoundException("Registro nao encontrado");
        }
        return null;
    }

    private <T> T enforceOrgAccess(final T entity, final boolean throwWhenDenied) {
        if (!(entity instanceof OrgScoped orgScoped)) {
            return entity;
        }

        final Long currentOrgId = organizacaoContext.getCurrentOrganizacaoId().orElse(null);
        if (currentOrgId == null || Objects.equals(currentOrgId, orgScoped.getIdOrganizacao())) {
            return entity;
        }

        if (throwWhenDenied) {
            throw new com.br.pokefichas.commons.exception.EntityNotFoundException("Registro nao encontrado");
        }
        return null;
    }

    private <T> Optional<T> findContextScopedOptional(final Class<T> entityClass, final Serializable id) {
        return Optional.ofNullable(query(entityClass)
                .where(buildIdPredicate(entityClass, id))
                .fetchOne());
    }

    private boolean isContextScopedEntity(final Class<?> entityClass) {
        return isTenantEntity(entityClass) || isOrgEntity(entityClass);
    }

    private <T> Predicate buildIdPredicate(final Class<T> entityClass, final Serializable id) {
        final PathBuilder<T> entityPath = pathBuilderFactory.create(entityClass);
        @SuppressWarnings("unchecked")
        final Class<Serializable> idClass = (Class<Serializable>) id.getClass();
        return entityPath.getSimple("id", idClass).eq(id);
    }

    private <T> void applyTenantPredicate(final JPQLQuery<?> query,
                                          final Class<T> entityClass,
                                          final PathBuilder<T> entityPath,
                                          final boolean skipContextFilter) {
        if (skipContextFilter) {
            return;
        }
        if (isTenantEntity(entityClass)) {
            query.where(buildTenantPredicate(entityPath));
            return;
        }
        if (isOrgEntity(entityClass)) {
            query.where(buildOrgPredicate(entityPath));
        }
    }

    private <T> void applyTenantPredicate(final JPAUpdateClause clause,
                                          final Class<T> entityClass,
                                          final PathBuilder<T> entityPath) {
        if (isTenantEntity(entityClass)) {
            clause.where(buildTenantPredicate(entityPath));
            return;
        }
        if (isOrgEntity(entityClass)) {
            clause.where(buildOrgPredicate(entityPath));
        }
    }

    private <T> void applyTenantPredicate(final JPADeleteClause clause,
                                          final Class<T> entityClass,
                                          final PathBuilder<T> entityPath) {
        if (isTenantEntity(entityClass)) {
            clause.where(buildTenantPredicate(entityPath));
            return;
        }
        if (isOrgEntity(entityClass)) {
            clause.where(buildOrgPredicate(entityPath));
        }
    }

    private <T> Predicate buildTenantPredicate(final PathBuilder<T> entityPath) {
        return entityPath.getNumber("idEntidade", Long.class).eq(tenantContext.getRequiredTenantId());
    }

    private <T> Predicate buildOrgPredicate(final PathBuilder<T> entityPath) {
        return entityPath.getNumber("idOrganizacao", Long.class).eq(organizacaoContext.getRequiredOrganizacaoId());
    }

    private boolean isTenantEntity(final Class<?> entityClass) {
        return TenantBaseEntity.class.isAssignableFrom(entityClass);
    }

    private boolean isOrgEntity(final Class<?> entityClass) {
        return OrgBaseEntity.class.isAssignableFrom(entityClass);
    }

    private boolean hasField(final Class<?> entityClass, final String fieldName) {
        Class<?> currentClass = entityClass;
        while (currentClass != null && currentClass != Object.class) {
            try {
                currentClass.getDeclaredField(fieldName);
                return true;
            } catch (NoSuchFieldException ignored) {
                currentClass = currentClass.getSuperclass();
            }
        }
        return false;
    }

    private record AuditFieldMetadata(boolean hasUpdatedAt, boolean hasUpdatedBy) {
    }
}
