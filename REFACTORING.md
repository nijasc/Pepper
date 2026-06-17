# Architektur-Review & Refactoring-Befunde

Systematischer Befund pro Datei aus dem Architektur-Review (Stand 2026-06-17). Reines
Refactoring-Ziel: **keine Verhaltensänderung**, jede Änderung kompiliert isoliert (JDK 17),
ein Commit pro Subtask, keine Code-Kommentare. Befundformat: **Problem → Vorschlag → Umfang**.

## Umsetzungsstand

Alle priorisierten God-Class- und Infrastruktur-Splits sind umgesetzt; die Fassaden
(`AdminView`, `OpenAIService`, `NavigationManager`, `AnimationGenerator`, `SelfieController`,
`HoldController`, `MainActivity`) behalten ihre öffentliche API, die Verantwortlichkeiten liegen
in eigenen Kollaboratoren. Offen bleiben bewusst zurückgestellte Entscheidungen
(Multi-Modul-Schnitt, Package-Schnitt-Schritte 2–3) — siehe unten.

## Priorisierung

| Prio | Datei (LOC) | Kernproblem | Status |
|------|-------------|-------------|--------|
| P1 | `action/admin/AdminView.java` (1531) | 14 Panels + PIN + Raffle-CRUD + Gallery + Stats in einer Klasse | ✅ Panels in Controller + ViewBinding |
| P1 | `openai/OpenAIService.java` (550) | HTTP-Transport + Streaming-Parser + Circuit-Breaker + Token-Cache + Prompt-Bau | ✅ Transport/Parser/Breaker/Token ausgelagert |
| P1 | `action/navigation/NavigationManager.java` (814) | Mapping + Localization + Guiding + Scanning + Snapshots im Singleton | ✅ in Kollaboratoren + Fassade aufgeteilt |
| P2 | `action/dynamicanim/AnimationGenerator.java` (646) | Anim-Gen + Dance-Gen + Song-Research/Planning + Datenklassen | ✅ Datenklassen + `SongResearcher` + `GeneratorBase` |
| P2 | `MainActivity.java` (462) | Lifecycle + Spracherkennung + Watchdog | ✅ `SpeechSession` ausgelagert |
| P2 | `action/selfie/SelfieController.java` (515) | Capture + Bildkomposition + lokaler Webserver + QR | ✅ `SelfieShareServer` ausgelagert |
| P3 | `action/hold/HoldController.java` (476) | Animation + Voice + Touch + Eskalation | ✅ `HoldQuotes` ausgelagert |
| P3 | `action/dance/DanceLibraryView.java` (417), `action/raffle/RaffleJoinView.java` (415) | UI komplett in Code | ✅ auf ViewBinding umgestellt |
| Quer | `action/Action.java`, OpenAIService-Instanziierung | Feld-Injection statt Konstruktor; ad-hoc `new OpenAIService(...)` an 8+ Stellen | ✅ Konstruktor-Injection + geteilte Instanz |

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
- **✅ Umgesetzt:** `OpenAiHttpClient` (Transport) + `OpenAiStreamParser`/`OpenAiResponse` (SSE),
  `OpenAiCircuitBreaker` + `OpenAiTokenProvider` (Breaker/Token-Cache); `OpenAIService` ist Fassade.

### `action/dynamicanim/AnimationGenerator.java` (646 LOC)
- **Problem:** Animation-Generierung, Tanz-Generierung, Song-Research und Song-Planning plus die
  verschachtelten Datenklassen `SongPlan`/`SongResearch`/`SongPlan` liegen gemeinsam in einer Klasse.
- **Vorschlag:** `SongPlan`/`SongResearch` als Top-Level-Klassen; `planSong`/`researchSong` in einen
  `SongResearcher` auslagern. Später Anim- von Dance-Generierung trennen.
- **Umfang:** klein–mittel.
- **✅ Umgesetzt:** `SongPlan`/`SongResearch` als Top-Level-Klassen, `SongResearcher` (Research/Planning);
  Anim- von Dance-Generierung über `GeneratorBase` getrennt.

### Geteilte Infrastruktur (Querschnitt)
- **Problem:** `new OpenAIService(new ArrayList<>())` ad-hoc in `AnimationGenerator`,
  `SystemSpeechRewriter`, `QuizGenerator`, `MainActivity` (Auth-Prefetch). Token-Cache und
  Circuit-Breaker sind ohnehin statisch/geteilt — separate Instanzen sind inkonsistent.
- **Vorschlag:** Geteilte Instanz (`OpenAIService.shared()`) für die Utility-Aufrufer mit leerer
  Action-Liste; `setC` weiterhin pro Aufruf (Token nach Erst-Laden gecacht, Context danach
  irrelevant).
- **Umfang:** klein.
- **✅ Umgesetzt:** geteilte `OpenAIService.shared()`-Instanz statt ad-hoc `new OpenAIService(...)`.

