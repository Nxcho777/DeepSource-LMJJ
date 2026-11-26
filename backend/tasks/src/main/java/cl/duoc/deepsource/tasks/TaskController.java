package cl.duoc.deepsource.tasks;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
class TaskController {
    private static final Logger LOG = LoggerFactory.getLogger(TaskController.class);
    private final TaskRepository repository;
    private final ProjectsClient projects;

    TaskController(TaskRepository repository, ProjectsClient projects) {
        this.repository = repository;
        this.projects = projects;
    }

    @GetMapping("/api/v1/tasks")
    List<Dto> listAll(@RequestParam(defaultValue = "false") boolean includeArchived) {
        LOG.info("GET /api/v1/tasks");
        return repository.findAll(Sort.by("id")).stream().filter(row -> includeArchived || !row.archived)
                .map(TaskController::dto).toList();
    }

    @GetMapping("/api/v1/projects/{projectId}/tasks")
    List<Dto> list(@PathVariable String projectId, @RequestParam(defaultValue = "false") boolean includeArchived) {
        LOG.info("GET /api/v1/projects/{projectId}/tasks");
        return repository.findAll(Sort.by("id")).stream()
                .filter(row -> row.projectId.equals(projectId) && (includeArchived || !row.archived))
                .map(TaskController::dto).toList();
    }

    @GetMapping("/api/v1/projects/{projectId}/tasks/{id}")
    Dto get(@PathVariable String projectId, @PathVariable String id) {
        return dto(find(projectId, id));
    }

    @PostMapping("/api/v1/projects/{projectId}/tasks")
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    Dto create(@PathVariable String projectId, @RequestBody Dto request) {
        validate(request);
        if (repository.existsById(request.id())) throw error(HttpStatus.CONFLICT, "ID already exists");
        projects.requireActive(projectId);
        var row = new TaskRow();
        row.id = request.id();
        row.projectId = projectId;
        apply(row, request);
        LOG.info("POST /api/v1/projects/{projectId}/tasks");
        return dto(repository.save(row));
    }

    @PutMapping("/api/v1/projects/{projectId}/tasks/{id}")
    @Transactional
    Dto update(@PathVariable String projectId, @PathVariable String id, @RequestBody Dto request) {
        validate(request);
        if (!id.equals(request.id())) throw error(HttpStatus.BAD_REQUEST, "ID does not match path");
        var row = find(projectId, id);
        if (row.archived) throw error(HttpStatus.CONFLICT, "Record is archived");
        projects.requireActive(projectId);
        apply(row, request);
        LOG.info("PUT /api/v1/projects/{projectId}/tasks/{id}");
        return dto(row);
    }

    @DeleteMapping("/api/v1/projects/{projectId}/tasks/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Transactional
    void delete(@PathVariable String projectId, @PathVariable String id) {
        find(projectId, id).archived = true;
        LOG.info("DELETE /api/v1/projects/{projectId}/tasks/{id}");
    }

    private TaskRow find(String projectId, String id) {
        return repository.findById(id)
                .filter(row -> row.projectId.equals(projectId))
                .orElseThrow(() -> error(HttpStatus.NOT_FOUND, "Record not found"));
    }

    private static void validate(Dto dto) {
        if (dto.id() == null || !dto.id().matches("[a-zA-Z0-9-]{1,80}")
                || !validText(dto.name(), 255) || dto.description() == null || dto.description().length() > 4000
                || !validMinutes(dto.estimatedMinutes())) {
            throw error(HttpStatus.BAD_REQUEST, "Invalid catalog fields");
        }
    }

    private static boolean validText(String value, int max) {
        return value != null && !value.isBlank() && value.length() <= max;
    }

    private static boolean validMinutes(Integer value) {
        return value != null && value > 0 && value <= 6_000_000;
    }

    private static void apply(TaskRow row, Dto request) {
        row.name = request.name().trim();
        row.description = request.description().trim();
        row.estimatedMinutes = request.estimatedMinutes();
    }

    private static Dto dto(TaskRow row) {
        return new Dto(row.id, row.projectId, row.name, row.description, row.estimatedMinutes, row.archived);
    }

    private static ResponseStatusException error(HttpStatus status, String reason) {
        return new ResponseStatusException(status, reason);
    }

    record Dto(String id, String projectId, String name, String description, Integer estimatedMinutes, Boolean archived) {}
}
