
package com.taskflow.repository;

import com.taskflow.entity.Task;
import com.taskflow.entity.TaskPriority;
import com.taskflow.entity.TaskStatus;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
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


    long countByStatus(TaskStatus status);

    long countByPriority(TaskPriority priority);

    long countByAssignedTo_Id(Long userId);

    long countByAssignedTo_IdAndStatus(
            Long userId,
            TaskStatus status
    );

    long countByAssignedTo_IdAndDueDateBeforeAndStatusNot(
            Long userId,
            LocalDate date,
            TaskStatus status
    );

    List<Task> findByAssignedTo_IdAndDueDateAfterAndStatusNotOrderByDueDateAsc(
            Long userId,
            LocalDate date,
            TaskStatus status
    );


    long countByProject_CreatedBy_Id(Long managerId);

    long countByProject_CreatedBy_IdAndStatus(
            Long managerId,
            TaskStatus status
    );

    long countByProject_CreatedBy_IdAndPriority(
            Long managerId,
            TaskPriority priority
    );

    long countByProject_CreatedBy_IdAndDueDateBeforeAndStatusNot(
            Long managerId,
            LocalDate date,
            TaskStatus status
    );


    List<Task> findByProject_CreatedBy_Id(Long managerId);


}
