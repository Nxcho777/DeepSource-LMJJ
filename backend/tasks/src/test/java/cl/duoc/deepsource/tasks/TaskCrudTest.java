package cl.duoc.deepsource.tasks;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:tasks-test;DB_CLOSE_DELAY=-1")
@AutoConfigureMockMvc
class TaskCrudTest {
    private static final String PATH = "/api/v1/projects/test-parent/tasks";
    private static final String DATA = """
        {"id":"ev4-test","projectId":"test-parent","name":"Atlas","description":"Prueba","estimatedMinutes":120}
        """;

    @Autowired MockMvc mvc;
    @MockitoBean ProjectsClient projects;

    @Test
    void createReadUpdateDeleteAndRetainArchivedRow() throws Exception {
        mvc.perform(post(PATH).contentType(MediaType.APPLICATION_JSON).content(DATA))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.id").value("ev4-test"));
        mvc.perform(post(PATH).contentType(MediaType.APPLICATION_JSON).content(DATA))
                .andExpect(status().isConflict());
        mvc.perform(get(PATH)).andExpect(status().isOk()).andExpect(jsonPath("$[0].name").value("Atlas"));
        mvc.perform(get(PATH + "/ev4-test")).andExpect(status().isOk());
        mvc.perform(put(PATH + "/ev4-test").contentType(MediaType.APPLICATION_JSON).content(DATA.replace("Atlas", "Boreal")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.name").value("Boreal"));
        mvc.perform(delete(PATH + "/ev4-test")).andExpect(status().isNoContent());
        mvc.perform(get(PATH)).andExpect(status().isOk()).andExpect(jsonPath("$").isEmpty());
        mvc.perform(get(PATH).param("includeArchived", "true")).andExpect(jsonPath("$[0].archived").value(true));
        mvc.perform(put(PATH + "/ev4-test").contentType(MediaType.APPLICATION_JSON).content(DATA))
                .andExpect(status().isConflict());
        mvc.perform(get("/api/v1/projects/other/tasks/ev4-test")).andExpect(status().isNotFound());
        mvc.perform(get("/api/v1/tasks").param("includeArchived", "true"))
                .andExpect(jsonPath("$[0].projectId").value("test-parent"));
    }

    @Test
    void rejectsInvalidAndMissingRecords() throws Exception {
        mvc.perform(post(PATH).contentType(MediaType.APPLICATION_JSON).content("{}")).andExpect(status().isBadRequest());
        mvc.perform(get(PATH + "/missing")).andExpect(status().isNotFound());
    }
}
