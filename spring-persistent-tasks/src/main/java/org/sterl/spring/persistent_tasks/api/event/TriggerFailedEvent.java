package org.sterl.spring.persistent_tasks.api.event;

import org.sterl.spring.persistent_tasks.trigger.model.TriggerEntity;

public record TriggerFailedEvent(TriggerEntity trigger) implements PersistentTaskEvent {

}
