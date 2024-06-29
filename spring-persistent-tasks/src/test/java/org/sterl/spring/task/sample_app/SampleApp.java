package org.sterl.spring.task.sample_app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.sterl.spring.task.EnablePersistentTasks;

@SpringBootApplication
@EnablePersistentTasks
public class SampleApp {

    public static void main(String[] args) {
        SpringApplication.run(SampleApp.class, args);
    }
}
