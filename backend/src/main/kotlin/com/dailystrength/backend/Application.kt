package com.dailystrength.backend

import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.callloging.CallLogging
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import kotlinx.serialization.json.Json

/**
 * AI Coach proxy. Holds the LLM credentials server-side (ANTHROPIC_API_KEY) so the mobile app never
 * ships a provider key. Exposes POST /coach/plan matching the app's contract.
 *
 * Run: ANTHROPIC_API_KEY=sk-ant-... ./gradlew run
 */
fun main() {
    val port = System.getenv("PORT")?.toIntOrNull() ?: 8080
    embeddedServer(Netty, port = port) { module() }.start(wait = true)
}

fun Application.module(coachService: CoachService = CoachService()) {
    install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true; explicitNulls = false }) }
    install(CallLogging)
    install(StatusPages) {
        exception<Throwable> { call, cause ->
            call.application.log.error("Unhandled error", cause)
            call.respond(HttpStatusCode.InternalServerError, ErrorResponse("internal_error"))
        }
    }

    routing {
        get("/health") { call.respond(mapOf("status" to "ok")) }

        post("/coach/plan") {
            val request = call.receive<CoachRequest>()
            val plan = coachService.generatePlan(request)
            if (plan == null) {
                // The app falls back to its deterministic generator on any non-2xx / failure.
                call.respond(HttpStatusCode.UnprocessableEntity, ErrorResponse("no_valid_plan"))
            } else {
                call.respond(plan)
            }
        }
    }
}

@kotlinx.serialization.Serializable
data class ErrorResponse(val error: String)
