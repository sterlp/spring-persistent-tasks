package org.sterl.spring.persistent_tasks.trigger.api;

import org.springframework.lang.NonNull;
import org.sterl.spring.persistent_tasks.api.CronTriggerInfo;
import org.sterl.spring.persistent_tasks.shared.ExtendetConvert;
import org.sterl.spring.persistent_tasks.trigger.model.CronTriggerEntity;

public enum ToCronTriggerInfo implements ExtendetConvert<CronTriggerEntity<?>, CronTriggerInfo> {
    INSTANCE;

    @Override
    public CronTriggerInfo convert(@NonNull CronTriggerEntity<?> CronTriggerInfo) {
        var info = new CronTriggerInfo();
        info.setId(CronTriggerInfo.getId());
        info.setTaskName(CronTriggerInfo.getTaskId().name());
        info.setSchedule(CronTriggerInfo.getSchedule().description());
        info.setTag(CronTriggerInfo.getTag());
        info.setPriority(CronTriggerInfo.getPriority());
        info.setSuspended(CronTriggerInfo.isSuspended());
        info.setHasStateProvider(CronTriggerInfo.getStateProvider() != null);
        return info;
    }

}
