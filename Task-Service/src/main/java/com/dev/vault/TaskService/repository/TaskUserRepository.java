package com.dev.vault.TaskService.repository;

import com.dev.vault.TaskService.model.entity.TaskUser;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskUserRepository extends JpaRepository<TaskUser, Long> {

    TaskUser findByUserId(Long userId);


    boolean existsByUserIdAndTask_TaskId(long userId, Long taskId);

}