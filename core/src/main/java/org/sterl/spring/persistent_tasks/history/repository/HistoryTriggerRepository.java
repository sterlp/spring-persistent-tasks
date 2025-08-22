package org.sterl.spring.persistent_tasks.history.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.query.Param;
import org.sterl.spring.persistent_tasks.api.TriggerKey;
import org.sterl.spring.persistent_tasks.shared.model.HasTrigger;
import org.sterl.spring.persistent_tasks.shared.repository.TriggerRepository;

@NoRepositoryBean
public interface HistoryTriggerRepository<T extends HasTrigger> extends TriggerRepository<T> {

    @Query("""
            SELECT e FROM #{#entityName} e 
            WHERE e.data.key = :key
            ORDER BY e.id DESC
            """)
    Page<T> listKnownStatusFor(@Param("key") TriggerKey key, Pageable page);
}
