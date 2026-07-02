# Cerberus WebSocket System

## Architecture overview

Un seul endpoint partagé : `/ws/CerberusWebSocket`, géré par `CerberusWebSocket`.

Il n'y a **pas de handler WebSocket par feature**. À la place :
- Les clients se connectent une fois et **s'abonnent à des channels nommés**.
- Le serveur pousse des events vers ces channels via `WebSocketEventSender`.
- Le mapping session → channel est maintenu dans `WebSocketSessionRegistry`.

```
Client (browser)
    │  connect /ws/CerberusWebSocket
    ▼
CerberusWebSocket (TextWebSocketHandler)
    │  on connect : enregistre la session dans WebSocketSessionRegistry
    │  on message : route vers un service ou gère un abonnement channel
    ▼
WebSocketSessionRegistry
    │  wsId  →  WebSocketSession
    │  user  →  Set<wsId>
    │  channel → Set<wsId>
    │  appSessionID → wsId
    ▼
WebSocketEventSender
    │  sendToChannel / sendToUser / sendToAppSession
    │  throttling optionnel (cerberus_featureflipping_websocketpushperiod)
    ▼
Client reçoit WebSocketEnvelope<T> sérialisé en JSON
```

**Important : il n'y a plus d'indirection par Spring `ApplicationEvent`.** Les anciennes classes
`CerberusEvent` / `CerberusEventContext` / `CerberusEventToWebSocketRouter` ont été supprimées lors
du refactor. Tout composant (service métier, moteur d'exécution, MCP tool) qui veut pousser un
event injecte directement `WebSocketEventSender`.

---

## Composants

| Classe | Rôle |
|---|---|
| `CerberusWebSocket` | Handler Spring WS unique — reçoit les messages clients, gère les abonnements (`subscribe`/`unsubscribe`), route les `message` vers les services (IA, etc.) |
| `WebSocketSessionRegistry` | Registre en mémoire : session ↔ user / channel / appSessionID |
| `WebSocketEventSender` | Envoie des `WebSocketEnvelope` aux sessions ; gère le throttling et les compteurs de messages |
| `WebSocketEnvelope<T>` | Record du message envoyé sur le fil (`sender`, `channel`, `sessionID`, `user`, `payload`, `timestamp`) |
| `WebSocketStatic` | Toutes les constantes : channels (`CHANNEL_*`) et subjects clients (`SUBJECT_*`). Il n'y a plus de `TYPE_*` : le `channel` porte à lui seul le sens de l'event |
| `WebSocketService` | Mappe un event métier (`notifyExecutionStart`, `notifyExecutionUpdate`, `notifyExecutionDone`, `notifyExecutionMonitorRefresh`, `notifyQueueListRefresh`) vers l'ensemble des channels à notifier. Voir section dédiée ci-dessous |
| `runtime/QueueStatus` | Snapshot des exécutions en cours + compteurs de queue |
| `runtime/ExecutionMonitor` | Grille en mémoire des exécutions récentes pour le monitor homepage |
| `runtime/NotificationCenter` | Construit et pousse les listes d'exécutions personnelles d'un utilisateur (running / queued / dernières exécutions) au moment du subscribe |
| `WebSocketRegistryController` | Endpoint REST `/public/ws/registry/status` pour inspecter les sessions actives |

---

## Format du message — `WebSocketEnvelope`

Chaque message envoyé au client est sérialisé en :

```json
{
  "sender":    "system",
  "channel":   "execution.update.12345",
  "sessionID": null,
  "user":      null,
  "payload":   { ... },
  "timestamp": "2026-07-01T10:00:00Z"
}
```

- `channel` : le topic auquel le client est abonné **et** ce qui s'est passé — constantes `WebSocketStatic.CHANNEL_*` (il n'y a plus de champ `type` séparé)
- `sender` vaut toujours `"system"` pour les events poussés côté serveur (voir `WebSocketEnvelope.of(...)`)
- `sessionID` / `user` ne sont renseignés que pour un envoi ciblé (`sendToAppSession` / `sendToUser`) ; `null` pour un broadcast de channel (`sendToChannel`)
- `payload` : l'objet métier sérialisé en Map ou objet Jackson

---

## Protocole client

Le client envoie des messages JSON avec un champ `subject`. Les constantes sont dans `WebSocketStatic.SUBJECT_*` : `subscribe`, `unsubscribe`, `message`.

Le abonnement/désabonnement se fait sur une **liste** de channels (champ `channels`, pas `channel`).

