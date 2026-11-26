package cl.duoc.deepsource.projects;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
class ProjectController {
    private static final Logger LOG = LoggerFactory.getLogger(ProjectController.class);
    private final ProjectRepository repository;

    ProjectController(ProjectRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/api/v1/projects")
    List<Dto> list(@RequestParam(defaultValue = "false") boolean includeArchived) {
        LOG.info("GET /api/v1/projects");
        return repository.findAll(Sort.by("id")).stream()
                .filter(row -> (includeArchived || !row.archived))
                .map(ProjectController::dto).toList();
    }

    @GetMapping("/api/v1/projects/{id}")
    Dto get(@PathVariable String id) {
        return dto(find(id));
    }

    @PostMapping("/api/v1/projects")
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    Dto create(@RequestBody Dto request) {
        validate(request);
        if (repository.existsById(request.id())) throw error(HttpStatus.CONFLICT, "ID already exists");
        var row = new ProjectRow();
        row.id = request.id();
        apply(row, request);
        LOG.info("POST /api/v1/projects");
        return dto(repository.save(row));
    }

    @PutMapping("/api/v1/projects/{id}")
    @Transactional
    Dto update(@PathVariable String id, @RequestBody Dto request) {
        validate(request);
        if (!id.equals(request.id())) throw error(HttpStatus.BAD_REQUEST, "ID does not match path");
        var row = find(id);
        if (row.archived) throw error(HttpStatus.CONFLICT, "Record is archived");
        apply(row, request);
        LOG.info("PUT /api/v1/projects/{id}");
        return dto(row);
    }

    @DeleteMapping("/api/v1/projects/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Transactional
    void delete(@PathVariable String id) {
        find(id).archived = true;
        LOG.info("DELETE /api/v1/projects/{id}");
    }

    private ProjectRow find(String id) {
        return repository.findById(id)
                .orElseThrow(() -> error(HttpStatus.NOT_FOUND, "Record not found"));
    }

    private static void validate(Dto dto) {
        if (dto.id() == null || !dto.id().matches("[a-zA-Z0-9-]{1,80}")
                || !validText(dto.name(), 255) || dto.description() == null || dto.description().length() > 4000
                || !validText(dto.client(), 255) || !validMinutes(dto.totalMinutes())
                || dto.iconIndex() == null || dto.iconIndex() < 0 || dto.iconIndex() > 15
                || dto.isCompleted() == null) {
            throw error(HttpStatus.BAD_REQUEST, "Invalid catalog fields");
        }
    }

    private static boolean validText(String value, int max) {
        return value != null && !value.isBlank() && value.length() <= max;
    }

    private static boolean validMinutes(Integer value) {
        return value != null && value > 0 && value <= 6_000_000;
    }

    private static void apply(ProjectRow row, Dto request) {
        row.name = request.name().trim();
        row.description = request.description().trim();
        row.client = request.client().trim();
        row.totalMinutes = request.totalMinutes();
        row.iconIndex = request.iconIndex();
        row.isCompleted = row.isCompleted || request.isCompleted();
    }

    private static Dto dto(ProjectRow row) {
        return new Dto(row.id, row.name, row.description, row.client, row.totalMinutes,
                row.iconIndex, row.isCompleted, row.archived);
    }

    private static ResponseStatusException error(HttpStatus status, String reason) {
        return new ResponseStatusException(status, reason);
    }

    record Dto(String id, String name, String description, String client, Integer totalMinutes,
               Integer iconIndex, Boolean isCompleted, Boolean archived) {}
}
