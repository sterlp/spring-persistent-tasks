package org.sterl.spring.task.component;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.sterl.spring.task.model.TriggerEntity;
import org.sterl.spring.task.model.TriggerHistoryEntity;
import org.sterl.spring.task.repository.TriggerHistoryRepository;

import lombok.RequiredArgsConstructor;

@Component
@Transactional(propagation = Propagation.MANDATORY)
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
