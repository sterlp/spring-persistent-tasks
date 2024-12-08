package org.sterl.spring.persistent_tasks.trigger.model;

import java.time.OffsetDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Lob;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
@ToString(of = {"status", "priority", "executionCount", "created", "triggerTime", "start", "end"})
public class BaseTriggerData {

    @Default
    @Column(updatable = false, name = "created_time")
    private OffsetDateTime created = OffsetDateTime.now();

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

    @Lob
    private byte[] state;
    
    @Column(length = 512)
    private String exceptionName;
    @Lob
    private String lastException;
}
