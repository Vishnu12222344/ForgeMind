package com.forgemind.projects.repository;

import com.forgemind.projects.entity.ProjectTag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ProjectTagRepository extends JpaRepository<ProjectTag, UUID> {

    List<ProjectTag> findByProject_Id(UUID projectId);

    void deleteByProject_Id(UUID projectId);
}