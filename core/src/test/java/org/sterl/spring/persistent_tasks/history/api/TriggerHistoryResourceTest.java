package org.sterl.spring.persistent_tasks.history.api;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.OffsetDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestTemplate;
import org.sterl.spring.persistent_tasks.AbstractSpringTest;
import org.sterl.spring.persistent_tasks.api.TaskId;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;

class TriggerHistoryResourceTest extends AbstractSpringTest {

    @LocalServerPort
    private int port;
    private String baseUrl;
    private final RestTemplate template = new RestTemplate();

    @BeforeEach
    void setupRest() {
        baseUrl = "http://localhost:" + port + "/spring-tasks-api/";
    }

    @Test
    void testGroupSearch() throws JsonMappingException, JsonProcessingException {
        TaskId<String> taskId = taskService.replace("foo", c -> asserts.info("foo"));
        triggerService.queue(taskId.newTrigger().id("1").correlationId("a1").tag("tag1").build());
        triggerService.queue(taskId.newTrigger().id("2").correlationId("a1").tag("tag1").build());
        triggerService.queue(taskId.newTrigger().id("3").correlationId("a1").tag("tag2").build());

        // WHEN
        persistentTaskTestService.runAllDueTrigger(OffsetDateTime.now().plusMinutes(1));
        var result = template.exchange(
                baseUrl + TriggerHistoryResource.PATH_GROUP + "?tag=tag1",
                HttpMethod.GET,
                null,
                String.class);

        assertThat(result.getBody()).contains("\"count\":2");
        assertThat(result.getBody()).contains("\"groupByValue\":\"a1\"");
    }

}
