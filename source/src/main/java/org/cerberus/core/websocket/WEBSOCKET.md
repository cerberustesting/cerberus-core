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

---

## Composants

| Classe | Rôle |
|---|---|
| `CerberusWebSocket` | Handler Spring WS — reçoit les messages clients, gère les abonnements, dispatche vers les services |
| `WebSocketSessionRegistry` | Registre en mémoire : session ↔ user / channel / appSessionID |
| `WebSocketEventSender` | Envoie des `WebSocketEnvelope` aux sessions ; gère le throttling |
| `WebSocketEnvelope<T>` | Wrapper de message envoyé sur le fil |
| `WebSocketStatic` | Toutes les constantes : channels (`CHANNEL_*`), types d'events (`TYPE_*`), subjects clients (`SUBJECT_*`) |
| `CerberusEvent` | Spring application event — publié par les **MCP tools** pour déclencher un push WS sans dépendre de `WebSocketEventSender` |
| `CerberusEventContext` | Contexte de routing embarqué dans un `CerberusEvent` (extrait des args MCP via `fromArgs`) |
| `CerberusEventToWebSocketRouter` | `@EventListener` qui route les `CerberusEvent` vers `WebSocketEventSender` |

---

## Format du message — `WebSocketEnvelope`

Chaque message envoyé au client est sérialisé en :

```json
{
  "sender":    "system",
  "type":      "execution.update",
  "channel":   "execution.12345",
  "sessionID": "abc-123",
  "user":      "bcivel",
  "payload":   { ... },
  "timestamp": "2026-06-17T10:00:00.000Z"
}
```

- `type` : ce qui s'est passé — constantes `WebSocketStatic.TYPE_*`
- `channel` : le topic auquel le client est abonné — constantes `WebSocketStatic.CHANNEL_*`
- `payload` : l'objet métier sérialisé en Map ou objet Jackson

---

## Protocole client

Le client envoie des messages JSON avec un champ `subject`. Les constantes sont dans `WebSocketStatic.SUBJECT_*`.

### S'abonner à un channel

```json
{
  "subject":   "subscribe",
  "channel":   "queue.status",
  "sessionID": "abc-123",
  "sender":    "bcivel"
}
```

Le serveur enregistre la session sur le channel et pousse immédiatement l'état courant.

### Se désabonner

```json
{
  "subject": "unsubscribe",
  "channel": "queue.status",
  "sessionID": "abc-123"
}
```

### Autres subjects

| `subject` | Usage |
|---|---|
| `chat_with_ai` | Chat IA |
| `test_proposal` | Génération de proposition de test |
| `test_creation` | Création de test case par l'IA |
| `execution_debug_assistant` | Debug IA sur une exécution |
| `ao_generate` / `ao_generate_continue` | Génération d'Application Objects |

---

## Channels existants

| Channel | Constante | Description |
|---|---|---|
| `queue.status` | `WebSocketStatic.CHANNEL_QUEUE_STATUS` | Compteurs running/queued |
| `execution.monitor` | `WebSocketStatic.CHANNEL_EXECUTION_MONITOR` | Grille monitor homepage |
| `execution.{id}` | `WebSocketStatic.CHANNEL_EXECUTION(id)` | Updates live d'une exécution |
| `ai.chat` | `WebSocketStatic.CHANNEL_AI_CHAT` | Stream de réponse IA |

---

## Envoyer des events depuis le serveur

### Option 1 — injection directe de `WebSocketEventSender`

À utiliser dans les services et le moteur d'exécution.

```java
@Autowired
private WebSocketEventSender webSocketEventSender;

// Tous les abonnés au channel, sans throttle
webSocketEventSender.sendToChannel(
    WebSocketStatic.CHANNEL_EXECUTION(executionId),
    WebSocketStatic.TYPE_EXECUTION_UPDATE,
    payload   // Map<String,Object> ou tout objet sérialisable par Jackson
);

// Avec throttle : drop les pushes excessifs, flush le dernier après la période
webSocketEventSender.sendToChannel(channel, type, payload, true);

// Vers un utilisateur connecté (toutes ses sessions)
webSocketEventSender.sendToUser(user, type, channel, payload);

// Vers un onglet précis identifié par appSessionID
webSocketEventSender.sendToAppSession(appSessionID, type, channel, payload);
```

