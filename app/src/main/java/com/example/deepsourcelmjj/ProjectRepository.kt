package com.example.deepsourcelmjj

object ProjectRepository {
    val projects = listOf(
        ProjectItem(
            projectId = "project-portal-itau",
            title = "Portal Cliente Itaú",
            client = "Itaú Corpbanca",
            teamCount = 6,
            totalHours = "184 hrs",
            lastUpdate = "Ayer",
            status = "En progreso",
            assignedUsers = listOf(
                ProjectAssignment("user-marina-soto", UserRole.LEADER),
                ProjectAssignment("user-carlos-lopez", UserRole.DEV),
                ProjectAssignment("user-juan-perez", UserRole.DEV)
            )
        ),
        ProjectItem(
            projectId = "project-falabella",
            title = "Rediseño App Falabella",
            client = "Falabella Retail",
            teamCount = 4,
            totalHours = "92 hrs",
            lastUpdate = "Hace 2 días",
            status = "En progreso",
            assignedUsers = listOf(
                ProjectAssignment("user-marina-soto", UserRole.LEADER),
                ProjectAssignment("user-carlos-lopez", UserRole.DEV)
            )
        ),
        ProjectItem(
            projectId = "project-bci",
            title = "Core Bancario BCI",
            client = "Banco BCI",
            teamCount = 8,
            totalHours = "310 hrs",
            lastUpdate = "Hace 3 días",
            status = "En revisión",
            assignedUsers = listOf(
                ProjectAssignment("user-marina-soto", UserRole.LEADER),
                ProjectAssignment("user-juan-perez", UserRole.DEV)
            )
        ),
        ProjectItem(
            projectId = "project-webpay",
            title = "Integración Webpay",
            client = "Transbank",
            teamCount = 2,
            totalHours = "45 hrs",
            lastUpdate = "05 Oct",
            status = "Finalizado",
            assignedUsers = listOf(
                ProjectAssignment("user-marina-soto", UserRole.LEADER)
            ),
            isCompleted = true
        )
    )

    fun getProjectsForUser(userId: String): List<ProjectItem> {
        return projects.filter { project -> project.assignedUsers.any { assignment -> assignment.userId == userId } }
    }

    fun getProjectById(projectId: String): ProjectItem? {
        return projects.firstOrNull { project -> project.projectId == projectId }
    }

    fun getUserRoleInProject(projectId: String, userId: String): UserRole? {
        return getProjectById(projectId)
            ?.assignedUsers
            ?.firstOrNull { assignment -> assignment.userId == userId }
            ?.profile
    }
}
