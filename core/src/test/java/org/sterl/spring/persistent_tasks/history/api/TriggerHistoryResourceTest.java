package org.sterl.spring.persistent_tasks.history.api;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.web.client.RestTemplate;
import org.sterl.spring.persistent_tasks.AbstractSpringTest;
import org.sterl.spring.persistent_tasks.trigger.repository.RunningTriggerRepository;

class TriggerHistoryResourceTest extends AbstractSpringTest {

    @LocalServerPort
    private int port;
    @Autowired
    private RunningTriggerRepository triggerRepository;
    private String baseUrl;
    private final RestTemplate template = new RestTemplate();

    @BeforeEach
    void setupRest() {
        baseUrl = "http://localhost:" + port + "/spring-tasks-api";
    }

    @Test
    void test() {
        fail("Not yet implemented");
    }

}
