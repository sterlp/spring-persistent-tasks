package org.sterl.spring.persistent_tasks.shared.repository;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.NonNull;
import org.sterl.spring.persistent_tasks.api.TriggerKey;
import org.sterl.spring.persistent_tasks.api.TriggerSearch;
import org.sterl.spring.persistent_tasks.api.TriggerStatus;
import org.sterl.spring.persistent_tasks.shared.QueryHelper;
import org.sterl.spring.persistent_tasks.shared.StringHelper;
import org.sterl.spring.persistent_tasks.shared.model.HasTriggerData;
import org.sterl.spring.persistent_tasks.shared.model.QTriggerData;
import org.sterl.spring.persistent_tasks.trigger.model.TriggerEntity;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Predicate;

@NoRepositoryBean
public interface TriggerDataRepository<T extends HasTriggerData> extends JpaRepository<T, Long>, QuerydslPredicateExecutor<T> {

    default Predicate buildSearch(
            @NonNull QTriggerData qData,
            @NonNull TriggerSearch search) {

        final var predicate = new BooleanBuilder();

        if (search.getSearch() != null) {
            final var value = StringHelper.applySearchWildCard(search.getSearch());
            Predicate pId;
            if (StringHelper.isSqlSearch(value)) {
                pId = ExpressionUtils.or(
                        qData.key.id.like(value),
                        qData.correlationId.like(value));
            } else {
                pId = ExpressionUtils.or(
                        qData.key.id.eq(value),
                        qData.correlationId.eq(value));
            }
            predicate.andAnyOf(pId);
        }
        
        predicate.and(QueryHelper.eqOrLike(qData.key.id, search.getKeyId()));
        predicate.and(QueryHelper.eqOrLike(qData.key.taskName, search.getTaskName()));
        predicate.and(QueryHelper.eqOrLike(qData.correlationId, search.getCorrelationId()));
        predicate.and(QueryHelper.eqOrLike(qData.tag, search.getTag()));
        predicate.and(QueryHelper.eq(qData.status, search.getStatus()));

        return predicate;
    }

    @Query("""
           SELECT e FROM #{#entityName} e
           WHERE  e.data.key.taskName = :taskName
           """)
    Page<T> findAll(@Param("taskName") String taskName, Pageable page);
    
    @Query("""
           SELECT COUNT(e.data.key) 
           FROM #{#entityName} e WHERE e.data.key.taskName = :taskName
           """)
    long countByTaskName(@Param("taskName") String taskName);

    @Query("""
            SELECT COUNT(e.data.key) 
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
