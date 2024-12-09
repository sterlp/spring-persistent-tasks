package org.sterl.spring.persistent_tasks.shared.model;

import java.time.OffsetDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Lob;
import jakarta.persistence.PrePersist;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Basically a value class for the ability to clone the data more easily
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
@ToString(of = {"status", "priority", "executionCount", "createdTime", "triggerTime", "start", "end"})
@Builder(toBuilder = true)
public class TriggerData {
    
    @PrePersist
    void beforeSave() {
        if (start != null && end != null) {
            runningDurationInMs = end.toInstant().toEpochMilli() - start.toInstant().toEpochMilli();
        }
    }

    @Default
    @Column(updatable = false, name = "created_time")
    private OffsetDateTime createdTime = OffsetDateTime.now();

    @Default
    @Column(nullable = false)
    private OffsetDateTime triggerTime = OffsetDateTime.now();

    @Column(name = "start_time")
    private OffsetDateTime start;

    @Column(name = "end_time")
    private OffsetDateTime end;

    @Default
    private int executionCount = 0;

    /** priority, the higher a more priority it will get */
    @Default
    private int priority = 4;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    @Default
    private TriggerStatus status = TriggerStatus.NEW;
    
    private Long runningDurationInMs;

    @Lob
    private byte[] state;

    @Column(length = 512)
    private String exceptionName;
    @Lob
    private String lastException;
}
