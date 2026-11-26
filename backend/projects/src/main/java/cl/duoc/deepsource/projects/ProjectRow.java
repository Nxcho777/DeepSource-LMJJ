package cl.duoc.deepsource.projects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Version;

@Entity
class ProjectRow {
    @Id
    @Column(length = 80)
    String id;
    @Column(nullable = false)
    String name;
    @Column(nullable = false, length = 4000)
    String description;
    String client;
    int totalMinutes;
    int iconIndex;
    boolean isCompleted;
    boolean archived;
    @Version
    Long revision;

    protected ProjectRow() {}
}
