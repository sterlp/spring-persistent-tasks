package org.sterl.spring.persistent_tasks.history.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.sterl.spring.persistent_tasks.api.HistoryOverview;
import org.sterl.spring.persistent_tasks.history.model.TriggerHistoryDetailEntity;
import org.sterl.spring.persistent_tasks.history.model.TriggerHistoryLastStateEntity;

public interface TriggerHistoryLastStateRepository extends HistoryTriggerRepository<TriggerHistoryLastStateEntity> {

    
}
