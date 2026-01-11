package org.sterl.spring.persistent_tasks.trigger.repository;

import java.io.Serializable;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.sterl.spring.persistent_tasks.api.TriggerKey;
import org.sterl.spring.persistent_tasks.trigger.model.CronTriggerEntity;

import lombok.extern.slf4j.Slf4j;

/**
 * In-memory repository for cron trigger definitions.
 * Cron triggers are registered at startup and live in memory like task definitions.
 */
@Slf4j
@Component
public class CronTriggerRepository {

    private final ConcurrentMap<TriggerKey, CronTriggerEntity<? extends Serializable>> cronTriggerEntities = new ConcurrentHashMap<>();

    /**
     * Registers a cron trigger.
     *
     * @param cronTrigger the cron trigger to register
     * @return they unique {@link TriggerKey}
     * @throws IllegalStateException if a trigger with this ID already exists
     */
    public TriggerKey register(@NonNull CronTriggerEntity<? extends Serializable> cronTrigger) {
        final var key = cronTrigger.key();
        if (cronTriggerEntities.containsKey(key)) {
            throw new IllegalStateException("Cron trigger with '" + key + "' is already registered!");
        }
        cronTriggerEntities.put(key , cronTrigger);
        log.info("Registered cron for={}", cronTrigger);
        return key;
    }

    /**
     * Gets a cron trigger by ID.
     *
     * @param id the trigger ID
     * @return the cron trigger if found
     */
    public Optional<CronTriggerEntity<? extends Serializable>> get(@NonNull TriggerKey key) {
        return Optional.ofNullable(cronTriggerEntities.get(key));
    }

    /**
     * Returns all registered cron triggers.
     *
     * @return collection of all cron triggers
     */
    public Collection<CronTriggerEntity<? extends Serializable>> getAll() {
        return cronTriggerEntities.values();
    }

    public boolean suspend(@NonNull TriggerKey key) {
        var cronTrigger = cronTriggerEntities.get(key);
        if (cronTrigger != null) {
            cronTriggerEntities.put(key, cronTrigger.withSuspended(true));
            log.info("Suspended cron={}", key);
            return true;
        }
        return false;
    }
    public boolean resume(@NonNull TriggerKey key) {
        var cronTrigger = cronTriggerEntities.get(key);
        if (cronTrigger != null) {
            cronTriggerEntities.put(key, cronTrigger.withSuspended(false));
            log.info("Resumed cron={}", key);
            return true;
        }
        return false;
    }

    /**
     * Unregisters a cron trigger.
     *
     * @param id the trigger ID
     * @return the removed cron trigger, or empty if not found
     */
    public Optional<CronTriggerEntity<? extends Serializable>> unregister(@NonNull TriggerKey key) {
        CronTriggerEntity<? extends Serializable> removed = cronTriggerEntities.remove(key);
        if (removed != null) {
            log.info("Unregistered cron={}", key);
        }
        return Optional.ofNullable(removed);
    }

    /**
     * Removes all cron triggers (for testing).
     */
    public void deleteAll() {
        log.warn("*** All cron triggers {} will be removed now! ***", cronTriggerEntities.size());
        cronTriggerEntities.clear();
    }

    /**
     * Returns the number of registered cron triggers.
     */
    public int size() {
        return cronTriggerEntities.size();
    }

    /**
     * Checks if a cron trigger with the given ID exists.
     */
    public boolean contains(@NonNull TriggerKey key) {
        return cronTriggerEntities.containsKey(key);
    }
}
