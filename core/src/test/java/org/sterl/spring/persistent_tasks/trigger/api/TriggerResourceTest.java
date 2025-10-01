package org.sterl.spring.persistent_tasks.trigger.api;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.RestTemplate;
import org.sterl.spring.persistent_tasks.AbstractSpringTest;
import org.sterl.spring.persistent_tasks.AbstractSpringTest.TaskConfig.Task3;
import org.sterl.spring.persistent_tasks.api.TaskId;
import org.sterl.spring.persistent_tasks.api.TaskId.TriggerBuilder;
import org.sterl.spring.persistent_tasks.api.Trigger;
import org.sterl.spring.persistent_tasks.api.TriggerKey;
import org.sterl.spring.persistent_tasks.api.TriggerStatus;
import org.sterl.spring.persistent_tasks.shared.model.TriggerEntity;
import org.sterl.spring.persistent_tasks.trigger.model.RunningTriggerEntity;
import org.sterl.spring.persistent_tasks.trigger.repository.RunningTriggerRepository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.github.f4b6a3.uuid.UuidCreator;

class TriggerResourceTest extends AbstractSpringTest {

    @LocalServerPort
    private int port;
    @Autowired
    private RunningTriggerRepository triggerRepository;
    private String baseUrl;
    private final RestTemplate template = new RestTemplate();

    @BeforeEach
    void setupRest() {
        baseUrl = "http://localhost:" + port + "/spring-tasks-api/triggers";
    }
    
    @Test
    void testList() {
        // GIVEN
        var k1 = createStatus(new TriggerKey("1-foo", "foo"), TriggerStatus.WAITING).getKey();
        var k2 = createStatus(new TriggerKey("2-foo", "bar"), TriggerStatus.WAITING).getKey();
        
        // WHEN
        var response = template.exchange(
                baseUrl,
                HttpMethod.GET,
                null,
                String.class);
        
        // THEN
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).contains(k1.getId());
        assertThat(response.getBody()).contains(k1.getTaskName());
        // AND
        assertThat(response.getBody()).contains(k2.getId());
        assertThat(response.getBody()).contains(k2.getTaskName());
    }
    
    @Test
    void testSearchById() {
        // GIVEN
        var uuid = UUID.randomUUID().toString();
        var key1 = triggerService.queue(TriggerBuilder
                .newTrigger("task1")
                    .id("[@foo:bar@hallo.de:" + uuid + "]")
                    .build())
                .getKey();
        var key2 = triggerService.queue(TriggerBuilder
                .newTrigger("task1").build()).getKey();
        
        // WHEN
        var response = template.exchange(
                baseUrl + "?search=*" + key2.getId().substring(5, 30) + "*",
                HttpMethod.GET,
                null,
                String.class);
        // THEN
        assertThat(response.getBody()).contains(key2.getId());
        assertThat(response.getBody()).doesNotContain(key1.getId());
        
        // WHEN
        response = template.exchange(
                baseUrl + "?search=" + key1.getId().substring(0, 30) + "*",
                HttpMethod.GET,
                null,
                String.class);
        
        // THEN
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).contains(key1.getId());
        assertThat(response.getBody()).doesNotContain(key2.getId());
        
        // WHEN exact search, no wild card
        response = template.exchange(
                baseUrl + "?search=" + key1.getId(),
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
    void testSearchByCorrelationId() {
        // GIVEN
        var t1 = triggerService.queue(TriggerBuilder.newTrigger("task1").correlationId("correlationId" + UUID.randomUUID().toString()).build());
        var t2 = triggerService.queue(TriggerBuilder.newTrigger("task1").build()); // null
        var t3 = triggerService.queue(TriggerBuilder.newTrigger("task2").correlationId("correlationId" + UUID.randomUUID().toString()).build());
        
        // WHEN
        var response = template.exchange(
                baseUrl + "?search=" + t3.getData().getCorrelationId().substring(0, 30) + "*",
                HttpMethod.GET,
                null,
                String.class);
        // THEN
        assertThat(response.getBody()).contains(t3.getData().getCorrelationId());
        assertThat(response.getBody()).doesNotContain(t1.getData().getCorrelationId());
        assertThat(response.getBody()).doesNotContain(t2.getData().getKey().getId());
    }

    @Test
    void testSearchByStatus() {
        // GIVEN
        var k1 = createStatus(new TriggerKey("1-foo", "foo"), TriggerStatus.WAITING).getKey();
        var k2 = createStatus(new TriggerKey("2-foo", "bar"), TriggerStatus.RUNNING).getKey();

        // WHEN
        var response = template.exchange(
                baseUrl + "?status=" + TriggerStatus.RUNNING,
                HttpMethod.GET,
                null,
                String.class);

        // THEN
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).contains(k2.getId());
        assertThat(response.getBody()).doesNotContain(k1.getId());
    }
    
    @Test
    void testCancel() {
        // GIVEN
        var template = new RestTemplate();
        
        var triggerKey = triggerService.queue(TriggerBuilder.newTrigger("task1").build()).getKey();
        
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
        persistentTaskTestService.runAllDueTrigger(OffsetDateTime.now());
        
        asserts.assertValue(Task3.NAME + "::Hallo");
        asserts.assertMissing(Task3.NAME + "::Hallo2");
        assertThat(triggerService.countTriggers(TriggerStatus.WAITING)).isOne();
        assertThat(response.getBody().getKey()).isEqualTo(triggerKey);
    }
    
    @Test
    void testGroupSearch() throws JsonMappingException, JsonProcessingException {
        TaskId<String> taskId = taskService.replace("foo", c -> asserts.info("foo"));
        triggerService.queue(taskId.newTrigger().id("1").correlationId("a1").tag("tag1").build());
        triggerService.queue(taskId.newTrigger().id("2").correlationId("a1").tag("tag1").build());
        triggerService.queue(taskId.newTrigger().id("3").correlationId("a1").tag("tag2").build());

        // WHEN
        var result = template.exchange(
                baseUrl + "-grouped?tag=tag1",
                HttpMethod.GET,
                null,
                String.class);

        assertThat(result.getBody()).contains("\"count\":2");
        assertThat(result.getBody()).contains("\"groupByValue\":\"a1\"");
    }
    
    @Test
    void testReadIncompatibleTriggerState() {
        // GIVEN we have a bad state we cannot read in the DB
        var key = UuidCreator.getTimeOrdered().toString();
        trx.execute(t -> {
            return triggerRepository.save(new RunningTriggerEntity(
                        new TriggerKey(key, "slowTask")
                    ).withState(new byte[] {12, 54, 33})
                );
        });
        
        // WHEN
        assertThat(triggerRepository.count()).isGreaterThanOrEqualTo(1L);
        var response = template.getForEntity(baseUrl, String.class);
        
        // THEN
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains(key);
        assertThat(response.getBody()).contains("Failed to deserialize state of length");
    }
    
    private RunningTriggerEntity createStatus(TriggerKey key, TriggerStatus status) {
        final var now = OffsetDateTime.now();
        final var isCancel = status == TriggerStatus.CANCELED;

        var result = new RunningTriggerEntity();
        result.setData(TriggerEntity
                .builder()
                .correlationId(UUID.randomUUID().toString())
                .start(isCancel ? null : now.minusMinutes(1))
                .end(isCancel ? null : now)
                .createdTime(now)
                .key(key)
                .status(status)
                .runningDurationInMs(isCancel ? null : 600L)
                .build()
            );
        
        return triggerRepository.save(result);
    }

}
