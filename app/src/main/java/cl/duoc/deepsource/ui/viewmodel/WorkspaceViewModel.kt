package cl.duoc.deepsource.ui.viewmodel

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import cl.duoc.deepsource.data.repository.DeepSourceRepository
import cl.duoc.deepsource.domain.model.Project
import cl.duoc.deepsource.domain.model.TimeEntry
import cl.duoc.deepsource.domain.model.User
import cl.duoc.deepsource.notification.AndroidTimeEntryNotifier
import cl.duoc.deepsource.notification.TimeEntryNotifier
import cl.duoc.deepsource.widget.DeepSourceWidgetProvider
import java.time.LocalDate
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class WorkspaceUiState(
    val projects: List<Project> = emptyList(),
    val users: List<User> = emptyList(),
    val timeEntries: List<TimeEntry> = emptyList(),
    val selectedProjectId: String = "project-1",
    val isLoading: Boolean = true,
    val error: String? = null
) {
    val selectedProject: Project? get() = projects.firstOrNull { it.id == selectedProjectId } ?: projects.firstOrNull()
    val currentUser: User? get() = users.firstOrNull { it.id == DeepSourceRepository.DEFAULT_USER_ID }
    val profileProjects: List<Project> get() = projects.take(3)
}

data class RegisterTimeUiState(
    val userId: String = DeepSourceRepository.DEFAULT_USER_ID,
    val date: String = LocalDate.now().toString(),
    val hours: String = "00",
    val minutes: String = "00",
    val task: String = "",
    val isSaving: Boolean = false,
    val message: String? = null,
    val isError: Boolean = false,
    val receipt: TimeReceipt? = null
) {
    val durationMinutes: Int get() = (hours.toIntOrNull() ?: 0) * 60 + (minutes.toIntOrNull() ?: 0)
    val isValid: Boolean get() = hours.toIntOrNull()?.let { it in 0..23 } == true &&
        minutes.toIntOrNull()?.let { it in 0..59 } == true && durationMinutes > 0 && task.isNotBlank()
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

class WorkspaceViewModel(
    private val repository: DeepSourceRepository,
    private val savedStateHandle: SavedStateHandle,
    private val refreshWidget: () -> Unit,
    private val notifier: TimeEntryNotifier
) : ViewModel() {
    companion object {
        fun factory(context: Context) = viewModelFactory {
            initializer {
                WorkspaceViewModel(
                    DeepSourceRepository.getInstance(context),
                    createSavedStateHandle(),
                    { DeepSourceWidgetProvider.requestRefresh(context) },
                    AndroidTimeEntryNotifier(context)
                )
            }
        }
    }

    private val _uiState = MutableStateFlow(WorkspaceUiState(selectedProjectId = savedStateHandle["projectId"] ?: "project-1"))
    val uiState: StateFlow<WorkspaceUiState> = _uiState.asStateFlow()

    private val _registerState = MutableStateFlow(RegisterTimeUiState(
        userId = savedStateHandle["userId"] ?: DeepSourceRepository.DEFAULT_USER_ID,
        date = savedStateHandle["date"] ?: LocalDate.now().toString(),
        hours = savedStateHandle["hours"] ?: "00",
        minutes = savedStateHandle["minutes"] ?: "00",
        task = savedStateHandle["task"] ?: ""
    ))
    val registerState: StateFlow<RegisterTimeUiState> = _registerState.asStateFlow()

    init {
        viewModelScope.launch {
            try {
                combine(repository.observeProjects(), repository.observeUsers(), repository.observeTimeEntries()) { projects, users, entries ->
                    Triple(projects, users, entries)
                }.collect { (projects, users, entries) ->
                    _uiState.update { it.copy(projects = projects, users = users, timeEntries = entries, isLoading = false, error = null) }
                }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                _uiState.update { it.copy(isLoading = false, error = error.message ?: "No se pudieron cargar los datos.") }
            }
        }
    }

    fun selectProject(id: String) {
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

    private fun changeForm(key: String, value: String, update: (RegisterTimeUiState) -> RegisterTimeUiState) {
        savedStateHandle[key] = value
        _registerState.update(update)
    }

    fun saveTimeEntry() {
        val form = _registerState.value
        val workspace = _uiState.value
        if (form.isSaving || form.receipt != null || !form.isValid || workspace.isLoading) return
        val project = workspace.selectedProject ?: return
        val user = workspace.users.firstOrNull { it.id == form.userId } ?: return
        _registerState.update { it.copy(isSaving = true, message = null, isError = false) }
        viewModelScope.launch {
            try {
                val entry = repository.addTimeEntry(project.id, user.id, form.durationMinutes, form.date, form.task.trim())
                runCatching { refreshWidget() }
                runCatching { notifier.notifySaved(entry, project.name) }
                savedStateHandle["hours"] = "00"
                savedStateHandle["minutes"] = "00"
                savedStateHandle["task"] = ""
                _registerState.update {
                    it.copy(hours = "00", minutes = "00", task = "", isSaving = false,
                        message = null, isError = false,
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
