package cl.duoc.deepsource.tasks;

import java.net.http.HttpClient;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;

@Component
class ProjectsClient {
    private final RestClient client;

    ProjectsClient(@Value("${deepsource.projects-url}") String baseUrl) {
        var http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(2)).build();
        var factory = new JdkClientHttpRequestFactory(http);
        factory.setReadTimeout(Duration.ofSeconds(2));
        client = RestClient.builder().baseUrl(baseUrl).requestFactory(factory).build();
    }

    void requireActive(String projectId) {
        ProjectState state;
        try {
            state = client.get().uri("/api/v1/projects/{id}", projectId).retrieve().body(ProjectState.class);
        } catch (HttpClientErrorException.NotFound error) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found", error);
        } catch (RestClientException error) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Projects service unavailable", error);
        }
        if (state == null || state.archived() == null) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Project state unknown");
        }
        if (state.archived()) throw new ResponseStatusException(HttpStatus.CONFLICT, "Project is archived");
    }

    record ProjectState(Boolean archived) {}
}
