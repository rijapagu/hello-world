package com.dailystrength.data.remote

import com.dailystrength.data.remote.dto.CoachRequestDto
import com.dailystrength.data.remote.dto.ContextDto
import com.dailystrength.data.remote.dto.ProfileDto
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import io.ktor.utils.io.ByteReadChannel
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class AiCoachApiTest {

    private fun client(handler: MockEngine): HttpClient = HttpClient(handler) {
        install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true; explicitNulls = false }) }
    }

    private val request = CoachRequestDto(
        systemPrompt = "sys",
        profile = ProfileDto(30, 175, 75.0, "INTERMEDIATE", listOf("PULL_UP_BAR")),
        context = ContextDto("NONE", "PULL", emptyList(), emptyMap(), null),
        allowedExerciseIds = listOf("assisted_pullup"),
    )

    @Test
    fun `parses a well-formed workout plan response`() = runTest {
        val engine = MockEngine {
            respond(
                content = ByteReadChannel(
                    """
                    {"workout_type":"pull","duration":15,
                     "exercises":[{"exercise_id":"assisted_pullup","sets":3,"target_reps":5}]}
                    """.trimIndent(),
                ),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json"),
            )
        }
        val api = AiCoachApi(client(engine), "https://example.test/")

        val plan = api.generatePlan(request)

        assertEquals("pull", plan.workoutType)
        assertEquals(15, plan.duration)
        assertEquals(1, plan.exercises.size)
        assertEquals("assisted_pullup", plan.exercises.first().exerciseId)
        assertEquals(3, plan.exercises.first().sets)
        assertEquals(5, plan.exercises.first().targetReps)
    }

    @Test
    fun `posts to the coach plan endpoint`() = runTest {
        lateinit var calledUrl: String
        val engine = MockEngine { req ->
            calledUrl = req.url.toString()
            respond(
                content = ByteReadChannel("""{"workout_type":"core","duration":12,"exercises":[{"exercise_id":"plank","sets":3,"target_hold_seconds":40}]}"""),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json"),
            )
        }
        val api = AiCoachApi(client(engine), "https://example.test/")

        api.generatePlan(request)

        assertEquals("https://example.test/coach/plan", calledUrl)
    }
}
