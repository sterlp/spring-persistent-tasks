package org.sterl.spring.persistent_tasks.history.component;

import java.time.OffsetDateTime;

import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.sterl.spring.persistent_tasks.history.model.LastTriggerStateEntity;
import org.sterl.spring.persistent_tasks.history.model.TriggerStateDetailEntity;
import org.sterl.spring.persistent_tasks.history.repository.LastTriggerStateRepository;
import org.sterl.spring.persistent_tasks.history.repository.TriggerStateDetailRepository;
import org.sterl.spring.persistent_tasks.shared.stereotype.TransactionalCompontant;
import org.sterl.spring.persistent_tasks.trigger.event.TriggerLifeCycleEvent;
import org.sterl.spring.persistent_tasks.trigger.model.TriggerEntity;

import lombok.RequiredArgsConstructor;

@TransactionalCompontant
@RequiredArgsConstructor
public class TriggerHistoryComponent {

    private final LastTriggerStateRepository lastTriggerStateRepository;
    private final TriggerStateDetailRepository triggerStateDetailRepository;

    public void write(TriggerEntity e) {
        var state = new LastTriggerStateEntity();
        state.setId(e.getId());
        state.setData(e.getData().toBuilder().build());
        lastTriggerStateRepository.save(state);
        
        
        var detail = new TriggerStateDetailEntity();
        detail.setInstanceId(e.getId());
        detail.setData(e.getData().toBuilder().build());
        detail.getData().setCreatedTime(OffsetDateTime.now());
        triggerStateDetailRepository.save(detail);
    }
    
    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void onPersistentTaskEvent(TriggerLifeCycleEvent triggerLifeCycleEvent) {
        write(triggerLifeCycleEvent.trigger());
    }
}