### Option 2 — `CerberusEvent` via Spring (MCP tools uniquement)

Les MCP tools reçoivent un `Map<String, Object> args` qui peut contenir une clé `_context`
avec les infos de routing (user, appSessionID, channel) transmises par le client IA.
Ils ne connaissent pas la session WebSocket directement — ils publient un event Spring,
et `CerberusEventToWebSocketRouter` se charge du routage.

```java
@Autowired
private ApplicationEventPublisher eventPublisher;

CerberusEventContext context = CerberusEventContext.fromArgs(args); // lit _context dans les args MCP

// Notification vers la session qui a déclenché l'appel MCP
eventPublisher.publishEvent(CerberusEvent.of(
    "ui.notification",
    context,
    Map.of("level", "success", "title", "OK", "message", "Création réussie")
));
```

`CerberusEventToWebSocketRouter` route selon la priorité : **appSessionID > user > channel**.

**Ne pas utiliser** cette option hors des MCP tools — préférer l'injection directe de `WebSocketEventSender`.

---

## Throttling

`sendToChannel(..., throttling=true)` limite la fréquence de push via le paramètre
`cerberus_featureflipping_websocketpushperiod` (défaut : 5000 ms).

Comportement :
- Si le délai depuis le dernier envoi est suffisant → envoi immédiat.
- Sinon → stocke le dernier payload et programme un flush après le délai restant.
- Le flush envoie toujours le **dernier** payload reçu, pas le premier.
- Clé de throttle : `channel:type`.

Utiliser `throttling=false` (ou la surcharge à 3 args) pour les pushes forcés (fin d'exécution, etc.).

---

## Ajouter un nouveau channel — checklist

### 1. Déclarer le channel et le type dans `WebSocketStatic`

```java
public static final String CHANNEL_MY_FEATURE = "my.feature";
// ou dynamique :
public static String CHANNEL_MY_FEATURE(long id) { return "my.feature." + id; }

public static final String TYPE_MY_FEATURE_UPDATED = "my.feature.updated";
```

### 2. Gérer le `subscribe` dans `CerberusWebSocket`

Dans `handleTextMessage`, dans le bloc `case "subscribe"` :

```java
if (WebSocketStatic.CHANNEL_MY_FEATURE.equals(incoming.getChannel())) {
    webSocketEventSender.send(session, WebSocketEnvelope.of(
        WebSocketStatic.CHANNEL_MY_FEATURE,
        WebSocketStatic.TYPE_MY_FEATURE_UPDATED,
        incoming.getSessionID(),
        incoming.getSender(),
        myService.getCurrentState().toMap()
    ));
}
```

### 3. Pousser les updates depuis ton service

```java
@Autowired
private WebSocketEventSender webSocketEventSender;

webSocketEventSender.sendToChannel(
    WebSocketStatic.CHANNEL_MY_FEATURE,
    WebSocketStatic.TYPE_MY_FEATURE_UPDATED,
    payload,
    true   // throttle si les updates sont fréquents
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
      "channels": ["queue.status", "execution.42"],
      "open": true,
      "messagesSent": 47
    }
  ]
}
```

---

## Données runtime

| Classe | Description |
|---|---|
| `runtime/ExecutionMonitor` | Grille en mémoire des exécutions récentes pour le monitor homepage. Chargée au démarrage depuis la DB, mise à jour à chaque fin d'exécution. |
| `runtime/QueueStatus` | Snapshot des exécutions en cours et compteurs de queue. Construit depuis `ExecutionUUID` au moment du subscribe, poussé à chaque changement. |