### S'abonner à un ou plusieurs channels

```json
{
  "subject":   "subscribe",
  "channels":  ["execution.list.queued", "campaign.update.MyCampaign"],
  "sessionID": "abc-123",
  "sender":    "bcivel"
}
```

Pour chaque channel :
1. Le serveur vérifie qu'il est autorisé (`CerberusWebSocket#isSupportedSubscriptionChannel` — liste explicite + préfixes dynamiques comme `execution.update.*`, `execution.start.*`, `campaign.update.*`, `campaign.delta.*`).
2. Il enregistre la session dans `WebSocketSessionRegistry`.
3. Il pousse un état initial si le channel en a un (voir `sendChannelSpecificInit` : `myexecution.list.*`, `execution.list.queued`, `execution.monitor`).

Un channel inconnu lève une erreur (`Unsupported subscription channel`), renvoyée au client via un message d'erreur.

### Se désabonner

```json
{
  "subject":  "unsubscribe",
  "channels": ["campaign.update.MyCampaign"],
  "sessionID": "abc-123"
}
```

Sans `channels` (ou liste vide), toute la session est désinscrite (`webSocketSessionRegistry.unregister(session)`).

### `subject: "message"` — requêtes ponctuelles

Contrairement à `subscribe`/`unsubscribe`, `message` n'accepte **qu'un seul** channel dans `channels`
(sinon erreur `Only one channel is allowed for subject=message`). Ce channel identifie l'action demandée :

| Channel (dans `channels`) | Usage |
|---|---|
| `chat.send` | Chat IA (`AIService.chatWithAI`) |
| `testcase.proposal.request` | Génération de proposition de test |
| `testcase.create.request` | Création de test case par l'IA |
| `ao.generate.request` / `ao.generatecontinue.request` | Génération d'Application Objects |
| `execution.debug.request` | Debug IA sur une exécution |

---

## Channels existants

