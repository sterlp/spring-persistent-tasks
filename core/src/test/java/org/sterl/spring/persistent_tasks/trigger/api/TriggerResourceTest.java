package org.sterl.spring.persistent_tasks.trigger.api;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.web.PagedModel;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.RestTemplate;
import org.sterl.spring.persistent_tasks.AbstractSpringTest;
import org.sterl.spring.persistent_tasks.api.TaskId.TaskTriggerBuilder;
import org.sterl.spring.persistent_tasks.api.Trigger;

class TriggerResourceTest extends AbstractSpringTest {

    @LocalServerPort
    int port;
    
    @Test
    void testList() {
        // GIVEN
        var template = new RestTemplate();
        
        var triggerKey = triggerService.queue(TaskTriggerBuilder.newTrigger("task1").build()).getKey();
        
        // WHEN
        var response = template.exchange(
                "http://localhost:" + port + "/spring-tasks-api/triggers",
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
    void testCancel() {
        // GIVEN
        var template = new RestTemplate();
        
        var triggerKey = triggerService.queue(TaskTriggerBuilder.newTrigger("task1").build()).getKey();
        
        // WHEN
        var canceled = template.exchange("http://localhost:" + port + "/spring-tasks-api/triggers/" +
                triggerKey.getTaskName() + "/" + triggerKey.getId(), 
                HttpMethod.DELETE, (HttpEntity<?>)null, Trigger.class);
        
        // THEN
        assertThat(canceled.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(canceled.getBody()).isNotNull();
        assertThat(canceled.getBody().getKey()).isEqualTo(triggerKey);
    }

}
