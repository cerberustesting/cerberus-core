# Cerberus Authentication Overview

Cerberus supports two mutually-exclusive auth modes, selected by the Spring profile
`spring.profiles.active=local|keycloak` (Docker/entrypoint sets this). The DB parameter
`org.cerberus.authentification` (`Property.isKeycloak()`) is a related, informational flag used
by the UI/API to know which mode is active — it does not itself switch the Spring profile.

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

Both profiles share the same role-based URL authorization rules (`WebSecurityRules.java`).

## 2. Public API & MCP (`/api/public/**`, `/mcp`)

**Internal** — `X-API-KEY` header, validated by `PublicApiAuthenticationService`/`IAPIKeyService`
(REST API) or `McpApiKeyAuthFilter` (`/mcp`). `/mcp` is additionally gated by the
`cerberus_mcp_enable` DB parameter.

**Keycloak** — Bearer JWT validated by `NimbusJwtDecoder` against the realm's JWKS;
`PublicApiRoleFilter` / `McpApiKeyAuthFilter` enforce required roles (and optional audience for
`/mcp`). Both filters resolve auth in this order: reuse an existing Basic/Bearer `Authentication` →
else fall back to `X-API-KEY`.

- **Internal server-to-MCP calls** (AI chat backend, `AIMcpClientService`): reuses the logged-in
  user's cached OAuth token if present (Keycloak mode); otherwise falls back to a personal API key,
  lazily auto-generated per user (`resolveOrCreateApiKey`).
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
| `org.cerberus.keycloak.{url,realm,client,secret}` | Main Keycloak connection & web app client |
| `org.cerberus.keycloak.mcpclient` | Dedicated public OAuth client id for the MCP Inspector |
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