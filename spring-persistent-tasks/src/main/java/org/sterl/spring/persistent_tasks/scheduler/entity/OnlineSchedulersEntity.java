package org.sterl.spring.persistent_tasks.scheduler.entity;

import java.util.Set;

public record OnlineSchedulersEntity(Set<String> online, int countOffline) {

    public boolean hasSchedulersOffline() {
        return countOffline > 0;
    }
}
