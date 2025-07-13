package org.sterl.spring.persistent_tasks.history.component;

import java.time.OffsetDateTime;

import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.sterl.spring.persistent_tasks.history.model.CompletedTriggerEntity;
import org.sterl.spring.persistent_tasks.history.model.HistoryTriggerEntity;
import org.sterl.spring.persistent_tasks.history.repository.CompletedTriggerRepository;
import org.sterl.spring.persistent_tasks.history.repository.TriggerHistoryDetailRepository;
import org.sterl.spring.persistent_tasks.shared.model.TriggerEntity;
import org.sterl.spring.persistent_tasks.trigger.event.TriggerLifeCycleEvent;
import org.sterl.spring.persistent_tasks.trigger.event.TriggerRunningEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class TriggerHistoryComponent {

    private final CompletedTriggerRepository completedTriggerRepository;
    private final TriggerHistoryDetailRepository triggerHistoryDetailRepository;

    // we have to ensure to run in an own transaction
    // as if the trigger fails, a rollback would also remove this entry
    // furthermore async to ensure that we would not block
    // as REQURES_NEW would block two DB connections ...
    @Async("triggerHistoryExecutor")
    @Transactional(timeout = 10)
    @EventListener
    public void onRunning(TriggerRunningEvent e) {
        log.debug("Received event={} for {} new status={}",
                e.getClass().getSimpleName(),
                e.key(), e.status());
        
        execute(e.id(), e.data(), false);
    }
    
    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    void onPersistentTaskEvent(TriggerLifeCycleEvent e) {
        if (e instanceof TriggerRunningEvent) return; // we have an own listener for that
        log.debug("Received event={} for {} new status={}",
                e.getClass().getSimpleName(),
                e.key(), e.status());
        
        
        execute(e.id(), e.data(), e.isDone());
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void execute(final long triggerId, final TriggerEntity data, boolean isDone) {
        if (isDone) {
            final var state = new CompletedTriggerEntity();
            state.setId(triggerId);
            state.setData(data.copy());
            completedTriggerRepository.save(state);
        }

        var detail = new HistoryTriggerEntity();
        detail.setInstanceId(triggerId);
        detail.setData(data.toBuilder()
                .state(null)
                .createdTime(OffsetDateTime.now())
                .build());
        triggerHistoryDetailRepository.save(detail);
    }
}
