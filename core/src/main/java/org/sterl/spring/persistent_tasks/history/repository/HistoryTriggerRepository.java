package org.sterl.spring.persistent_tasks.history.repository;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.query.Param;
import org.sterl.spring.persistent_tasks.api.TriggerKey;
import org.sterl.spring.persistent_tasks.shared.model.HasTriggerData;
import org.sterl.spring.persistent_tasks.shared.repository.TriggerDataRepository;

@NoRepositoryBean
public interface HistoryTriggerRepository<T extends HasTriggerData> extends TriggerDataRepository<T> {

    @Query("""
            SELECT e FROM #{#entityName} e 
            WHERE e.data.key = :key
            """)
    List<T> listKnownStatusFor(@Param("key") TriggerKey key, Pageable page);
}
