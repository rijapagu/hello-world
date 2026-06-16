package com.dailystrength.data.auth

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/** Minimal authenticated user surfaced to the domain. */
data class GoogleUser(val id: String, val name: String?)

/**
 * Google sign-in via the modern Credential Manager API. Sign-in is OPTIONAL by design: any failure
 * returns null and onboarding proceeds offline (Never Zero — never block the user on an account).
 *
 * Requires a real OAuth Web client id in `R.string.default_web_client_id`.
 */
@Singleton
class GoogleAuthClient @Inject constructor(
    @ApplicationContext private val appContext: Context,
) {
    private val credentialManager = CredentialManager.create(appContext)

    /** @param activityContext must be an Activity context to display the account picker. */
    suspend fun signIn(activityContext: Context): GoogleUser? {
        val webClientId = appContext.getString(com.dailystrength.R.string.default_web_client_id)
        if (webClientId.isBlank() || webClientId.startsWith("REPLACE_")) return null

        val googleIdOption = GetGoogleIdOption.Builder()
            .setServerClientId(webClientId)
            .setFilterByAuthorizedAccounts(false)
            .setAutoSelectEnabled(true)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        return runCatching {
            val result = credentialManager.getCredential(activityContext, request)
            val credential = result.credential
            if (credential is CustomCredential &&
                credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
            ) {
                val token = GoogleIdTokenCredential.createFrom(credential.data)
                GoogleUser(id = token.id, name = token.displayName)
            } else {
                null
            }
        }.getOrNull()
    }
}
