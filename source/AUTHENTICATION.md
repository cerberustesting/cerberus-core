# Cerberus Authentication Overview

Cerberus supports two mutually-exclusive auth modes, selected by the Spring profile
`spring.profiles.active=local|keycloak` (Docker/entrypoint sets this). The DB parameter
`org.cerberus.authentification` (`Property.isKeycloak()`) is a related, informational flag used
by the UI/API to know which mode is active — it does not itself switch the Spring profile.

## Quick reference: authentication modes by use case

| # | Use case | Mechanism | Enforced by | Profile |
|---|---|---|---|---|
| 1 | Web App — local login | Form login (`j_username`/`j_password`) → HTTP session cookie | `WebSecurityLocalConfiguration`, `DaoAuthenticationProvider` | `local` |
| 2 | Web App — OIDC login | OIDC Authorization Code flow → HTTP session cookie | `WebSecurityKeycloakConfiguration` (`oauth2Login()`) | `keycloak` |
| 3 | Public API via API key | `X-API-KEY` header on `/api/public/**` | `PublicApiAuthenticationService` / `IAPIKeyService` (gated by `cerberus_apikey_enable`) | both |
| 4 | Public API via OIDC | `Authorization: Bearer` JWT on `/api/public/**` | `publicApiJwtDecoder` + `PublicApiRoleFilter` (role-based, no audience check) | `keycloak` |
| 5 | Private API via session | Reuses the existing web-login session (`SecurityContextHolder`) — no separate token | `@PreAuthorize`-annotated controllers under `org.cerberus.core.apiprivate` (e.g. `AIPrivateController`) | both — it's #1/#2's session reused for the SPA/JSP's own AJAX calls |
| 6 | MCP tool from GUI via OIDC | Authorization Code + PKCE from the browser → `Authorization: Bearer` to `/mcp` | MCP Inspector (`McpInspector.js`) + `mcpJwtDecoder`/`McpApiKeyAuthFilter`; client id `org.cerberus.keycloak.mcpclient` | `keycloak` |
| 7 | MCP tool from GUI via API key | Personal API key pasted in the Inspector → `X-API-KEY` to `/mcp` | MCP Inspector fallback + `McpApiKeyAuthFilter` | both — default when Keycloak/`mcpclient` isn't available |
| 8 | MCP from a public AI provider via API key | `X-API-KEY` to `/mcp` — either the AI chat backend's fallback key, or a third-party client (Claude Desktop/Code, …) configured with a personal key | `AIMcpClientService.resolveOrCreateApiKey` + `McpApiKeyAuthFilter` | both |
| 9 | MCP from a public AI provider via OIDC | (a) AI chat backend: token exchange (RFC 8693) of the user's login token into one audienced `org.cerberus.keycloak.mcp.audience`; (b) third-party client: self-discovers OAuth via RFC 9728 `WWW-Authenticate` then runs its own Authorization Code flow against Keycloak | (a) `AIMcpClientService` + `TokenExchangeOAuth2AuthorizedClientProvider`; (b) `OAuthProtectedResourceMetadataServlet` + `mcpJwtDecoder` | `keycloak` |
| 10 | MCP from any client via HTTP Basic | Cerberus username/password sent as HTTP Basic to `/mcp` — the auth scheme actually advertised (`WWW-Authenticate: Basic realm="Cerberus MCP"`) to unauthenticated MCP clients in local mode, ahead of the X-API-KEY fallback (#8) | `WebSecurityLocalConfiguration.mcpSecurityFilterChain` (`.httpBasic()`) + `DaoAuthenticationProvider`; reused by `McpApiKeyAuthFilter` | `local` only — `/mcp`-specific, not available on `/api/public/**` |
| 11 | Local Runner via API key | `X-API-KEY`, same as the public REST API | Same as #3 | both |
| 12 | Local Runner via OIDC | Authorization Code + PKCE, dedicated client `org.cerberus.keycloak.localrunnerclient` — no dedicated server-side validation beyond exposing the client id via discovery | `GET /api/public/oauth-config` | `keycloak` |

*Not a distinct mode, but worth knowing:* legacy `zzpublic/*` servlets (`ResultCIV004`, `RunTestCaseV002`, `ManageV001`, …) use the exact same API-key mechanism as #3, just under older URLs (`permitAll()`-listed in `WebSecurityRules`); and Cerberus's own scheduler/queue workers call back into those same endpoints using a built-in `srvaccount` service-account API key (`APIKeyService.getServiceAccountAPIKey()`) — still mode #3, just with a system identity instead of a human one.

## 1. Cerberus Web App (browser login)
**Internal ("local" profile)** — `WebSecurityLocalConfiguration.java`
- Classic form login: `/Login.jsp` → `/j_security_check` (`j_username`/`j_password`).
- `DaoAuthenticationProvider` + `AuthenticationUserDetailsService` load the user/roles from the DB
  (`IUserService`, `IUserRoleService`), each role mapped to `ROLE_<name>`.
- Passwords hashed via `AuthenticationPasswordEncoder` (SHA1/SHA256/SHA512/MD5, default SHA1,
  configurable via `cerberus.password.encoding`).
- Cookie-based HTTP session (`HttpSessionSecurityContextRepository`).

**Keycloak ("keycloak" profile)** — `WebSecurityKeycloakConfiguration.java`
- OIDC Authorization Code flow (`oauth2Login()`), configured from system properties
  `org.cerberus.keycloak.{url,realm,client,secret}`.
- Roles come from the JWT's `realm_access.roles` / `resource_access.<client>.roles` claims via
  `KeycloakRoleMapper`.
- Tokens are cached in an `InMemoryOAuth2AuthorizedClientService` keyed by principal (not HTTP
  session), so background code (e.g. the AI chat WebSocket) can reuse a user's token later.
- The `Cerberus` client is public (no secret), so Spring's default refresh-token request — which only
  sends `client_id` when the client authenticates with `CLIENT_SECRET_POST` — omits it entirely,
  and Keycloak then rejects the refresh as invalid client credentials. `oAuth2AuthorizedClientManager`
  patches the refresh-token request to always include `client_id`.

Both profiles share the same role-based URL authorization rules (`WebSecurityRules.java`).

## 2. Public API & MCP (`/api/public/**`, `/mcp`)

**Internal** — `X-API-KEY` header. For the REST API, validated by
`PublicApiAuthenticationService`/`IAPIKeyService`, additionally gated by the legacy
`cerberus_apikey_enable` parameter (the same check also guards the legacy `zzpublic/*` servlets,
e.g. `ResultCIV004`/`ManageV001`, which predate `/api/public/**`). For `/mcp`, `McpApiKeyAuthFilter`
validates the key directly against `IUserService.verifyAPIKey` instead — it deliberately does
**not** route through `IAPIKeyService`, since that would incorrectly re-gate `/mcp` on the unrelated
`cerberus_apikey_enable` parameter (`cerberus_mcp_enable` is already the correct/sufficient gate).
`/mcp` is additionally gated by that `cerberus_mcp_enable` DB parameter. `/mcp` also has its own
dedicated `httpBasic()` filter chain (`WebSecurityLocalConfiguration.mcpSecurityFilterChain`) — a
Cerberus username/password challenged via `WWW-Authenticate: Basic`, resolved first, with
X-API-KEY as the fallback for clients that don't speak Basic.

**Keycloak** — Bearer JWT validated by `NimbusJwtDecoder` against the realm's JWKS;
`PublicApiRoleFilter` / `McpApiKeyAuthFilter` enforce required roles (and optional audience for
`/mcp`). Both filters resolve auth in this order: reuse an existing Basic/Bearer `Authentication` →
else fall back to `X-API-KEY`.

- **Internal server-to-MCP calls** (AI chat backend, `AIMcpClientService`): the user's cached
  `keycloak` login token is audienced for the web app client, not `/mcp` — `WebSecurityKeycloakConfiguration`
  registers a second, dedicated `cerberus-mcp` `ClientRegistration` (public client,
  `AuthorizationGrantType.TOKEN_EXCHANGE`) and a `TokenExchangeOAuth2AuthorizedClientProvider` that
  exchanges the login token (RFC 8693, Keycloak "Standard Token Exchange") for one issued to
  `cerberus-mcp` / audienced `org.cerberus.keycloak.mcp.audience`. Falls back to the user's personal
  API key (`resolveOrCreateApiKey`) if there's no cached login token or the exchange fails.
- **MCP Inspector (browser UI)**: probes `GET /api/public/oauth-config`; if Keycloak is enabled and
  a dedicated `cerberusMcpClientId` is configured, it runs Authorization Code + PKCE as a public
  client and sends the resulting token as `Authorization: Bearer`. Otherwise it falls back to
  pasting in an API key.
- **Third-party MCP clients** (Claude Desktop/Code, etc.): an unauthenticated call to `/mcp` gets a
  `401` with `WWW-Authenticate: Bearer resource_metadata="…/.well-known/oauth-protected-resource"`
  (RFC 9728, served by `OAuthProtectedResourceMetadataServlet`), letting the client self-discover
  the OAuth flow with no static config needed.

## 3. Local Runner (external tool)

The "local runner" is an external/third-party tool, not a component implemented in this repo — it
authenticates like any other public API consumer:
- **Internal mode**: falls back to `X-API-KEY`, same as the REST API / MCP.
- **Keycloak mode**: discovers `keycloakUrl` / `realm` / `localRunnerClientId` via
  `GET /api/public/oauth-config` (populated only when Keycloak is active), then is expected to run
  Authorization Code + PKCE as a public client (dedicated client id, e.g. `cerberus-local-runner`,
  no secret since it can't be safely embedded in a local tool).

There is no server-side registration/validation of this client id beyond exposing it in the
discovery endpoint — unlike the MCP client, which has its own JWT decoder wired server-side.

## Key toggles

| Property | Purpose |
|---|---|
| `spring.profiles.active=local\|keycloak` | Selects the active `WebSecurityXxxConfiguration` |
| `org.cerberus.authentification=keycloak` | Informational flag (`Property.isKeycloak()`) driving UI/API behavior |
| `cerberus_mcp_enable` (DB parameter) | Enables/disables `/mcp` entirely |
| `cerberus_mcpdelta_enable` (DB parameter) | Enables/disables `/mcpdelta/mcp` (MCP Delta, see [MCPDELTA.md](MCPDELTA.md)), which shares the `/mcp` filter chain and authentication |
| `org.cerberus.keycloak.{url,realm,client,secret}` | Main Keycloak connection & web app client |
| `org.cerberus.keycloak.mcpclient` | Dedicated public OAuth client id for the MCP Inspector *and* the server-side token exchange used by `AIMcpClientService` |
| `org.cerberus.keycloak.mcp.audience` | Required `aud` claim on tokens accepted by `/mcp` (validated by `mcpJwtDecoder`); target audience of the `cerberus-mcp` token exchange |
| `org.cerberus.keycloak.localrunnerclient` | Dedicated public OAuth client id for local runner tools |

## Key files

- `src/main/java/org/cerberus/core/config/security/WebSecurityLocalConfiguration.java`
- `src/main/java/org/cerberus/core/config/security/WebSecurityKeycloakConfiguration.java`
- `src/main/java/org/cerberus/core/config/security/WebSecurityRules.java`
- `src/main/java/org/cerberus/core/config/security/KeycloakRoleMapper.java`
- `src/main/java/org/cerberus/core/config/security/McpApiKeyAuthFilter.java`
- `src/main/java/org/cerberus/core/config/security/OAuthProtectedResourceMetadataServlet.java`
- `src/main/java/org/cerberus/core/service/ai/impl/AIMcpClientService.java`
- `src/main/java/org/cerberus/core/api/controllers/OAuthConfigController.java`
- `src/main/webapp/js/pages/McpInspector.js`