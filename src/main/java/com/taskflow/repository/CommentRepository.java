
package com.taskflow.repository;

import com.taskflow.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    // Find all comments belonging to a particular task
    List<Comment> findByTask_Id(Long taskId);

    // Find all comments written by a particular user
    List<Comment> findByUser_Id(Long userId);
}
