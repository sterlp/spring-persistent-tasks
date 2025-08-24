package org.sterl.spring.persistent_tasks.shared.repository;

import java.time.OffsetDateTime;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.sterl.spring.persistent_tasks.api.TriggerGroup;
import org.sterl.spring.persistent_tasks.api.TriggerKey;
import org.sterl.spring.persistent_tasks.api.TriggerSearch;
import org.sterl.spring.persistent_tasks.api.TriggerStatus;
import org.sterl.spring.persistent_tasks.shared.QueryHelper;
import org.sterl.spring.persistent_tasks.shared.StringHelper;
import org.sterl.spring.persistent_tasks.shared.model.HasTrigger;
import org.sterl.spring.persistent_tasks.shared.model.QTriggerEntity;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.jpa.impl.JPAQuery;

@NoRepositoryBean
public interface TriggerRepository<T extends HasTrigger> extends JpaRepository<T, Long>, QuerydslPredicateExecutor<T> {

    default Predicate buildSearch(
            @NonNull QTriggerEntity qData,
            @NonNull TriggerSearch search) {

        final var predicate = new BooleanBuilder();

        if (search.getSearch() != null) {
            Predicate pId;
            if (StringHelper.isSqlSearch(search.getSearch())) {
                final var value = StringHelper.applySearchWildCard(search.getSearch());
                pId = ExpressionUtils.anyOf(
                        qData.key.id.like(value),
                        qData.correlationId.like(value),
                        qData.tag.like(value)
                    );
            } else {
                final var value = search.getSearch();
                pId = ExpressionUtils.anyOf(
                        qData.key.id.eq(value),
                        qData.correlationId.eq(value),
                        qData.tag.eq(value)
                    );
            }
            predicate.and(pId);
        }
        
        predicate.and(QueryHelper.eqOrLike(qData.key.id, search.getKeyId()));
        predicate.and(QueryHelper.eqOrLike(qData.key.taskName, search.getTaskName()));
        predicate.and(QueryHelper.eqOrLike(qData.correlationId, search.getCorrelationId()));
        predicate.and(QueryHelper.eqOrLike(qData.tag, search.getTag()));
        predicate.and(QueryHelper.eq(qData.status, search.getStatus()));

        return predicate;
    }
    
    default Page<TriggerGroup> findByGroup(
            @NonNull JPAQuery<HasTrigger> query,
            @NonNull QTriggerEntity data,
            @NonNull StringPath groupByField,
            @Nullable TriggerSearch search,
            Pageable page
        ) {

        if (search != null && search.hasValue()) {
            final var filter = buildSearch(data, search);
            query = query.where(filter);
        }
        query = query.groupBy(groupByField);

        final var totalCount = query.fetchCount();
        var q = query.select(Projections.constructor(TriggerGroup.class, 
                data.key.id.count(), groupByField,
                data.runningDurationInMs.sum(), data.executionCount.sum(),
                data.runAt.min(), data.createdTime.min(), 
                data.start.min(), data.end.max()));

        q.orderBy(groupByField.asc());
        q.offset(page.getOffset());
        q.limit((page.getPageNumber() + 1) * page.getPageSize());

        return new PageImpl<>(q.fetch(), page, totalCount);
    }

    @Query("""
           SELECT e FROM #{#entityName} e
           WHERE  e.data.key.taskName = :taskName
           """)
    Page<T> findAll(@Param("taskName") String taskName, Pageable page);
    
    @Query("""
           SELECT COUNT(e.id) 
           FROM #{#entityName} e WHERE e.data.key.taskName = :taskName
           """)
    long countByTaskName(@Param("taskName") String taskName);

    @Query("""
            SELECT COUNT(e.id) 
            FROM #{#entityName} e WHERE e.data.key = :key
            """)
    long countByKey(@Param("key") TriggerKey key);

    @Query("""
           SELECT COUNT(e.id) 
           FROM #{#entityName} e
           WHERE e.data.status = :status
           """)
    long countByStatus(@Param("status") TriggerStatus status);

    @Query("""
            SELECT COUNT(e.id) 
            FROM #{#entityName} e
            WHERE e.data.status IN ( :status )
            """)
     long countByStatus(@Param("status") Set<TriggerStatus> status);
    
    @Query("""
           DELETE FROM #{#entityName} e
           WHERE e.data.createdTime < :age
           """)
    @Modifying
    long deleteOlderThan(@Param("age") OffsetDateTime age);
}
