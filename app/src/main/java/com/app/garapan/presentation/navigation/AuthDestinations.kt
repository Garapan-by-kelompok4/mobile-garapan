package com.app.garapan.presentation.navigation

import com.app.garapan.domain.model.Role
import com.app.garapan.domain.model.User

fun User.authDestination(): String =
    if (requiresProfileSetup()) Routes.setupRoute(role.setupRouteParam()) else Routes.MAIN

fun User.requiresProfileSetup(): Boolean =
    when (role) {
        Role.MAHASISWA -> mahasiswa == null || mahasiswa.university.isBlank() || mahasiswa.bio.isBlank()
        Role.KLIEN -> klien == null || klien.bio.isBlank()
        Role.ADMIN -> false
    }

private fun Role.setupRouteParam(): String =
    when (this) {
        Role.MAHASISWA -> "student"
        Role.KLIEN -> "client"
        Role.ADMIN -> "admin"
    }