### `action/admin/AdminView.java` (1531 LOC)
- **Problem:** Größte God-Class. 14 Panels (PIN/Menu/DevLog/Gallery/Detail/Lang/History/Raffle/
  Camera/Status/Stats/Attract/Debug) programmatisch gebaut; PIN-Lockout, Raffle-CRUD,
  Selfie-Gallery, Stats-Export, DSGVO-Export, Kamera-Test gemischt.
- **Vorschlag:** Panel-Switching → `PanelNavigator`; je Panel eine Komponente (PinController,
  Dashboard, Gallery, Raffle, …); danach Panel-Layouts nach XML + ViewBinding.
- **Umfang:** groß (mehrere Subtasks). UI-Verhalten muss erhalten bleiben → Hardware-Test je Schritt.
- **✅ Umgesetzt:** `PanelNavigator` + `PinController`, `DashboardController`, `SelfieGalleryController`,
  `RaffleAdminController`, `LanguagePanelController`, `HistoryPanelController`, `AttractPanelController`,
  `CameraPanelController`; `AdminView` auf ViewBinding umgestellt (reiner Container).

### `action/navigation/NavigationManager.java` (814 LOC)
- **Problem:** Singleton koordiniert Mapping+Localization, Guiding (GoTo), Scanning+Snapshot,
  Pose-Berechnung und Sprachausgabe.
- **Vorschlag:** Kollaboratoren `RoomMapper` (Scan/Localize), `Guide` (GoTo/Listen), `MapRenderer`;
  Manager bleibt Fassade. **Umfang:** groß.
- **✅ Umgesetzt:** `RoomScanner` (Scan/Snapshot), `RobotLocalizer` (Mapping/Localization),
  `RobotGuide` (GoTo/Listen), `NavMapRenderer` (Karten-Rendering); `NavigationManager` ist Fassade.

### `MainActivity.java` (462 LOC)
- **Problem:** Lifecycle + Android-Spracherkennung + Listen-/Processing-State + Watchdog vermischt.
- **Vorschlag:** `SpeechSession` für Recognizer + Watchdog; Activity bleibt Lifecycle-Host. **Umfang:** mittel.
- **✅ Umgesetzt:** `SpeechSession` (Recognizer + Watchdog); `MainActivity` bleibt Lifecycle-Host.

### `action/selfie/SelfieController.java` (515 LOC) / `action/hold/HoldController.java` (476)
- **Problem:** Selfie mischt Capture, Bildkomposition, lokalen Webserver, QR/WiFi-Payload;
  Hold mischt Animation, Voice-Listen, Touch-Sensor, Eskalation.
- **Vorschlag:** Capture/Upload/Storage bzw. Animation/Voice/Touch in Kollaboratoren trennen. **Umfang:** mittel.
- **✅ Umgesetzt:** Selfie — lokaler Bild-Server/Upload in `SelfieShareServer` (kapselt `LocalImageServer`);
  Hold — lokalisierte Sprüche in `HoldQuotes`.

### UI in Code (`DanceLibraryView`, `RaffleJoinView`, `NavigationView`)
- **Problem:** Layout programmatisch statt XML; inkonsistent zu XML-basierten Views.
- **Vorschlag:** Nach Aktivierung von ViewBinding schrittweise nach XML ziehen, Logik in Controller. **Umfang:** mittel.
- **✅ Umgesetzt:** ViewBinding aktiviert; `DanceLibraryView`, `RaffleJoinView`, `NavigationView` darauf umgestellt.

### `action/Action.java`
- **Problem:** `HistoryManager` via Setter (`setHistoryManager`) feld-injiziert — späte Bindung,
  fehleranfällig.
- **Vorschlag:** Konstruktor-Übergabe, einheitlich für alle Actions. **Umfang:** mittel (alle Actions betroffen).
- **✅ Umgesetzt:** `HistoryManager` via Konstruktor statt Setter.

## Querschnitt
- UI programmatisch statt XML/ViewBinding (Voraussetzung: `viewBinding true`).
- Statischer mutierbarer State in `OpenAIService` (cachedToken, Circuit-Breaker) — pro Instanz isolieren.
- Single `:app`-Modul — mittelfristig `:core` + Feature-Module evaluieren (nur Entscheidung).

## Umsetzungsreihenfolge (empfohlen) — abgearbeitet
1. ✅ Fundament: ViewBinding aktivieren.
2. ✅ Risikoarme Infrastruktur-Splits: OpenAIService-Transport/Streaming, geteilte Instanz,
   AnimationGenerator-Datenklassen.
3. ✅ Danach größere, hardware-zu-testende Umbauten (AdminView-Panels, NavigationManager) einzeln.

## Multi-Modul-Schnitt (Evaluation, keine Umsetzung)

