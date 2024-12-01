package org.sterl.spring.persistent_tasks.api.event;

import org.sterl.spring.persistent_tasks.trigger.model.TriggerEntity;

public record TriggerCompleteEvent(TriggerEntity trigger) implements PersistentTaskEvent {

}
