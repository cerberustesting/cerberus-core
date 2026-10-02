# MCP Delta (`/mcpdelta/mcp`)

MCP Delta is a second MCP endpoint served by the Cerberus web app, next to `/mcp`. It has 5 tools
instead of one tool per operation:

| Tool | What it does |
|---|---|
| `read` | Renders any Cerberus object as a text document (testcase, folder, application, service, datalib, campaign, robot, environment…), an execution analysis, or the outline of a page |
| `find` | Finds where a value is used: matching document lines across a folder, an application, a label or everything |
| `write` | Applies whole documents, `old text → new text` edits or replace rules, validated first and written in one transaction; `dryRun` simulates exactly |
| `run` | Queues executions or campaigns, waits for them and returns the failures already analysed; also drives debug sessions |
| `undo` | Restores what a write changed, if nobody modified it since |

It reads and writes the same tables as the web app, and executions go through the same queue
(`QueuedExecutionService`) and debug service (`DebugExecutionService`). The sources live in
`org.cerberus.core.mcpdelta`; `mcpdelta.host` is the only part that knows about the web app.

## Enabling it on an instance

1. Deploy the WAR. The database migration creates the parameter `cerberus_mcpdelta_enable` with
   the value `false`: the endpoint answers `403 MCP Delta is disabled` until it is turned on.
2. In *Administration > Parameters*, set `cerberus_mcpdelta_enable` to `true`. Parameters take up to
   60 seconds to leave the cache. `/mcp` keeps its own toggle, `cerberus_mcp_enable`; each endpoint
   can be on or off independently.
3. Optional: set `cerberus_log_mcpcalls` to `true` to log every call in *Log Viewer*, with the
   caller and the tool (`delta/read`, `delta/write`…), never the arguments.

## Connecting a client

The address is the one used in the browser, context path included, followed by `/mcpdelta/mcp`
(for example `https://cerberus.example.com/Cerberus/mcpdelta/mcp`). Authentication is exactly the
one of `/mcp` (see [AUTHENTICATION.md](AUTHENTICATION.md)): both paths share the same Spring
Security filter chain and `McpApiKeyAuthFilter`.

**Keycloak instance (OAuth).** No header to configure: an unauthenticated call gets
`401 WWW-Authenticate: Bearer resource_metadata=".../.well-known/oauth-protected-resource/mcpdelta/mcp"`
(RFC 9728), and the client runs the login flow against Keycloak itself.

```bash
claude mcp add --transport http cerberus-delta https://cerberus.example.com/Cerberus/mcpdelta/mcp
```

Then `/mcp` in Claude Code opens the Keycloak login. A client that cannot send headers, such as a
claude.ai custom connector, needs this mode.

**Local authentication.** HTTP Basic with the Cerberus login and password, or the personal API key
of the user (*My profile*) in `X-API-KEY`:

```bash
claude mcp add --transport http cerberus-delta https://cerberus.example.com/Cerberus/mcpdelta/mcp --header "X-API-KEY: <personal API key>"
```

## Who can do what

Unlike `/mcp`, Delta acts as the authenticated user and checks the Cerberus roles of that user
(Keycloak roles and the `userrole` table, merged). Testcases, objects and queue entries it creates
or modifies carry the user's login (`UsrCreated`, `UsrModif`). A refused call says which role is
missing and changes nothing.

| Operation | Role |
|---|---|
| `read`, `find`, waiting for a tag | `TestRO` |
| Create or modify testcases | `Test` |
| Delete testcases, create, modify or delete folders | `TestAdmin` |
| Labels | `Label` |
| Data library | `TestDataManager` |
| Applications, services, robots, environments | `Integrator` |
| Campaigns, executions, debug sessions | `RunTest` |
| Invariants, campaign event hooks | `Administrator` |
| Writing another user's context | `Administrator` |

`write` with a plan (`plan`) and `undo` check the same roles as the write they apply or revert.

## Security

- **Origin.** A request without an `Origin` header (CLI clients, server-to-server) is served. A
  browser request is served only from the instance's own origin or an origin listed in
  `org.cerberus.mcpdelta.allowedOrigins`; anything else gets `403 Origin not allowed`.
- **Size.** A request body above 10 MB is refused (`413`).
- **Journal.** `undo` relies on a journal holding the rows exactly as they were before each write,
  secrets included (datalib passwords, service headers). It is kept in
  `<catalina.base>/mcpdelta` by default, a folder the web app never serves, and should stay so.

## JVM properties (all optional)

| Property | Default | Purpose |
|---|---|---|
| `org.cerberus.mcpdelta.home` | `<catalina.base>/mcpdelta` | Folder of the undo journal |
| `org.cerberus.mcpdelta.grid` | the active robot executors without credentials, chrome first, tried in turn | Selenium grid used to outline a live page (`read` of `live:<url>`) |
| `org.cerberus.mcpdelta.allowedOrigins` | none | Comma-separated browser origins allowed besides the instance's own |

## Limits

- The *systems* a user is restricted to (`usersystem`) are not enforced, as on `/mcp`.
- The `executor` option of `run` is refused: Cerberus picks the robot executor itself.
- Outlining a live page needs a Selenium grid reachable from the server without credentials (a
  robot executor, or `org.cerberus.mcpdelta.grid`). A grid that does not answer is skipped and tried
  last for the next 10 minutes. Outlines of pages saved by executions always work.
- A data library entry is identified by its name, system, environment and country, but Cerberus does
  not enforce that these are unique. When several entries share them, Delta says so, shows the first
  one and refuses to change or delete it: give each entry its own key first.

## Troubleshooting

| Answer | Cause |
|---|---|
| `403` `MCP Delta is disabled` | `cerberus_mcpdelta_enable` is not `true`, or was changed less than 60 seconds ago |
| `401` `Unauthorized` | No credentials, wrong password or API key, expired token, or (Keycloak) a token without the audience `org.cerberus.keycloak.mcp.audience` when it is configured |
| `403` `Origin not allowed` | Browser call from another origin: add it to `org.cerberus.mcpdelta.allowedOrigins` |
| `… lacks the role X …` | The user needs that role in *User Manager* |
