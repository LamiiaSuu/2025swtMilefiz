# MI’lefiz

MI’lefiz ist ein im Rahmen des Moduls **Softwaretechnik (Projekt 2025/26)** entwickeltes Mehrspieler-Spiel, das auf dem Brettspielklassiker **Malefiz** basiert und diesen um zusätzliche Mechaniken erweitert.  
Im Fokus stehen eine saubere, erweiterbare Architektur, kontinuierliche Entwicklung im Scrum-Prozess sowie eine lauffähige Full-Stack-Anwendung mit eigenem Game-Server.

## Projektidee (fachlich)

Während klassisches Malefiz rundenbasiert gespielt wird, verfolgt MI’lefiz einen stärker interaktiven Ansatz:

- **Mehrspieler-Spielinstanzen** auf einem serverseitig verwalteten Spielfeld (Map)
- **3D-Spielansicht** aus Ego-Perspektive (umschaltbar zwischen eigenen Figuren)
- **Bewegung entlang eines Wegenetzes** (Felder/Nodes)
- **Nicht rundenbasiert**: Spieler können alle *n* Sekunden würfeln und eine Figur bewegen
- **Begrenzte Sicht / keine vollständige Gegnerübersicht** (keine klassische Minimap)
- **Duell-System**: Betritt eine Figur ein Feld einer gegnerischen Figur, wird ein Duell als Minigame ausgelöst
- **Sperren-Mechanik**: Sperren können versetzt werden; belegte Felder werden im Auswahlmodus einheitlich markiert

Gewonnen hat, wer zuerst eine eigene Figur ins Zielfeld bringt.

## Systemübersicht

MI’lefiz besteht aus einem Web-Client (Frontend) und einem Spring-basierten Game-Server (Backend).  
Die Kommunikation erfolgt sowohl über **REST** (z.B. initiale Requests, Aktionen/Commands) als auch über **WebSocket (STOMP / PubSub)** für Live-Updates des Spielzustands.

### Komponenten

- **Game Client**
  - UI für Spielzustände, Interaktionen und Minigames
  - 3D-Visualisierung des Spielfelds und der Figuren
- **Game Server**
  - Verwaltung von Spielinstanzen (Sessions)
  - zentrale Spielregeln und Validierung
  - Synchronisation der Clients (Events/State Updates)
- **Map-/Editor-Funktionalität**
  - Erstellung/Bearbeitung von Spielfeldern (Basen, Ziel, Sperren)
  - Import/Export von Maps

## Tech Stack

**Backend**
- Java 21
- Spring Boot
- WebSocket (STOMP)
- REST API
- automatisierte Tests (JUnit)

**Frontend**
- Vue 3 (Composition API)
- TypeScript
- three.js (bzw. Wrapper wie tres.js möglich)
