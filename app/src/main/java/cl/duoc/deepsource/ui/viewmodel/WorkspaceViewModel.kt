package cl.duoc.deepsource.ui.viewmodel

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import cl.duoc.deepsource.data.repository.DeepSourceRepository
import cl.duoc.deepsource.data.repository.CatalogRepository
import cl.duoc.deepsource.data.local.DeepSourceDatabase
import cl.duoc.deepsource.data.remote.HolidayRepository
import cl.duoc.deepsource.data.remote.NetworkClients
import cl.duoc.deepsource.domain.model.ProjectTask
import cl.duoc.deepsource.domain.model.Project
import cl.duoc.deepsource.domain.model.TimeEntry
import cl.duoc.deepsource.domain.model.User
import cl.duoc.deepsource.domain.model.UserRole
import cl.duoc.deepsource.notification.AndroidTimeEntryNotifier
import cl.duoc.deepsource.notification.TimeEntryNotifier
import cl.duoc.deepsource.widget.DeepSourceWidgetProvider
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import cl.duoc.deepsource.domain.model.RegistrationDates
import java.time.LocalDate
import java.io.IOException
import java.util.UUID
import retrofit2.HttpException
data class WorkspaceUiState(
    val projects: List<Project> = emptyList(),
    val currentUserId: String = DeepSourceRepository.DEFAULT_USER_ID,
    val users: List<User> = emptyList(),
    val timeEntries: List<TimeEntry> = emptyList(),
    val tasks: List<ProjectTask> = emptyList(),
    val selectedProjectId: String = "project-1",
    val isLoading: Boolean = true,
    val error: String? = null
) {
    val selectedProject: Project? get() = projects.firstOrNull { it.id == selectedProjectId && !it.archived }
        ?: projects.firstOrNull { !it.archived }
    val currentUser: User? get() = users.firstOrNull { it.id == currentUserId }
    val isAdmin: Boolean get() = currentUser?.role == UserRole.ADMIN
    val profileProjects: List<Project> get() = projects.filter { it.isCompleted && !it.archived }.take(3)
}

data class RegisterTimeUiState(
    val userId: String = DeepSourceRepository.DEFAULT_USER_ID,
    val date: String = RegistrationDates().today.toString(),
    val hours: String = "00",
    val minutes: String = "00",
    val task: String = "",
    val selectedTaskId: String? = null,
    val description: String = "",
    val isSaving: Boolean = false,
    val message: String? = null,
    val isError: Boolean = false,
    val receipt: TimeReceipt? = null,
    val durationTouched: Boolean = false,
    val taskTouched: Boolean = false
) {
    val durationMinutes: Int get() = (hours.toIntOrNull() ?: 0) * 60 + (minutes.toIntOrNull() ?: 0)
    val isValid: Boolean get() = hours.toIntOrNull()?.let { it in 0..23 } == true &&
            minutes.toIntOrNull()?.let { it in 0..59 } == true && durationMinutes > 0 && selectedTaskId != null
    val durationError: String? get() = if (durationTouched && durationMinutes == 0) "Debes ingresar una duración mayor a cero." else null
    val taskError: String? get() = if (taskTouched && task.isBlank()) "Debes ingresar el nombre de la tarea." else null
}

data class TimeReceipt(
    val entryId: String,
    val projectName: String,
    val taskName: String,
    val date: String,
    val durationMinutes: Int
) {
    val durationLabel: String
        get() = "${durationMinutes / 60} h ${(durationMinutes % 60).toString().padStart(2, '0')} min"
}

data class CreateProjectUiState(
    val isSaving: Boolean = false,
    val error: String? = null,
    val createdProjectId: String? = null
)

