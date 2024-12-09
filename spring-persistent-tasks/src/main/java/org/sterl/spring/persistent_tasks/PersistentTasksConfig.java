package org.sterl.spring.persistent_tasks;

import org.springframework.boot.autoconfigure.AutoConfigurationPackage;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
@AutoConfigurationPackage(basePackageClasses = EnablePersistentTasks.class)
@ComponentScan(basePackageClasses = EnablePersistentTasks.class)
public class PersistentTasksConfig {

}
