package com.taskflow.repository;

import com.taskflow.entity.ProjectMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectMemberRepository extends JpaRepository<ProjectMember, Long> {

    boolean existsByProject_IdAndUser_Id(Long projectId, Long userId);

    List<ProjectMember> findByProject_Id(Long projectId);

    Optional<ProjectMember> findByProject_IdAndUser_Id(Long projectId, Long userId);


    List<ProjectMember> findByProject_CreatedBy_Id(Long managerId);

}