class WorkspaceViewModel(
    private val repository: DeepSourceRepository,
    private val savedStateHandle: SavedStateHandle,
    private val refreshWidget: () -> Unit,
    private val notifier: TimeEntryNotifier,
    private val context: Context,
    private val catalog: CatalogRepository? = null,
    private val holidays: HolidayRepository? = null
) : ViewModel() {
    companion object {
        fun factory(context: Context) = viewModelFactory {
            initializer {
                WorkspaceViewModel(
                    DeepSourceRepository.getInstance(context),
                    createSavedStateHandle(),
                    { DeepSourceWidgetProvider.requestRefresh(context) },
                    AndroidTimeEntryNotifier(context),
                    context.applicationContext,
                    NetworkClients.projects?.let { projects ->
                        NetworkClients.tasks?.let { tasks ->
                            CatalogRepository(DeepSourceDatabase.getInstance(context), projects, tasks)
                        }
                    },
                    HolidayRepository(NetworkClients.holidays)
                )
            }
        }
    }

    private val _uiState = MutableStateFlow(WorkspaceUiState(selectedProjectId = savedStateHandle["projectId"] ?: "project-1", currentUserId = savedStateHandle["currentUserId"] ?: DeepSourceRepository.DEFAULT_USER_ID))
    val uiState: StateFlow<WorkspaceUiState> = _uiState.asStateFlow()

    private val _registerState = MutableStateFlow(RegisterTimeUiState(
        userId = savedStateHandle["userId"] ?: DeepSourceRepository.DEFAULT_USER_ID,
        date = savedStateHandle["date"] ?: RegistrationDates().today.toString(),
        hours = savedStateHandle["hours"] ?: "00",
        minutes = savedStateHandle["minutes"] ?: "00",
        task = savedStateHandle["task"] ?: "",
        selectedTaskId = savedStateHandle["selectedTaskId"],
        description = savedStateHandle["description"] ?: ""
    ))
    val registerState: StateFlow<RegisterTimeUiState> = _registerState.asStateFlow()

    private val _createProjectState = MutableStateFlow(CreateProjectUiState())
    val createProjectState: StateFlow<CreateProjectUiState> = _createProjectState.asStateFlow()

    private val _catalogError = MutableStateFlow<String?>(null)
    val catalogError = _catalogError.asStateFlow()
    private val _holidayDates = MutableStateFlow<Map<LocalDate, String>>(emptyMap())
    val holidayDates = _holidayDates.asStateFlow()
    private val _taskState = MutableStateFlow(CreateProjectUiState())
    val taskState = _taskState.asStateFlow()
    private var newProjectId = UUID.randomUUID().toString()
    private var newTaskId = UUID.randomUUID().toString()

    fun refreshCatalog() {
        val remote = catalog ?: return
        viewModelScope.launch {
            try {
                remote.refresh()
                _catalogError.value = null
                runCatching { refreshWidget() }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                _catalogError.value = catalogErrorMessage(error)
            }
        }
    }

    fun loadHolidays(years: IntRange) {
        val repository = holidays ?: return
        viewModelScope.launch {
            for (year in years) {
                try {
                    val dates = repository.forYear(year)
                    _holidayDates.update { it + dates }
                } catch (error: CancellationException) {
                    throw error
                } catch (_: Exception) {
                    // The date picker remains available without the advisory.
                }
            }
        }
    }

    fun resetCreateProjectState() {
        if (!_createProjectState.value.isSaving) {
            _createProjectState.value = CreateProjectUiState()
            newProjectId = UUID.randomUUID().toString()
        }
    }

    fun createProject(name: String, client: String, description: String, totalHours: Int, iconIndex: Int,
        existing: Project? = null) {
        if (_createProjectState.value.isSaving) return
        _createProjectState.value = CreateProjectUiState(isSaving = true)
        viewModelScope.launch {
            try {
                val project = requireCatalog().saveProject(_uiState.value.currentUserId, existing,
                    name, client, description, totalHours, iconIndex, newProjectId)
                _catalogError.value = null
                runCatching { refreshWidget() }
                _createProjectState.value = CreateProjectUiState(createdProjectId = project.id)
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                _createProjectState.value = CreateProjectUiState(error = catalogErrorMessage(error))
            }
        }
    }

    fun finishProject(projectId: String) {
        viewModelScope.launch {
            try {
                requireCatalog().finishProject(_uiState.value.currentUserId, projectId)
                runCatching { refreshWidget() }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                _catalogError.value = catalogErrorMessage(error)
            }
        }
    }

    init {
        refreshCatalog()
        viewModelScope.launch {
            try {
                combine(
                    combine(repository.observeProjects(), repository.observeUsers(), repository.observeTimeEntries()) { projects, users, entries ->
                        Triple(projects, users, entries)
                    },
                    repository.observeTasks()
                ) { original, tasks -> original to tasks }.collect { (original, tasks) ->
                    val (projects, users, entries) = original
                    _uiState.update { it.copy(projects = projects, users = users, timeEntries = entries, tasks = tasks, isLoading = false, error = null) }
                }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                _uiState.update { it.copy(isLoading = false, error = error.message ?: "No se pudieron cargar los datos.") }
            }
        }
    }

    fun logout() {
        // Eliminar la identidad activa y los datos temporales del formulario.
        savedStateHandle.remove<String>("currentUserId")
        savedStateHandle.remove<String>("userId")
        listOf("date", "hours", "minutes", "task", "description", "selectedTaskId").forEach { savedStateHandle.remove<String>(it) }
        _uiState.update { it.copy(currentUserId = "") }
        context.getSharedPreferences("deepsource_widget_timer", Context.MODE_PRIVATE).edit().remove("activeUserId").apply()
        _registerState.value = RegisterTimeUiState(userId = "")
    }

    fun setCurrentUserByEmail(email: String) {
        val user = _uiState.value.users.firstOrNull { it.email.equals(email.trim(), ignoreCase = true) }
        val userId = user?.id ?: if (email.equals("admin@deepsource.cl", true)) "user-admin" else DeepSourceRepository.DEFAULT_USER_ID
        context.getSharedPreferences("deepsource_widget_timer", Context.MODE_PRIVATE).edit().putString("activeUserId",userId).apply()
        savedStateHandle["currentUserId"] = userId
        savedStateHandle["userId"] = userId
        _uiState.update { it.copy(currentUserId = userId) }
        _registerState.update { it.copy(userId = userId) }
    }

    fun saveProfileName(name: String, onComplete: (String?) -> Unit) {
        val userId = _uiState.value.currentUserId
        viewModelScope.launch {
            val errorMessage = try {
                repository.renameUser(userId, name)
                null
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                error.message ?: "No se pudo guardar el perfil."
            }
            onComplete(errorMessage)
        }
    }

    fun selectProject(id: String) {
        savedStateHandle.remove<String>("selectedTaskId")
        savedStateHandle.remove<String>("task")
        _registerState.update { it.copy(selectedTaskId = null, task = "") }
        savedStateHandle["projectId"] = id
        _uiState.update { it.copy(selectedProjectId = id) }
    }

    fun selectUser(id: String) {
        savedStateHandle["userId"] = id
        _registerState.update { it.copy(userId = id) }
    }

    fun setDate(value: String) = changeForm("date", value) { it.copy(date = value) }
    fun setHours(value: String) = changeForm("hours", value) { it.copy(hours = value) }
    fun setMinutes(value: String) = changeForm("minutes", value) { it.copy(minutes = value) }
    fun setTask(value: String) = changeForm("task", value) { it.copy(task = value) }
    fun setDescription(value: String) = changeForm("description", value) { it.copy(description = value) }
    fun selectTask(task: ProjectTask) {
        savedStateHandle["selectedTaskId"] = task.id
        savedStateHandle["task"] = task.name
        _registerState.update { it.copy(selectedTaskId = task.id, task = task.name, taskTouched = false) }
    }

    fun resetTaskState() {
        if (!_taskState.value.isSaving) {
            _taskState.value = CreateProjectUiState()
            newTaskId = UUID.randomUUID().toString()
        }
    }

    fun createTask(name: String, description: String, estimatedHours: Int, existing: ProjectTask? = null) {
        if (_taskState.value.isSaving) return
        val projectId = _uiState.value.selectedProject?.id ?: return
        _taskState.value = CreateProjectUiState(isSaving = true)
        viewModelScope.launch {
            try {
                val task = requireCatalog().saveTask(_uiState.value.currentUserId, existing?.projectId ?: projectId,
                    existing, name, description, estimatedHours, newTaskId)
                if (_uiState.value.selectedProject?.id == task.projectId) selectTask(task)
                _taskState.value = CreateProjectUiState(createdProjectId = task.id)
                runCatching { refreshWidget() }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                _taskState.value = CreateProjectUiState(error = catalogErrorMessage(error))
            }
        }
    }

    fun deleteProject(project: Project) = mutateCatalog {
        deleteProject(_uiState.value.currentUserId, project.id)
        if (_uiState.value.selectedProjectId == project.id) selectProject("")
    }

    fun deleteTask(task: ProjectTask) = mutateCatalog {
        deleteTask(_uiState.value.currentUserId, task)
        if (_registerState.value.selectedTaskId == task.id) selectProject(task.projectId)
    }

    private fun mutateCatalog(operation: suspend CatalogRepository.() -> Unit) {
        viewModelScope.launch {
            try {
                requireCatalog().operation()
                _catalogError.value = null
                runCatching { refreshWidget() }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                _catalogError.value = catalogErrorMessage(error)
            }
        }
    }

    private fun requireCatalog(): CatalogRepository =
        checkNotNull(catalog) { "Configura las URL de los servicios para modificar el catálogo." }

    fun markDurationTouched() {
        _registerState.update { it.copy(durationTouched = true) }
    }

    fun markTaskTouched() {
        _registerState.update { it.copy(taskTouched = true) }
    }

    private fun changeForm(key: String, value: String, update: (RegisterTimeUiState) -> RegisterTimeUiState) {
        savedStateHandle[key] = value
        _registerState.update(update)
    }

    fun saveTimeEntry() {
        val form = _registerState.value
        val workspace = _uiState.value
        val selectedDate = runCatching { LocalDate.parse(form.date) }.getOrNull()
        val validDate = selectedDate != null && RegistrationDates().allows(selectedDate)

        if (!validDate) {
            _registerState.update {
                it.copy(
                    message = "Solo puedes registrar horas de lunes a viernes, durante la semana anterior o la semana actual hasta hoy.",
                    isError = true
                )
            }
            return
        }

        if (form.isSaving || form.receipt != null || !form.isValid || workspace.isLoading) return
        val project = workspace.selectedProject ?: return
        if (workspace.tasks.none { it.id == form.selectedTaskId && it.projectId == project.id && !it.archived }) {
            _registerState.update { it.copy(message = "Selecciona una tarea disponible.", isError = true) }
            return
        }
        val user = workspace.users.firstOrNull { it.id == form.userId } ?: return
        _registerState.update { it.copy(isSaving = true, message = null, isError = false) }
        viewModelScope.launch {
            try {
                val entry = repository.addTimeEntry(project.id, user.id, form.durationMinutes, form.date, form.description.trim(), form.selectedTaskId)
                runCatching { refreshWidget() }
                runCatching { notifier.notifySaved(entry, project.name) }
                savedStateHandle["hours"] = "00"
                savedStateHandle["minutes"] = "00"
                savedStateHandle["task"] = ""
                savedStateHandle.remove<String>("selectedTaskId")
                savedStateHandle["description"] = ""
                _registerState.update {
                    it.copy(hours = "00", minutes = "00", task = "", selectedTaskId = null, description = "", isSaving = false,
                        message = null, isError = false,
                        durationTouched = false, taskTouched = false,
                        receipt = TimeReceipt(entry.id, project.name, entry.note, entry.date, entry.durationMinutes))
                }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                _registerState.update { it.copy(isSaving = false, message = error.message ?: "No se pudo guardar el registro.", isError = true) }
            }
        }
    }

    fun acceptReceipt() {
        _registerState.update { it.copy(receipt = null) }
    }
}

internal fun catalogErrorMessage(error: Exception): String = when (error) {
    is IOException -> "Sin conexión al catálogo. Puedes seguir consultando y registrando horas."
    is HttpException -> when (error.code()) {
        400 -> "Revisa los datos del formulario."
        404 -> "El elemento ya no está disponible. Actualiza el catálogo."
        409 -> "El catálogo cambió. Actualízalo y vuelve a intentar."
        else -> "El catálogo no está disponible. Vuelve a intentar."
    }
    else -> error.message ?: "No se pudo realizar la operación."
}
