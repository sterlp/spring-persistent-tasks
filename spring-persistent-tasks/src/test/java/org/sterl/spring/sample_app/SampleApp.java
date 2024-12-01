package org.sterl.spring.sample_app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.sterl.spring.persistent_tasks.EnablePersistentTasks;

@SpringBootApplication
@EnablePersistentTasks
public class SampleApp {

    public static void main(String[] args) {
        SpringApplication.run(SampleApp.class, args);
    }
}
