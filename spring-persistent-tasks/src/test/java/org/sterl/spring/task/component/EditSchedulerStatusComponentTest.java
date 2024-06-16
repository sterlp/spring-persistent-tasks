package org.sterl.spring.task.component;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.sterl.spring.task.SampleApp;

@SpringBootTest(classes = SampleApp.class)
class EditSchedulerStatusComponentTest {

    @Autowired EditSchedulerStatusComponent subject;
    
    @Test
    void setSchedulersOfflineTest() throws Exception {
        // WHEN & THEN
        assertThat(subject.setSchedulersOffline(Duration.ofSeconds(5))).isZero();
        // WHEN & THEN
        assertThat(subject.setSchedulersOffline(Duration.ofSeconds(-1))).isOne();
    }
}
