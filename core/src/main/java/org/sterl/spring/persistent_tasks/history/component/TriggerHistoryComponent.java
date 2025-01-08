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
import org.sterl.spring.persistent_tasks.trigger.model.TriggerEntity;

import lombok.RequiredArgsConstructor;

@TransactionalCompontant
@RequiredArgsConstructor
public class TriggerHistoryComponent {

    private final TriggerHistoryLastStateRepository triggerHistoryLastStateRepository;
    private final TriggerHistoryDetailRepository triggerHistoryDetailRepository;

    public void write(TriggerEntity e) {
        var state = new TriggerHistoryLastStateEntity();
        state.setId(e.getId());
        state.setData(e.getData().toBuilder().build());
        triggerHistoryLastStateRepository.save(state);

        var detail = new TriggerHistoryDetailEntity();
        detail.setInstanceId(e.getId());
        detail.setData(e.getData().toBuilder()
                .state(null)
                .build());
        detail.getData().setCreatedTime(OffsetDateTime.now());
        triggerHistoryDetailRepository.save(detail);
    }
    
    @Transactional
    @EventListener
    public void onPersistentTaskEvent(TriggerLifeCycleEvent triggerLifeCycleEvent) {
        write(triggerLifeCycleEvent.trigger());
    }
}
