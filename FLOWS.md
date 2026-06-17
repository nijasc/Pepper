# Pepper – Kernabläufe

Visualisierung der zentralen Abläufe der App als Mermaid-Diagramme, ausgerichtet an
der Code-Struktur unter `app/src/main/java/com/buhlergroup/pepper/`. Dieses Dokument
gehört zur Entwickler-Dokumentation und wird von Pepper **nicht** vorgelesen.

## Inhalt

- [Boot / Startup](#boot--startup)
- [Sprache → Aktions-Dispatch](#sprache--aktions-dispatch)
- [Navigation / Raumscan](#navigation--raumscan)
- [Attract-Modus](#attract-modus)
- [Tanz-Ablauf](#tanz-ablauf)
- [Hold my beer (Zustandsautomat)](#hold-my-beer-zustandsautomat)
- [OpenAI-Gespräch & Sprachwechsel](#openai-gespräch--sprachwechsel)

## Boot / Startup

Vom Geräte-Boot bis betriebsbereit. Beteiligt: `boot/BootReceiver`,
`PepperApplication`, `MainActivity`.

```mermaid
flowchart TD
    Boot["Gerät bootet (ACTION_BOOT_COMPLETED)"] --> BR[BootReceiver]
    BR -->|startActivity MainActivity| Proc[Prozessstart]
    Proc --> App["PepperApplication.onCreate: Startzeit, DebugLog.init, Crash-Restart-Handler"]
    App --> MA[MainActivity.onCreate]
    MA --> Views["Views und Controller anhängen: Admin, Selfie, Navigation, Dance, Hold, DebugOverlay u.a."]
    Views --> Speech["initSpeech und Speech-Watchdog starten"]
    Speech --> Purge["Abgelaufene Raffle- und Selfie-Daten löschen"]
    Purge --> Reg[QiSDK.register]
    Reg --> Focus{onRobotFocusGained}
    Focus -->|Fokus erhalten| FG["OpenAI-Token, RobotContext, BackgroundMovement halten, NavigationManager, FollowController"]
    FG --> Init["LanguageManager (Default Deutsch), SpeechManager, HistoryManager, AdminController, ActionHandler"]
    Init --> Pending{said vorhanden}
    Pending -->|ja| Handle[ActionHandler.handleInput]
    Pending -->|nein| Listen[listenToSpeech]
    Handle --> Listen
    Focus -->|verweigert| Refused[onRobotFocusRefused]
```

## Sprache → Aktions-Dispatch

Vom erkannten Sprachbefehl bis zur Ausführung einer Action, inklusive Offline-
und Fallback-Pfaden. Beteiligt: `MainActivity`, `action/ActionHandler`,
`action/IntentEngine`, `action/KeywordFallback`, `openai/OpenAIService`.

```mermaid
flowchart TD
    Mic["Spracherkennung (RecognizerIntent)"] --> Result["onActivityResult: said"]
    Result --> Handle["ActionHandler.handleInput(said)"]
    Handle --> Empty{leer}
    Empty -->|ja| End1[Ende]
    Empty -->|nein| Online{online}
    Online -->|nein| KwOff[KeywordFallback.match]
    KwOff -->|Treffer| RunKw[runAction]
    KwOff -->|kein Treffer| SayOff[Offline-Ansage]
    Online -->|ja| Think[ThinkingController.start]
    Think --> Combined["handleCombined: OpenAI-Streaming mit Routing"]
    Combined --> Marker{"Marker [[action:NAME]]"}
    Marker -->|Action| Exec["Action.execute + Stats"]
    Marker -->|nur Text| Say["SpeechManager.say + History"]
    Combined -->|NETWORK_ERROR nach Retries| KwErr[KeywordFallback oder Offline-Ansage]
    Combined -->|NOT_HANDLED| Legacy["handleLegacy: IntentEngine.getIntent"]
    Legacy -->|Intent| Exec
    Legacy -->|kein Intent| KwLegacy["KeywordFallback oder 'nicht verstanden'"]
    Exec --> Stop[ThinkingController.stop]
    Say --> Stop
```

## Navigation / Raumscan

Raum-Scan, Lokalisierung und Fahrt zu Wegpunkten. `NavigationManager` ist Fassade und
delegiert an die Kollaboratoren `RoomScanner` (Scan/Snapshot), `RobotLocalizer`
(Mapping/Localization) und `RobotGuide` (GoTo/Stopp-Listener); Karten-Rendering in
`NavMapRenderer`. Beteiligt: `action/navigation/NavigationManager` (+ `NavigationController`,
`NavigationView`, `data/`).

```mermaid
flowchart TD
    subgraph Scan["Raum-Scan (RoomScanner)"]
        S1["startScan: LocalizeAndMap, Fähigkeiten halten"] --> S2["Operator dreht Pepper von Hand"]
        S2 --> S3["Snapshot-Loop: Live-Karte via NavMapRenderer (captureSnapshot bei 'Position erfassen')"]
        S3 --> S4["Stopp per STOP-Button oder Sprache ('stopp'/'fertig')"]
        S4 --> S5["stopAndSaveScan: dumpMap, serialize, Datei + DB"]
    end
    subgraph Loc["Lokalisierung (RobotLocalizer)"]
        L1["localize(scan): Karte laden, Localize starten"] --> L2{LocalizationStatus}
        L2 -->|LOCALIZED| L3["localized = true"]
        L2 -->|Timeout 40 s| L4["Abbruch + Fehlermeldung"]
        L3 -->|Orientierung verloren| L5["handleLocalizationLost: anhalten, neu orientieren"]
    end
    subgraph Drive["Wegpunkte (RobotGuide)"]
        D1["saveWaypoint: Pose in Karte speichern"]
        D2["goToWaypoint / guideToWaypoint: GoTo zum Wegpunkt"]
        D3["guide: Stopp-Listener auf 'stopp'"]
    end
    S5 --> L1
    L3 --> D1
    L3 --> D2
    D2 --> D3
```

## Attract-Modus

Leerlaufverhalten. Der Watchdog in `MainActivity` ruft zyklisch
`AttractController.tick(...)`.

```mermaid
stateDiagram-v2
    [*] --> Idle
    Idle --> Attract: aktiv und Leerlauf und kein Overlay und frei
    Attract --> Idle: Interaktion oder Overlay oder beschäftigt oder Stop
    state Attract {
        [*] --> Roaming
        Roaming --> Greeting: Person in 1.5 m Nähe
        Greeting --> Roaming: nach Begrüßung, Intervall greetSeconds
        Roaming --> Roaming: driveRoamStep
    }
```

## Tanz-Ablauf

Tänze werden in der Admin-Bibliothek erzeugt (iTunes + LLM-Choreografie) und beim
Sprachbefehl nur abgespielt. Beteiligt: `action/dance/DanceRepository`,
`action/dance/itunes/ITunesSearch`, `action/dynamicanim/AnimationGenerator`
(Choreografie; Song-Research/Planning in `SongResearcher`/`SongPlan`/`SongResearch`,
gemeinsame Generator-Basis `GeneratorBase`), `action/dance/DanceAction`.

```mermaid
flowchart TD
    subgraph Create["Erstellen (Admin-Tanzbibliothek)"]
        C1["getOrCreate(query): planSong für Mood und Startzeit"] --> C2["ITunesSearch.search: Titel, Preview-URL, Dauer"]
        C2 --> C3{in DB vorhanden}
        C3 -->|ja, Datei existiert| C4[wiederverwenden]
        C3 -->|nein| C5["generateValidatedDance: LLM-Choreografie"]
        C5 --> C6["qianim speichern + Preview-Audio cachen (m4a) + DB-Eintrag"]
    end
    subgraph Seed["Built-in"]
        SB["ensureBuiltInDances: Hula, Six Seven"]
    end
    subgraph Play["Abspielen (Sprachbefehl 'tanze')"]
        P1[DanceAction.execute] --> P2["pickFromLibrary: Favoriten, Namenstreffer oder Zufall"]
        P2 --> P3{Tanz gefunden}
        P3 -->|ja| P4["preparePlayback: lokaler Cache oder iTunes-Preview"]
        P4 --> P5["playDance: qianim-Animation + Audio (5 bis 35 s)"]
        P5 -->|Fehler| P6
        P3 -->|Bibliothek leer| P6["playFallback: 'Six... seven!' + six_seven-Animation"]
    end
```

## Hold my beer (Zustandsautomat)

Zustandsautomat der Hold-Session. Beteiligt: `action/hold/HoldController`.
Aktueller Stand: 15 s Wartezeit, ruhige Halte-Hand (kein Pose-Loop), 5-Sekunden-
Countdown vor der Rückkehr.

```mermaid
stateDiagram-v2
    [*] --> Idle
    Idle --> Accept: requestHold, Fähigkeiten halten
    Accept --> WaitForObject: Arm heben, Touch-Sensor
    WaitForObject --> NoObject: 15 s ohne Auflegen
    NoObject --> Ende: hold_release
    WaitForObject --> Holding: bestätigt per Touch oder Tablet, Hand schließen
    Holding --> Holding: Eskalationen 60/180/300 s, Overtime ab 10 min, ruhige Hand
    Holding --> Countdown: stopp oder STOP
    Countdown --> Release: 5-Sekunden-Countdown
    Release --> Bye: hold_release, Verabschiedung, Zähler
    Bye --> Ende
    Ende --> [*]
```

## OpenAI-Gespräch & Sprachwechsel

Freies Gespräch via Streaming und Sprachwechsel (automatisch über Marker oder per
Befehl). `OpenAIService` ist Fassade über `OpenAiHttpClient` (Transport),
`OpenAiStreamParser`/`OpenAiResponse` (SSE), `OpenAiCircuitBreaker` und `OpenAiTokenProvider`.
Beteiligt: `openai/OpenAIService`, `openai/history/HistoryManager`
(max. 10 Gesprächseinträge), `lang/LanguageManager`, `action/lang/ChangeLanguageAction`.

```mermaid
flowchart TD
    subgraph Conv["Gespräch (OpenAIService.getResponseStreaming)"]
        O1["History.toInput + System-Prompt aus instructions.md, Actions, Marker, Emotion, Raffle-Hinweis"] --> O2{Circuit offen}
        O2 -->|ja| O3["schnell scheitern, Fallback"]
        O2 -->|nein| O4[SSE-Stream lesen]
        O4 --> O5["Marker [[lang:CODE]] und [[action:NAME]] auslesen"]
        O5 --> O6["Sätze extrahieren, SpeechManager.say je Satz mit Sprach-Tag"]
        O6 --> O7{Erfolg}
        O7 -->|ja| O8["recordSuccess, History aktualisieren"]
        O7 -->|Fehler| O9["recordFailure, nach 3x 30 s Cooldown"]
    end
    subgraph Lang["Sprachwechsel"]
        LA["automatisch: Modell setzt [[lang:CODE]], Sprach-Tag je Satz"]
        LC["per Befehl: ChangeLanguageAction.parseLang, LanguageManager.applyLanguage"]
        LC --> LD["RecognizerIntent-Sprache aktualisieren + Bestätigung sprechen"]
    end
```
