package org.sterl.spring.sample_app.component;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.OffsetDateTime;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.sterl.spring.persistent_tasks.AbstractSpringTest;
import org.sterl.spring.persistent_tasks.scheduler.component.EditSchedulerStatusComponent;

class EditSchedulerStatusComponentTest extends AbstractSpringTest {

    @Autowired
    private EditSchedulerStatusComponent subject;

    @Test
    void setSchedulersOfflineTest() throws Exception {
        var now = OffsetDateTime.now();
        // WHEN & THEN
        assertThat(subject.findOnlineSchedulers(
                now.minusSeconds(1))).hasSize(2);
        // WHEN & THEN
        assertThat(subject.findOnlineSchedulers(
                now.plusSeconds(1))).isEmpty();
    }
}
