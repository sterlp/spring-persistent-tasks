package org.sterl.spring.persistent_tasks.api;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.sterl.spring.persistent_tasks.shared.StringHelper;

import lombok.Data;

@Data
public class TriggerSearch {
    private String search;
    private String keyId;
    private String taskName;
    private String correlationId;
    private TriggerStatus status;
    private String tag;
    
    public boolean hasValue() {
        return StringHelper.hasValue(search)
                || StringHelper.hasValue(keyId)
                || StringHelper.hasValue(taskName)
                || StringHelper.hasValue(correlationId)
                || status != null
                || StringHelper.hasValue(tag);
    }
    
    public static TriggerSearch byCorrelationId(String correlationId) {
        var result = new TriggerSearch();
        result.setCorrelationId(correlationId);
        return result;
    }

    public static TriggerSearch byStatus(TriggerStatus status) {
        var result = new TriggerSearch();
        result.setStatus(status);
        return result;
    }

    public static TriggerSearch forTriggerRequest(TriggerRequest<?> trigger) {
        var search = new TriggerSearch();
        if (trigger.key() != null) {
            search.setKeyId(trigger.key().getId());
            search.setTaskName(trigger.key().getTaskName());
        }

        if (trigger.correlationId() != null) search.setCorrelationId(trigger.correlationId());
        if (trigger.tag() != null) search.setTag(trigger.tag());
        if (trigger.status() != null) search.setStatus(trigger.status());

        return search;
    }
    
    /** create time ASC */
    public static final Sort DEFAULT_SORT = sortByCreatedTime(Direction.ASC);

    public static Sort sortByCreatedTime(Direction direction) {
        return Sort.by(direction, "data.createdTime");
    }

    public static Pageable applyDefaultSortIfNeeded(Pageable page) {
        var result = page;
        if (page.getSort() == Sort.unsorted()) {
            result = PageRequest.of(page.getPageNumber(), page.getPageSize(), DEFAULT_SORT);
        }
        return result;
    }
}
