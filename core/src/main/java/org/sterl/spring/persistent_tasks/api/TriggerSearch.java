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
                || StringHelper.hasValue(tag)
                || status != null;
    }
    
    
    public static TriggerSearch byCorrelationId(String correlationId) {
        var result = new TriggerSearch();
        result.setCorrelationId(correlationId);
        return result;
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
