package org.sterl.spring.persistent_tasks.history.component;

import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.sterl.spring.persistent_tasks.api.event.TriggerLifeCycleEvent;
import org.sterl.spring.persistent_tasks.history.model.TriggerHistoryEntity;
import org.sterl.spring.persistent_tasks.history.repository.TriggerHistoryRepository;
import org.sterl.spring.persistent_tasks.shared.stereotype.TransactionalCompontant;
import org.sterl.spring.persistent_tasks.trigger.model.TriggerEntity;

import lombok.RequiredArgsConstructor;

@TransactionalCompontant
@RequiredArgsConstructor
public class TriggerHistoryComponent {

    private final TriggerHistoryRepository historyRepository;

    public TriggerHistoryEntity write(TriggerEntity e) {
        var result = new TriggerHistoryEntity();
        result.setData(e.getData().toBuilder().build());
        result.setTriggerId(e.getId().toBuilder().build());
        return historyRepository.save(result);
    }
    
    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void onPersistentTaskEvent(TriggerLifeCycleEvent triggerLifeCycleEvent) {
        write(triggerLifeCycleEvent.trigger());
    }
}
