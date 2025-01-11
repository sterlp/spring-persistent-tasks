package org.sterl.spring.persistent_tasks.history.component;

import java.time.OffsetDateTime;

import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.sterl.spring.persistent_tasks.history.model.TriggerHistoryDetailEntity;
import org.sterl.spring.persistent_tasks.history.model.TriggerHistoryLastStateEntity;
import org.sterl.spring.persistent_tasks.history.repository.TriggerHistoryDetailRepository;
import org.sterl.spring.persistent_tasks.history.repository.TriggerHistoryLastStateRepository;
import org.sterl.spring.persistent_tasks.shared.model.TriggerData;
import org.sterl.spring.persistent_tasks.shared.stereotype.TransactionalCompontant;
import org.sterl.spring.persistent_tasks.trigger.event.TriggerLifeCycleEvent;
import org.sterl.spring.persistent_tasks.trigger.event.TriggerRunningEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@TransactionalCompontant
@RequiredArgsConstructor
@Slf4j
public class TriggerHistoryComponent {

    private final TriggerHistoryLastStateRepository triggerHistoryLastStateRepository;
    private final TriggerHistoryDetailRepository triggerHistoryDetailRepository;

    // we have to ensure to run in an own transaction
    // as if the trigger fails, a rollback would also remove this entry
    // furthermore async to ensure that we would not block
    // as REQURES_NEW would block two DB connections ...
    @Async
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

    public void execute(final long triggerId, final TriggerData data, boolean isDone) {
        if (isDone) {
            final var state = new TriggerHistoryLastStateEntity();
            state.setId(triggerId);
            state.setData(data.copy());
            triggerHistoryLastStateRepository.save(state);
        }

        var detail = new TriggerHistoryDetailEntity();
        detail.setInstanceId(triggerId);
        detail.setData(data.toBuilder()
                .state(null)
                .createdTime(OffsetDateTime.now())
                .build());
        triggerHistoryDetailRepository.save(detail);
    }
}