| Channel | Constante | Description |
|---|---|---|
| `chat.delta` / `chat.done` / `chat.title` / `chat.error` | `CHANNEL_CHAT_*` | Stream de réponse du chat IA |
| `tool.start` / `tool.result` / `tool.done` / `tool.error` | `CHANNEL_TOOL_*` | Cycle de vie d'un appel MCP tool, poussé vers l'appSession qui a déclenché l'appel |
| `execution.start` / `execution.update` / `execution.done` | `CHANNEL_EXECUTION_*` | Cycle de vie d'une exécution, broadcast |
| `execution.start.{id}` / `execution.update.{id}` / `execution.done.{id}` / `execution.delta.{id}` | `CHANNEL_EXECUTION_*_ID(id)` | Updates ciblées sur une exécution précise (page détail d'exécution) |
| `execution.light.start` / `execution.light.update` / `execution.light.done` | `CHANNEL_EXECUTION_LIGHT_*` | Version allégée (`TestcaseExecutionLightDTOV001`), broadcast — alimente les grilles/listes |
| `myexecution.light.start` / `myexecution.light.update` / `myexecution.light.done` | `CHANNEL_MYEXECUTION_LIGHT_*` | Même contenu, mais poussé uniquement à l'utilisateur qui a lancé l'exécution (`sendToUser`) |
| `execution.declarefalsenegative` / `execution.undeclarefalsenegative` | `CHANNEL_EXECUTION_(UN)DECLAREFALSENEGATIVE` | Notifie le changement de statut faux-négatif |
| `execution.list.running` / `execution.list.queued` / `execution.list.lastexecution` | `CHANNEL_EXECUTION_LIST_*` | Listes globales |
| `myexecution.list.running` / `myexecution.list.queued` / `myexecution.list.lastexecution` | `CHANNEL_MYEXECUTION_LIST_*` | Listes personnelles ; init poussée au `subscribe` par `NotificationCenter` |
| `execution.monitor` | `CHANNEL_EXECUTION_MONITOR` | Grille monitor de la homepage (`runtime/ExecutionMonitor`) |
| `queue.change` | `CHANNEL_QUEUE_CHANGE` | Changement dans la queue d'exécution |
| `campaign.start` / `campaign.update` / `campaign.delta` / `campaign.done` / `campaign.fail` / `campaign.success` | `CHANNEL_CAMPAIGN_*` | Cycle de vie d'une campagne, broadcast |
| `campaign.start.{campaign}` / `campaign.update.{campaign}` / `campaign.delta.{campaign}` / `campaign.done.{campaign}` | `CHANNEL_CAMPAIGN_*_ID(campaign)` | Updates ciblées sur une campagne précise |
| `objectcreation.application` / `.invariant` / `.testcase` / `.testcasestep` | `CHANNEL_OBJECTCREATION_*` | Notifie la création d'un objet par un MCP tool ou l'IA, poussé vers l'appSession appelante |
| `ao.proposals` / `testcase.proposals` | `CHANNEL_AO_PROPOSALS` / `CHANNEL_TESTCASE_PROPOSALS` | Propositions générées par l'IA, poussées vers l'appSession appelante |

Les channels `chat.send`, `execution.monitor`, `testcase.proposal.request`, `testcase.create.request`,
`ao.generate.request`, `ao.generatecontinue.request`, `execution.debug.request` sont des **channels
de requête client** (utilisés avec `subject: "message"`, voir plus haut), pas des channels d'update serveur → client.

---

## Envoyer des events depuis le serveur

### `WebSocketService` — un event métier, plusieurs channels

Un event métier (ex. "une exécution démarre") pousse souvent vers **plusieurs channels** en même
temps (broadcast, channel personnel, channel par id, campagne éventuelle...). Plutôt que de
dupliquer ces N appels à `WebSocketEventSender` dans chaque service appelant, le moteur d'exécution
centralise ce mapping event → channels dans `WebSocketService` :

```java
@Autowired
private WebSocketService webSocketService;

webSocketService.notifyExecutionStart(execution);
webSocketService.notifyExecutionUpdate(execution, forcePush);
webSocketService.notifyExecutionDone(execution);
webSocketService.notifyExecutionMonitorRefresh(execution);
webSocketService.notifyQueueListRefresh(queueStatus);
```

`ExecutionStartService` et `ExecutionRunService` n'importent plus `WebSocketEventSender` ni
`WebSocketStatic` directement : ils appellent une seule méthode par event, et
`WebSocketService` sait seule quels channels/payloads ça implique. Pour ajouter un channel à
notifier sur un event existant (ex. pousser aussi sur un nouveau channel au démarrage d'une
exécution), on modifie uniquement `WebSocketService#notifyExecutionStart` — pas chaque
site d'appel.

Pour un nouveau domaine (campagne, testcase, ...), ajouter les méthodes correspondantes dans
`WebSocketService` (ou, si le domaine grossit beaucoup, une classe dédiée du même style,
ex. `CampaignWebSocketService`) plutôt que de repousser à la main depuis le service métier.

### Injection directe de `WebSocketEventSender`

Pour un push isolé (un seul channel, pas de logique de fan-out à partager), l'injection directe
reste valide et c'est ce qu'utilisent les MCP tools et les services IA.

```java
@Autowired
private WebSocketEventSender webSocketEventSender;

// Broadcast à tous les abonnés du channel, sans throttle
webSocketEventSender.sendToChannel(
    WebSocketStatic.CHANNEL_EXECUTION_UPDATE_ID(execution.getId()),
    execution.toJson(true).toMap()
);

// Avec throttle : voir section Throttling ci-dessous
webSocketEventSender.sendToChannel(channel, payload, /*throttling*/ true, /*latestOnly*/ true);

// Vers un utilisateur connecté (toutes ses sessions abonnées au channel)
webSocketEventSender.sendToUser(execution.getExecutor(), WebSocketStatic.CHANNEL_MYEXECUTION_LIGHT_UPDATE, executionLight);

// Vers un onglet précis identifié par appSessionID
webSocketEventSender.sendToAppSession(appSessionID, WebSocketStatic.CHANNEL_TOOL_DONE, payload);
```

### Cas des MCP tools

Les MCP tools reçoivent un `Map<String, Object> args` qui peut contenir `appSessionID` et `user`
(transmis par le client quand l'appel vient de l'UI plutôt que d'un agent externe). Ils injectent
`WebSocketEventSender` comme n'importe quel autre composant et poussent directement dessus,
typiquement `tool.start` en entrée, `tool.done`/`objectcreation.*` en sortie :

```java
String appSessionID = MCPToolUtils.getString(args, "appSessionID", "");
String user = MCPToolUtils.getString(args, "user", "MCPTool");

webSocketEventSender.sendToAppSession(appSessionID, WebSocketStatic.CHANNEL_TOOL_START,
        Map.of("toolName", TOOL_NAME));
// ... exécution de l'outil ...
webSocketEventSender.sendToAppSession(appSessionID, WebSocketStatic.CHANNEL_OBJECTCREATION_INVARIANT,
        Map.of("toolName", TOOL_NAME, "invariant", invariantMapper.toDTO(invariantCreated)));
```

Voir `mcp/impl/invariant/CreateInvariantTool`, `mcp/impl/application/CreateApplicationTool` et
`ListApplicationTool` pour des exemples réels. Seule une poignée de tools a adopté ce pattern
pour l'instant — les autres MCP tools ne poussent encore aucun event WebSocket.

---

## Throttling

`sendToChannel(channel, payload, throttling=true, latestOnly)` limite la fréquence de push via le
paramètre `cerberus_featureflipping_websocketpushperiod` (défaut : 5000 ms). Clé de throttle : le `channel` lui-même.

Comportement :
- Si le délai depuis le dernier envoi est suffisant → envoi immédiat.
- Sinon → le payload est mis en attente et un flush est programmé après le délai restant.
- `latestOnly=true` : un seul payload en attente à la fois — chaque nouvel appel écrase le précédent (coalescing, utile pour un état qui se suffit à lui-même, ex. compteurs de queue).
- `latestOnly=false` : tous les payloads en attente sont conservés et flushés **dans l'ordre** (utile quand chaque event compte, ex. delta d'exécution).
- Une file en attente trop ancienne (> 2 min, `MAX_PENDING_RETENTION_MS`) est purgée sans être envoyée.
- Une file `latestOnly=false` trop longue est tronquée à 500 entrées (`MAX_PENDING_PAYLOADS_PER_CHANNEL`), les plus anciennes étant supprimées avec un log `WARN`.

Utiliser `sendToChannel(channel, payload)` (2 args, pas de throttle) pour les pushes qui doivent
partir immédiatement (fin d'exécution, etc.).

---

## Ajouter un nouveau channel — checklist

### 1. Déclarer le channel dans `WebSocketStatic` (et côté client dans `websocket.js`)

```java
// WebSocketStatic.java
public static final String CHANNEL_MY_FEATURE = "my.feature";
// ou dynamique :
public static String CHANNEL_MY_FEATURE_ID(long id) { return CHANNEL_MY_FEATURE + "." + id; }
```

```js
// src/main/webapp/js/static-data/websocket.js, dans CerberusWs.Channel
MY_FEATURE: 'my.feature',
```

### 2. Autoriser le `subscribe` dans `CerberusWebSocket`

Ajouter le cas dans `isSupportedSubscriptionChannel` (channel fixe) ou dans le bloc `default` par
préfixe (`channel.startsWith(...)`) pour un channel paramétré par id.

### 3. (Optionnel) Envoyer un état initial au subscribe

Ajouter un `case` dans `sendChannelSpecificInit` si les nouveaux abonnés doivent recevoir l'état
courant immédiatement, plutôt que d'attendre le prochain push.

### 4. Pousser les updates depuis ton service

```java
@Autowired
private WebSocketEventSender webSocketEventSender;

webSocketEventSender.sendToChannel(
    WebSocketStatic.CHANNEL_MY_FEATURE,
    payload,
    true,  // throttle si les updates sont fréquentes
    true   // latestOnly : coalesce si seul le dernier état compte
);
```

---

## Monitoring

`GET /public/ws/registry/status` (voir `WebSocketRegistryController`) retourne :

```json
{
  "totalSessions": 3,
  "totalMessagesSent": 142,
  "sessions": [
    {
      "wsId": "...",
      "user": "bcivel",
      "appSessionId": "abc-123",
      "channels": ["execution.list.queued", "execution.update.42"],
      "open": true,
      "messagesSent": 47
    }
  ]
}
```

---

## Données runtime (`websocket/runtime`)

| Classe | Description |
|---|---|
| `ExecutionMonitor` | Grille en mémoire des exécutions récentes pour le monitor homepage. Chargée au démarrage depuis la DB, mise à jour à chaque nouvelle exécution (`addNewExecutionToMonitor`). |
| `QueueStatus` | Snapshot des exécutions en cours (`executionHashMap`) et compteurs de queue (`running` / `queueSize` / `globalLimit`). Rafraîchi via `refreshQueueToTreat()` à chaque changement et au subscribe. |
| `NotificationCenter` | Ne maintient pas d'état : lit à la demande (running / queued / dernières exécutions d'un utilisateur) et pousse le résultat vers l'appSession au moment du `subscribe` sur un channel `myexecution.list.*`. |