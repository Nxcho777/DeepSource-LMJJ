package cl.duoc.deepsource.projects;

import org.springframework.data.jpa.repository.JpaRepository;

interface ProjectRepository extends JpaRepository<ProjectRow, String> {}
