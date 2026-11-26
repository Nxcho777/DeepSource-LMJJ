package cl.duoc.deepsource.tasks;

import org.springframework.data.jpa.repository.JpaRepository;

interface TaskRepository extends JpaRepository<TaskRow, String> {}
