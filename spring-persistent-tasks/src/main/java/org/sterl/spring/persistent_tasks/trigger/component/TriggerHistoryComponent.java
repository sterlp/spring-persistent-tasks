package org.sterl.spring.persistent_tasks.trigger.component;

import org.sterl.spring.persistent_tasks.shared.stereotype.TransactionalCompontant;
import org.sterl.spring.persistent_tasks.trigger.model.TriggerEntity;
import org.sterl.spring.persistent_tasks.trigger.model.TriggerHistoryEntity;
import org.sterl.spring.persistent_tasks.trigger.repository.TriggerHistoryRepository;

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
}
