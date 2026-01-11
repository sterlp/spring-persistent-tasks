package org.sterl.spring.persistent_tasks.trigger.component;

import java.io.Serializable;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;
import org.sterl.spring.persistent_tasks.trigger.model.CronTriggerEntity;
import org.sterl.spring.persistent_tasks.trigger.repository.CronTriggerRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Create new trigger for {@link CronTriggerEntity}s if required.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class QueueCronTriggerComponent {

    private final ReadTriggerComponent readTrigger;
    private final EditTriggerComponent editTrigger;
    private final TransactionTemplate trx;
    private final CronTriggerRepository cronTriggerRepository;

    /**
     * Ensures all non-suspended cron triggers have corresponding database triggers.
     * Creates missing triggers with next scheduled execution time.
     *
     * @return count of created triggers
     */
    public int execute() {
        var cronTriggers = cronTriggerRepository.getAll();
        int created = 0;

        for (CronTriggerEntity<?> cronTrigger : cronTriggers) {
            if (cronTrigger.isSuspended()) {
                continue;  // Skip suspended cron triggers
            }

            if (execute(cronTrigger)) {
                created++;
            }
        }

        return created;
    }

    /**
     * Creates a new trigger instance for the given {@link CronTriggerEntity} if none exist.
     *
     * @param cronTrigger the cron trigger definition
     * @return <code>true</code> if created, <code>false</code> if already exists
     */
    public <T extends Serializable> boolean execute(CronTriggerEntity<T> cronTrigger) {
        var key = cronTrigger.key();
        var allreadyQueued = trx.execute(t -> readTrigger.countByKey(key));
        if (allreadyQueued.longValue() > 0) return false;
        
        try {
            trx.execute(t -> editTrigger.addTrigger(cronTrigger.newTriggerRequest()));
            return true;
        } catch (DataIntegrityViolationException e) {
            // Another node created the trigger - ignore
            log.info("Trigger for cron {} already exists (race condition)", cronTrigger.getId());
            return false;
        }
    }
}
