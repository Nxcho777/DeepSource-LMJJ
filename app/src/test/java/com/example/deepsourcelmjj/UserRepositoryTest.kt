package com.example.deepsourcelmjj

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class UserRepositoryTest {

    @Test
    fun validCredentialsReturnExpectedUser() {
        val user = UserRepository.authenticate(
            email = "marina.soto@deepsource.cl",
            password = "Marina2026"
        )

        assertEquals("Marina Soto", user?.userName)
        assertEquals(UserRole.LEADER, user?.profile)
    }

    @Test
    fun invalidCredentialsAreRejected() {
        val user = UserRepository.authenticate(
            email = "hola@gmail.com",
            password = "jaja"
        )

        assertNull(user)
    }

    @Test
    fun userOnlyReceivesAssignedProjects() {
        val projects = ProjectRepository.getProjectsForUser("user-carlos-lopez")

        assertTrue(projects.isNotEmpty())
        assertTrue(projects.all { project ->
            project.assignedUsers.any { assignment -> assignment.userId == "user-carlos-lopez" }
        })
    }

    @Test
    fun projectRoleIsTakenFromAssignedUsers() {
        val role = ProjectRepository.getUserRoleInProject(
            projectId = "project-portal-itau",
            userId = "user-carlos-lopez"
        )

        assertEquals(UserRole.DEV, role)
    }
}
