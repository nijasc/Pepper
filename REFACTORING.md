# Architektur-Review & Refactoring-Befunde

Systematischer Befund pro Datei aus dem Architektur-Review (Stand 2026-06-17). Reines
Refactoring-Ziel: **keine Verhaltensänderung**, jede Änderung kompiliert isoliert (JDK 17),
ein Commit pro Subtask, keine Code-Kommentare. Befundformat: **Problem → Vorschlag → Umfang**.

## Priorisierung

| Prio | Datei (LOC) | Kernproblem |
|------|-------------|-------------|
| P1 | `action/admin/AdminView.java` (1531) | 14 Panels + PIN + Raffle-CRUD + Gallery + Stats in einer Klasse |
| P1 | `openai/OpenAIService.java` (550) | HTTP-Transport + Streaming-Parser + Circuit-Breaker + Token-Cache + Prompt-Bau |
| P1 | `action/navigation/NavigationManager.java` (814) | Mapping + Localization + Guiding + Scanning + Snapshots im Singleton |
| P2 | `action/dynamicanim/AnimationGenerator.java` (646) | Anim-Gen + Dance-Gen + Song-Research/Planning + Datenklassen |
| P2 | `MainActivity.java` (462) | Lifecycle + Spracherkennung + Watchdog |
| P2 | `action/selfie/SelfieController.java` (515) | Capture + Bildkomposition + lokaler Webserver + QR |
| P3 | `action/hold/HoldController.java` (476) | Animation + Voice + Touch + Eskalation |
| P3 | `action/dance/DanceLibraryView.java` (417), `action/raffle/RaffleJoinView.java` (415) | UI komplett in Code |
| Quer | `action/Action.java`, OpenAIService-Instanziierung | Feld-Injection statt Konstruktor; ad-hoc `new OpenAIService(...)` an 8+ Stellen |

## Detailbefunde

### `openai/OpenAIService.java` (550 LOC)
- **Problem:** Vier Verantwortlichkeiten in einer Klasse — (a) HTTP-Transport (`sendOpenAiRequest`,
  Streaming-Connection), (b) SSE-/Streaming-Parser (`consumeMarkers`, `extractSentence`,
  Marker-/Satz-Extraktion), (c) Circuit-Breaker + statischer Token-Cache, (d) Prompt-Bau
  (`buildSystemPrompt`, Raffle-/Emotion-Hints).
- **Vorschlag:** `OpenAiHttpClient` (Transport: Request/Response, gzip, Event-Stream öffnen),
  `OpenAiStreamParser` (Marker-/Satz-Extraktion aus dem SSE-Reader). `OpenAIService` bleibt
  Fassade mit unveränderter öffentlicher API (`sendOpenAiRequest`, `getResponseStreaming`,
  `lastLanguageTag`). Circuit-Breaker/Token-Cache als eigene Komponente (Folge-Subtask).
- **Umfang:** mittel. Validierung über `OpenAiServiceTest` + neuer Parser-Test.

### `action/dynamicanim/AnimationGenerator.java` (646 LOC)
- **Problem:** Animation-Generierung, Tanz-Generierung, Song-Research und Song-Planning plus die
  verschachtelten Datenklassen `SongPlan`/`SongResearch`/`SongPlan` liegen gemeinsam in einer Klasse.
- **Vorschlag:** `SongPlan`/`SongResearch` als Top-Level-Klassen; `planSong`/`researchSong` in einen
  `SongResearcher` auslagern. Später Anim- von Dance-Generierung trennen.
- **Umfang:** klein–mittel.

### Geteilte Infrastruktur (Querschnitt)
- **Problem:** `new OpenAIService(new ArrayList<>())` ad-hoc in `AnimationGenerator`,
  `SystemSpeechRewriter`, `QuizGenerator`, `MainActivity` (Auth-Prefetch). Token-Cache und
  Circuit-Breaker sind ohnehin statisch/geteilt — separate Instanzen sind inkonsistent.
- **Vorschlag:** Geteilte Instanz (`OpenAIService.shared()`) für die Utility-Aufrufer mit leerer
  Action-Liste; `setC` weiterhin pro Aufruf (Token nach Erst-Laden gecacht, Context danach
  irrelevant).
- **Umfang:** klein.

### `action/admin/AdminView.java` (1531 LOC)
- **Problem:** Größte God-Class. 14 Panels (PIN/Menu/DevLog/Gallery/Detail/Lang/History/Raffle/
  Camera/Status/Stats/Attract/Debug) programmatisch gebaut; PIN-Lockout, Raffle-CRUD,
  Selfie-Gallery, Stats-Export, DSGVO-Export, Kamera-Test gemischt.
- **Vorschlag:** Panel-Switching → `PanelNavigator`; je Panel eine Komponente (PinController,
  Dashboard, Gallery, Raffle, …); danach Panel-Layouts nach XML + ViewBinding.
- **Umfang:** groß (mehrere Subtasks). UI-Verhalten muss erhalten bleiben → Hardware-Test je Schritt.

### `action/navigation/NavigationManager.java` (814 LOC)
- **Problem:** Singleton koordiniert Mapping+Localization, Guiding (GoTo), Scanning+Snapshot,
  Pose-Berechnung und Sprachausgabe.
- **Vorschlag:** Kollaboratoren `RoomMapper` (Scan/Localize), `Guide` (GoTo/Listen), `MapRenderer`;
  Manager bleibt Fassade. **Umfang:** groß.

### `MainActivity.java` (462 LOC)
- **Problem:** Lifecycle + Android-Spracherkennung + Listen-/Processing-State + Watchdog vermischt.
- **Vorschlag:** `SpeechSession` für Recognizer + Watchdog; Activity bleibt Lifecycle-Host. **Umfang:** mittel.

### `action/selfie/SelfieController.java` (515 LOC) / `action/hold/HoldController.java` (476)
- **Problem:** Selfie mischt Capture, Bildkomposition, lokalen Webserver, QR/WiFi-Payload;
  Hold mischt Animation, Voice-Listen, Touch-Sensor, Eskalation.
- **Vorschlag:** Capture/Upload/Storage bzw. Animation/Voice/Touch in Kollaboratoren trennen. **Umfang:** mittel.

### UI in Code (`DanceLibraryView`, `RaffleJoinView`, `NavigationView`)
- **Problem:** Layout programmatisch statt XML; inkonsistent zu XML-basierten Views.
- **Vorschlag:** Nach Aktivierung von ViewBinding schrittweise nach XML ziehen, Logik in Controller. **Umfang:** mittel.

### `action/Action.java`
- **Problem:** `HistoryManager` via Setter (`setHistoryManager`) feld-injiziert — späte Bindung,
  fehleranfällig.
- **Vorschlag:** Konstruktor-Übergabe, einheitlich für alle Actions. **Umfang:** mittel (alle Actions betroffen).

## Querschnitt
- UI programmatisch statt XML/ViewBinding (Voraussetzung: `viewBinding true`).
- Statischer mutierbarer State in `OpenAIService` (cachedToken, Circuit-Breaker) — pro Instanz isolieren.
- Single `:app`-Modul — mittelfristig `:core` + Feature-Module evaluieren (nur Entscheidung).

## Umsetzungsreihenfolge (empfohlen)
1. Fundament: ViewBinding aktivieren.
2. Risikoarme Infrastruktur-Splits: OpenAIService-Transport/Streaming, geteilte Instanz,
   AnimationGenerator-Datenklassen.
3. Danach größere, hardware-zu-testende Umbauten (AdminView-Panels, NavigationManager) einzeln.
