package com.capstone.ggud.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.capstone.ggud.network.dto.LoginResponse
import kotlinx.coroutines.flow.first

private val Context.dataStore by preferencesDataStore(name = "auth_store")

class TokenStore(private val context: Context) {

    private val KEY_ACCESS = stringPreferencesKey("access_token")
    private val KEY_REFRESH = stringPreferencesKey("refresh_token")
    private val KEY_EXPIRES_AT = longPreferencesKey("expires_at_epoch_ms")
    private val KEY_OAUTH_STATE = stringPreferencesKey("oauth_state")

    suspend fun saveLogin(login: LoginResponse) {
        val expiresAt = System.currentTimeMillis() + (login.expiresIn * 1000L)
        context.dataStore.edit { pref ->
            pref[KEY_ACCESS] = login.accessToken
            login.refreshToken?.let { pref[KEY_REFRESH] = it }
            pref[KEY_EXPIRES_AT] = expiresAt
        }
    }

    suspend fun saveExpectedState(state: String) {
        context.dataStore.edit { pref -> pref[KEY_OAUTH_STATE] = state }
    }

    suspend fun getExpectedState(): String? {
        val pref = context.dataStore.data.first()
        return pref[KEY_OAUTH_STATE]
    }

    suspend fun clearExpectedState() {
        context.dataStore.edit { pref -> pref.remove(KEY_OAUTH_STATE) }
    }

    suspend fun getAccessToken(): String? {
        val pref = context.dataStore.data.first()
        return pref[KEY_ACCESS]
    }

    suspend fun getRefreshToken(): String? {
        val pref = context.dataStore.data.first()
        return pref[KEY_REFRESH]
    }

    suspend fun getExpiresAt() : Long? {
        val pref = context.dataStore.data.first()
        return pref[KEY_EXPIRES_AT]
    }

    suspend fun clearAll() {
        context.dataStore.edit { it.clear() }
    }
}
