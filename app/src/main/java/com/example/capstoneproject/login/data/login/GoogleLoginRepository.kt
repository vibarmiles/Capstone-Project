package com.example.capstoneproject.login.data.login

import android.content.Context
import android.content.Intent
import android.content.IntentSender
import com.example.capstoneproject.R
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.BeginSignInRequest.GoogleIdTokenRequestOptions
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await

enum class Provider {
    Google, Email, Phone
}

class GoogleLoginRepository(
    private val context: Context,
    private val oneTapClient: SignInClient
) {
    private val auth = FirebaseAuth.getInstance()

    suspend fun signIn(): IntentSender? {
        val result = try {
            oneTapClient.beginSignIn(
                buildSignInRequest()
            ).await()
        } catch (e: Exception) {
            null
        }
        return result?.pendingIntent?.intentSender
    }

    suspend fun getSignInResult(intent: Intent): SignInResult {
        val credential = oneTapClient.getSignInCredentialFromIntent(intent)
        val googleIdToken = credential.googleIdToken
        val googleCredentials = GoogleAuthProvider.getCredential(googleIdToken, null)
        return try {
            val user = auth.signInWithCredential(googleCredentials).await().user
            SignInResult(
                data = user?.run { User(id = uid, username = displayName ?: "", profilePicture = photoUrl.toString(), email = email ?: "") },
                errorMessage = null
            )
        } catch (e: Exception) {
            SignInResult(data = null, errorMessage = e.message)
        }
    }

    fun getSignInResultFromEmail(email: String, password: String, callback: (SignInResult) -> Unit) {
        auth.signInWithEmailAndPassword(email, password).addOnSuccessListener {
            val user = auth.currentUser
            try {
                callback.invoke(SignInResult(
                    data = user?.run {
                        User(
                            id = uid,
                            username = email,
                            profilePicture = null,
                            email = email
                        )
                    },
                    errorMessage = null
                ))
            } catch (e: Exception) {
                callback.invoke(SignInResult(data = null, errorMessage = e.message))
            }
        }
    }

    suspend fun signOut() {
        try {
            oneTapClient.signOut().await()
            auth.signOut()
        } catch (_: Exception) {

        }
    }

    private fun buildSignInRequest(): BeginSignInRequest {
        return BeginSignInRequest.builder()
            .setGoogleIdTokenRequestOptions(
                GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(context.getString(R.string.web_client_id))
                    .build()
            )
            .setAutoSelectEnabled(true)
            .build()
    }
}