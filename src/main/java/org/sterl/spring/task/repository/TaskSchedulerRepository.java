package org.sterl.spring.task.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.sterl.spring.task.model.TaskSchedulerEntity;

public interface TaskSchedulerRepository extends JpaRepository<TaskSchedulerEntity, String>{

}
