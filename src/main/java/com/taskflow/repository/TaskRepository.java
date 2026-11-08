
package com.taskflow.repository;

import com.taskflow.entity.Task;
import com.taskflow.entity.TaskPriority;
import com.taskflow.entity.TaskStatus;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findByProject_Id(Long projectId);

    List<Task> findByAssignedTo_Id(Long userId);

    List<Task> findByProject_IdAndStatus(Long projectId, TaskStatus status);

    Page<Task> findByStatus(TaskStatus status, Pageable pageable);

    Page<Task> findByPriority(TaskPriority priority, Pageable pageable);

    Page<Task> findByTitleContainingIgnoreCase(
            String title,
            Pageable pageable
    );

    Page<Task> findByTitleContainingIgnoreCaseAndStatusAndPriority(
            String title,
            TaskStatus status,
            TaskPriority priority,
            Pageable pageable
    );
}
