package org.sterl.spring.persistent_tasks.history.component;

import java.time.OffsetDateTime;

import org.springframework.context.event.EventListener;
import org.springframework.transaction.annotation.Transactional;
import org.sterl.spring.persistent_tasks.history.model.TriggerHistoryDetailEntity;
import org.sterl.spring.persistent_tasks.history.model.TriggerHistoryLastStateEntity;
import org.sterl.spring.persistent_tasks.history.repository.TriggerHistoryDetailRepository;
import org.sterl.spring.persistent_tasks.history.repository.TriggerHistoryLastStateRepository;
import org.sterl.spring.persistent_tasks.shared.stereotype.TransactionalCompontant;
import org.sterl.spring.persistent_tasks.trigger.event.TriggerLifeCycleEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@TransactionalCompontant
@RequiredArgsConstructor
@Slf4j
public class TriggerHistoryComponent {

    private final TriggerHistoryLastStateRepository triggerHistoryLastStateRepository;
    private final TriggerHistoryDetailRepository triggerHistoryDetailRepository;

    @Transactional(timeout = 10)
    @EventListener
    public void onPersistentTaskEvent(TriggerLifeCycleEvent e) {
        log.debug("Received event={} for {} new status={}",
                e.getClass().getSimpleName(),
                e.key(), e.status());
        
        
        var state = new TriggerHistoryLastStateEntity();
        state.setId(e.id());
        state.setData(e.getData().copy());
        triggerHistoryLastStateRepository.save(state);

        var detail = new TriggerHistoryDetailEntity();
        detail.setInstanceId(e.id());
        detail.setData(e.getData().toBuilder()
                .state(null)
                .createdTime(OffsetDateTime.now())
                .build());
        triggerHistoryDetailRepository.save(detail);
    }
}
