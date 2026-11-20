package coredevices.util.integrations

import com.eygraber.uri.Uri

// macOS v1: no OAuth flows (no Firefox sign-in, no ring integrations).
class MacosOAuthLauncher : OAuthLauncher {
    override suspend fun authenticate(
        authUrl: String,
        callbackScheme: String,
        expectedPathSegment: String,
    ): Uri = throw OAuthCancelledException()
}