**Frage:** Lohnt es, das single `:app`-Modul in `:core` + `:feature-*`-Module aufzuteilen?

**Befund / Abwägung:**
- **Build-Zeit:** Bei ~16,5k LOC ist die Compile-Zeit unkritisch (Debug-Build ~10–30 s, meist
  inkrementell). Modul-Parallelisierung bringt hier kaum messbaren Gewinn; der Mehraufwand
  (Gradle-Setup, `api`/`implementation`-Pflege) überwiegt vorerst.
- **QiSDK-Kopplung:** Fast jedes Feature-Package (`action/*`) hängt direkt an `com.aldebaran.qi.sdk`
  (QiContext, Animate, Say, GoTo, Touch). Eine saubere Feature-Modul-Grenze müsste den QiContext
  durchreichen; aktuell ist er über `RobotContext`/Controller-Singletons querverdrahtet. Module
  würden diese Kopplung sichtbar machen, aber nicht von allein auflösen.
- **Reihenfolge:** Die God-Classes (AdminView, NavigationManager) und der statische Controller-/
  Singleton-State (`SelfieController`, `HoldController`, `NavigationManager`, `OpenAIService`)
  müssten ZUERST entflochten werden. Module über ungeschnittene God-Classes zu legen, zementiert
  die Kopplung nur an Modulgrenzen.
- **Sinnvoller Erststich (falls überhaupt):** ein dünnes `:core` für reine Infrastruktur ohne
  Feature-/QiSDK-Logik — `openai/*`, `net`, `stats`, `config`, `debug`, `lang`. Das ist die einzige
  Schicht mit klarer, azyklischer Abhängigkeitsrichtung (Feature → core, nie umgekehrt).

**Empfehlung:** Multi-Modul-Schnitt **zurückstellen**. Zuerst die in-Modul-Refactorings abschließen
(God-Classes aufteilen, statischen State pro Instanz isolieren, Package-Schnitt `core` vs `feature`
sauber ziehen — Subtask „Package-Schnitt aufräumen"). Erst wenn die Abhängigkeitsrichtung im
Package-Schnitt stabil azyklisch ist, lohnt als optionaler Folgeschritt ein `:core`-Modul; eigene
`:feature-*`-Module bringen bei der aktuellen QiSDK-Querkopplung kurzfristig zu wenig ROI.

## Package-Schnitt (core vs. feature) — Befund

**Stand:** Infrastruktur liegt bereits in eigenen Top-Level-Packages (`openai`, `net`, `stats`,
`config`, `debug`, `lang`, `perception`), getrennt von den Feature-Packages (`action/*`). Die
strukturelle Trennung ist also weitgehend vorhanden.

**Problem — Abhängigkeiten in falscher Richtung (Infra → Feature, erzeugt Zyklen):**
- `openai/OpenAIService` → `action.Action` (Action-Liste für Prompt-Aufbau) und
  `action.raffle.RaffleRepository`/`RaffleEntity`/`RaffleStatus` (Raffle-Hinweis im System-Prompt).
- `openai/history/HistoryEntry` + `HistoryManager` → `action.Action` (History-Einträge referenzieren
  die erzeugende Action).
- `lang/SpeechManager` → `action.audio.AudioCoordinator`, `action.dialogue.DialogueController`,
  `action.thinking.ThinkingController` (Sprachausgabe koordiniert Audio-Ducking/Dialog/Thinking).

**Vorschlag (Reihenfolge nach Risiko):**
1. **Querschnitt-Koordinatoren verschieben (geringes Risiko, hoher Gewinn):**
   `AudioCoordinator`, `DialogueController`, `ThinkingController` sind keine Features, sondern
   cross-cutting Laufzeit-Koordination. In ein `core`-Package (z. B. `coordination`) ziehen → die
   `lang → action`-Inversion verschwindet. (Reines Verschieben + Import-Update.)
2. **OpenAIService ↔ Raffle entkoppeln (mittleres Risiko):** Raffle-Prompt-Hinweis nicht aus
   `OpenAIService` heraus aus `RaffleRepository` ziehen, sondern per Provider-Interface
   (`SystemPromptHint`) injizieren; Action-Liste ebenfalls als schmales Interface statt `List<Action>`.
3. **History ↔ Action entkoppeln (Domänen-Kopplung):** `HistoryEntry`/`HistoryManager` referenzieren
   `Action` nur, um den Urheber zu markieren — ein schmales Interface oder eine String-ID genügt.

**Bewertung:** Schritt 1 ist ein sauberer, isolierter Verschiebe-Commit. Schritte 2–3 berühren den
Prompt-/History-Pfad und sollten einzeln mit Hardware-Test gemacht werden. Zurückgestellt als
bewusste, dokumentierte Entscheidung (kein Blind-Umbau des Prompt-Pfads ohne Gerätetest).
