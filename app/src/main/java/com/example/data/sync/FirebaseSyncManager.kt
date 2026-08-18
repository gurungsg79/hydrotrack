package com.example.data.sync

import android.content.Context
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import com.example.data.local.WaterLogDao
import com.example.data.model.WaterLogEntity
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

sealed class SyncState {
    object Idle : SyncState()
    object Syncing : SyncState()
    data class Synced(val message: String, val timestamp: Long = System.currentTimeMillis()) : SyncState()
    data class Error(val errorMessage: String) : SyncState()
}

class FirebaseSyncManager(
    private val context: Context,
    private val waterLogDao: WaterLogDao
) {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val credentialManager = CredentialManager.create(context)

    private val _syncState = MutableStateFlow<SyncState>(SyncState.Idle)
    val syncState: StateFlow<SyncState> = _syncState.asStateFlow()

    private val _currentUser = MutableStateFlow<FirebaseUser?>(auth.currentUser)
    val currentUser: StateFlow<FirebaseUser?> = _currentUser.asStateFlow()

    init {
        auth.addAuthStateListener { firebaseAuth ->
            _currentUser.value = firebaseAuth.currentUser
        }
    }

    suspend fun signInWithGoogle(activityContext: Context, webClientId: String = ""): Result<FirebaseUser> = withContext(Dispatchers.IO) {
        try {
            _syncState.value = SyncState.Syncing

            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(if (webClientId.isNotBlank()) webClientId else "placeholder-client-id.apps.googleusercontent.com")
                .setAutoSelectEnabled(true)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val result = credentialManager.getCredential(
                request = request,
                context = activityContext
            )

            val credential = result.credential
            if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val authCredential = GoogleAuthProvider.getCredential(googleIdTokenCredential.idToken, null)
                val authResult = auth.signInWithCredential(authCredential).await()
                val user = authResult.user ?: throw Exception("Firebase user is null")
                _currentUser.value = user
                _syncState.value = SyncState.Synced("Signed in as ${user.email}")

                // Perform immediate sync
                syncDataWithCloud(user.uid)
                Result.success(user)
            } else {
                throw Exception("Unexpected credential type: ${credential.type}")
            }
        } catch (e: Exception) {
            Log.w("FirebaseSync", "Google Sign-In failed or cancelled: ${e.message}")
            _syncState.value = SyncState.Error(e.message ?: "Sign-in cancelled")
            Result.failure(e)
        }
    }

    suspend fun signOut() = withContext(Dispatchers.IO) {
        try {
            auth.signOut()
            credentialManager.clearCredentialState(ClearCredentialStateRequest())
            _currentUser.value = null
            _syncState.value = SyncState.Idle
        } catch (e: Exception) {
            Log.e("FirebaseSync", "Sign out error", e)
        }
    }

    suspend fun syncDataWithCloud(userId: String? = auth.currentUser?.uid) = withContext(Dispatchers.IO) {
        val uid = userId ?: auth.currentUser?.uid ?: return@withContext

        try {
            _syncState.value = SyncState.Syncing

            val userDocRef = firestore.collection("users").document(uid)
            val logsColRef = userDocRef.collection("water_logs")

            // 1. Upload unsynced local logs
            val unsynced = waterLogDao.getUnsyncedLogs()
            for (log in unsynced) {
                val data = hashMapOf(
                    "id" to log.id,
                    "timestamp" to log.timestamp,
                    "amountMl" to log.amountMl,
                    "effectiveHydrationMl" to log.effectiveHydrationMl,
                    "beverageType" to log.beverageType,
                    "note" to log.note,
                    "dateKey" to log.dateKey,
                    "deviceSyncTimestamp" to System.currentTimeMillis()
                )
                logsColRef.document(log.id.toString()).set(data, SetOptions.merge()).await()
                waterLogDao.updateLog(log.copy(syncedToCloud = true))
            }

            // 2. Download any cloud logs from other devices
            val snapshot = logsColRef.limit(100).get().await()
            val cloudLogs = snapshot.documents.mapNotNull { doc ->
                val amount = doc.getLong("amountMl")?.toInt() ?: return@mapNotNull null
                val effective = doc.getLong("effectiveHydrationMl")?.toInt() ?: amount
                val ts = doc.getLong("timestamp") ?: return@mapNotNull null
                val bev = doc.getString("beverageType") ?: "WATER"
                val note = doc.getString("note") ?: ""
                val dateKey = doc.getString("dateKey") ?: WaterLogEntity.getCurrentDateKey(ts)
                val docId = doc.getLong("id") ?: ts

                WaterLogEntity(
                    id = docId,
                    timestamp = ts,
                    amountMl = amount,
                    effectiveHydrationMl = effective,
                    beverageType = bev,
                    note = note,
                    syncedToCloud = true,
                    dateKey = dateKey
                )
            }

            if (cloudLogs.isNotEmpty()) {
                waterLogDao.insertLogs(cloudLogs)
            }

            _syncState.value = SyncState.Synced("Synced successfully with Cloud")
        } catch (e: Exception) {
            Log.w("FirebaseSync", "Cloud sync exception: ${e.message}")
            _syncState.value = SyncState.Error("Cloud sync offline: ${e.localizedMessage ?: "Network issue"}")
        }
    }
}
