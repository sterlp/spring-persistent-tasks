package org.sterl.spring.task.api.event;

import org.sterl.spring.task.model.TriggerEntity;

public record TriggerFailedEvent(TriggerEntity trigger) {

}
