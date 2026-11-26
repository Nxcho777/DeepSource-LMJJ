package cl.duoc.deepsource.tasks;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Version;

@Entity
class TaskRow {
    @Id
    @Column(length = 80)
    String id;
    @Column(nullable = false)
    String name;
    @Column(nullable = false, length = 4000)
    String description;
    @Column(nullable = false, length = 80, updatable = false)
    String projectId;
    int estimatedMinutes;
    boolean archived;
    @Version
    Long revision;

    protected TaskRow() {}
}
