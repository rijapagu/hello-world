package com.dailystrength.data.remote

import com.dailystrength.data.remote.dto.CoachRequestDto
import com.dailystrength.data.remote.dto.WorkoutPlanDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

/**
 * Thin Ktor wrapper over the AI Coach backend proxy. Throws on transport/parse errors; the repository
 * is responsible for catching and falling back to the deterministic generator. Constructed in
 * [com.dailystrength.di.NetworkModule] so the base URL can be injected with a qualifier.
 */
class AiCoachApi(
    private val client: HttpClient,
    private val baseUrl: String,
) {
    suspend fun generatePlan(request: CoachRequestDto): WorkoutPlanDto =
        client.post("${baseUrl.trimEnd('/')}/coach/plan") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
}
