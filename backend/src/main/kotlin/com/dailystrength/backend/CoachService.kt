package com.dailystrength.backend

import com.anthropic.client.AnthropicClient
import com.anthropic.client.okhttp.AnthropicOkHttpClient
import com.anthropic.models.messages.MessageCreateParams
import com.anthropic.models.messages.Model
import kotlinx.serialization.json.Json

/**
 * Calls Claude to produce a workout plan, constrained to the allowed exercise ids supplied by the
 * app. The model only *selects* from the internal library — it never invents exercises. As a
 * defense in depth, the service also drops any exercise id the model returns that isn't in the
 * allow-list before sending the plan back; the Android client validates again on receipt.
 *
 * Model: claude-opus-4-8 (Anthropic's most capable Opus-tier model). For a high-volume, cost-
 * sensitive deployment you may switch to Model.CLAUDE_SONNET_4_6.
 */
class CoachService(
    private val client: AnthropicClient = AnthropicOkHttpClient.fromEnv(), // reads ANTHROPIC_API_KEY
    private val json: Json = Json { ignoreUnknownKeys = true; explicitNulls = false },
) {

    fun generatePlan(request: CoachRequest): WorkoutPlan? {
        val userMessage = buildUserMessage(request)

        val params = MessageCreateParams.builder()
            .model(Model.CLAUDE_OPUS_4_8)
            .maxTokens(2000L)
            .system(request.systemPrompt)
            .addUserMessage(userMessage)
            .build()

        val response = client.messages().create(params)

        val text = response.content().stream()
            .flatMap { it.text().stream() }
            .map { it.text() }
            .findFirst()
            .orElse(null) ?: return null

        val plan = parsePlan(text) ?: return null
        return repair(plan, request.allowedExerciseIds.toSet())
    }

    private fun buildUserMessage(request: CoachRequest): String {
        val payload = json.encodeToString(
            CoachRequest.serializer(),
            // The system prompt is sent separately; keep the user turn to pure context.
            request.copy(systemPrompt = ""),
        )
        return buildString {
            appendLine("Generate today's workout. Return ONLY the JSON object, no prose, no code fences.")
            appendLine("You may use ONLY these exercise ids: ${request.allowedExerciseIds.joinToString(", ")}")
            appendLine()
            appendLine("Context:")
            append(payload)
        }
    }

    private fun parsePlan(text: String): WorkoutPlan? {
        val cleaned = text.trim()
            .removePrefix("```json").removePrefix("```").removeSuffix("```")
            .trim()
        // Extract the outermost JSON object in case the model adds stray text.
        val start = cleaned.indexOf('{')
        val end = cleaned.lastIndexOf('}')
        if (start < 0 || end <= start) return null
        return runCatching { json.decodeFromString(WorkoutPlan.serializer(), cleaned.substring(start, end + 1)) }
            .getOrNull()
    }

    /** Drops hallucinated ids and clamps duration to the 10..20 minute contract. */
    private fun repair(plan: WorkoutPlan, allowed: Set<String>): WorkoutPlan? {
        val exercises = plan.exercises.filter { it.exerciseId in allowed }
        if (exercises.isEmpty()) return null
        return plan.copy(
            duration = plan.duration.coerceIn(10, 20),
            exercises = exercises,
        )
    }
}
