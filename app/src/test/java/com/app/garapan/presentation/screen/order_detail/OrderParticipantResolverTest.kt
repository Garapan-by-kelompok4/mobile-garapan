package com.app.garapan.presentation.screen.order_detail

import com.app.garapan.domain.model.Role
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class OrderParticipantResolverTest {

    @Test
    fun treatsClientRoleAsBuyerWhenApiOmitsClientUserId() {
        val isBuyer = OrderParticipantResolver.isBuyer(
            currentUserId = "user-client",
            currentRole = Role.KLIEN,
            clientUserId = null,
            workerUserId = "user-worker"
        )

        assertTrue(isBuyer)
    }

    @Test
    fun keepsMahasiswaBuyerWhenClientUserIdMatchesCurrentUser() {
        val isBuyer = OrderParticipantResolver.isBuyer(
            currentUserId = "user-mahasiswa-buyer",
            currentRole = Role.MAHASISWA,
            clientUserId = "user-mahasiswa-buyer",
            workerUserId = "user-worker"
        )

        assertTrue(isBuyer)
    }

    @Test
    fun doesNotTreatWorkerAsBuyerWhenClientUserIdIsMissing() {
        val isBuyer = OrderParticipantResolver.isBuyer(
            currentUserId = "user-worker",
            currentRole = Role.MAHASISWA,
            clientUserId = null,
            workerUserId = "user-worker"
        )

        assertFalse(isBuyer)
    }
}
