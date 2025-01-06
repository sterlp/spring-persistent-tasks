package org.sterl.spring.persistent_tasks.trigger.api;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.time.OffsetDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.RestTemplate;
import org.sterl.spring.persistent_tasks.AbstractSpringTest;
import org.sterl.spring.persistent_tasks.AbstractSpringTest.TaskConfig.Task3;
import org.sterl.spring.persistent_tasks.api.TaskId.TaskTriggerBuilder;
import org.sterl.spring.persistent_tasks.api.Trigger;
import org.sterl.spring.persistent_tasks.shared.model.TriggerStatus;

class TriggerResourceTest extends AbstractSpringTest {

    @LocalServerPort
    private int port;
    private String baseUrl;
    private final RestTemplate template = new RestTemplate();

    @BeforeEach
    void setupRest() {
        baseUrl = "http://localhost:" + port + "/spring-tasks-api/triggers";
    }
    
    @Test
    void testList() {
        // GIVEN
        var triggerKey = triggerService.queue(TaskTriggerBuilder.newTrigger("task1").build()).getKey();
        
        // WHEN
        var response = template.exchange(
                baseUrl,
                HttpMethod.GET,
                null,
                String.class);
        
        // THEN
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).contains(triggerKey.getId());
        assertThat(response.getBody()).contains(triggerKey.getTaskName());
    }
    
    @Test
    void testSearchById() {
        // GIVEN
        var key1 = triggerService.queue(TaskTriggerBuilder
                .newTrigger("task1").build()).getKey();
        var key2 = triggerService.queue(TaskTriggerBuilder
                .newTrigger("task1").build()).getKey();
        
        // WHEN
        var response = template.exchange(
                baseUrl + "?id=" + key1.getId().substring(0, 30),
                HttpMethod.GET,
                null,
                String.class);
        
        // THEN
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).contains(key1.getId());
        assertThat(response.getBody()).doesNotContain(key2.getId());
    }
    
    @Test
    void testCancel() {
        // GIVEN
        var template = new RestTemplate();
        
        var triggerKey = triggerService.queue(TaskTriggerBuilder.newTrigger("task1").build()).getKey();
        
        // WHEN
        var canceled = template.exchange(baseUrl + "/" +
                triggerKey.getTaskName() + "/" + triggerKey.getId(), 
                HttpMethod.DELETE, (HttpEntity<?>)null, Trigger.class);
        
        // THEN
        assertThat(canceled.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(canceled.getBody()).isNotNull();
        assertThat(canceled.getBody().getKey()).isEqualTo(triggerKey);
    }
    
    @Test
    void testUpdateRunAt() {
        // GIVEN
        final var request = Task3.ID
                .newTrigger("Hallo")
                .runAfter(Duration.ofMinutes(5))
                .build();
        var triggerKey = triggerService.queue(request).getKey();

        triggerService.queue(Task3.ID
                .newTrigger("Hallo2")
                .runAfter(Duration.ofMinutes(5))
                .build());
        
        // WHEN
        var response = template.exchange(baseUrl + "/" + triggerKey.getTaskName() 
                + "/" + triggerKey.getId() + "/run-at", 
                HttpMethod.POST, new HttpEntity<>(OffsetDateTime.now()), Trigger.class);
        
        // THEN
        persistentTaskService.executeTriggersAndWait();
        asserts.assertValue(Task3.NAME + "::Hallo");
        asserts.assertMissing(Task3.NAME + "::Hallo2");
        assertThat(triggerService.countTriggers(TriggerStatus.WAITING)).isOne();
        assertThat(response.getBody().getKey()).isEqualTo(triggerKey);
    }

}
