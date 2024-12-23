package org.sterl.spring.sample_app.component;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.sterl.spring.persistent_tasks.AbstractSpringTest;
import org.sterl.spring.persistent_tasks.scheduler.component.EditSchedulerStatusComponent;

class EditSchedulerStatusComponentTest extends AbstractSpringTest {

    @Autowired
    private EditSchedulerStatusComponent subject;

    @Test
    void setSchedulersOfflineTest() throws Exception {
        // WHEN & THEN
        assertThat(subject.findOnlineSchedulers(Duration.ofSeconds(5)).size()).isEqualTo(2);
        // WHEN & THEN
        assertThat(subject.findOnlineSchedulers(Duration.ofSeconds(-1)).size()).isZero();
    }
}
