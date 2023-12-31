package com.dev.vault.ProjectService.repository;

import com.dev.vault.ProjectService.model.entity.Project;
import com.dev.vault.ProjectService.model.entity.ProjectMembers;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ProjectMembersRepository extends JpaRepository<ProjectMembers, Long> {
    @Query("""
             SELECT p FROM ProjectMembers p
             WHERE p.project =:project
            """)
    List<ProjectMembers> findByProject(Project project);


    @Query("""
            SELECT p FROM ProjectMembers p
            WHERE p.project.projectId =:projectId
            """)
    List<ProjectMembers> findByProject_ProjectId(Long projectId);


    Optional<ProjectMembers> findByProject_ProjectNameAndUserId(String projectName, Long userId);

}