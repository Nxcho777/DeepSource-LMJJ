package com.example.deepsourcelmjj

// Perfiles definidos en el modelo de datos entregado por DeepSource.
enum class UserRole {
    LEADER,
    DEV
}

data class AppUser(
    val userId: String,
    val userName: String,
    val userAvatar: String? = null,
    val profile: UserRole,
    val email: String
)

data class ProjectAssignment(
    val userId: String,
    val profile: UserRole
)

data class ProjectItem(
    val projectId: String,
    val title: String,
    val client: String,
    val teamCount: Int,
    val totalHours: String,
    val lastUpdate: String,
    val status: String,
    val assignedUsers: List<ProjectAssignment>,
    val isCompleted: Boolean = false
)
