# Pepper – Kernabläufe

Visualisierung der zentralen Abläufe der App als Mermaid-Diagramme. Beschrieben wird
das **Verhalten** (was passiert), nicht der Code-Aufbau. Dieses Dokument gehört zur
Entwickler-/Bediener-Doku und wird von Pepper **nicht** vorgelesen.

## Inhalt

- [Start](#start)
- [Sprache → Aktions-Dispatch](#sprache--aktions-dispatch)
- [Modellauswahl & Anbieter-Rückfall](#modellauswahl--anbieter-rückfall)
- [Profil & Wissensbasis](#profil--wissensbasis)
- [Gespräch & Sprachwechsel](#gespräch--sprachwechsel)
- [Navigation / Raumscan](#navigation--raumscan)
- [Lotse-Modus](#lotse-modus)
- [Attract-Modus](#attract-modus)
- [Tanz-Ablauf](#tanz-ablauf)
- [Selfie](#selfie)
- [Verlosungs-Beitritt](#verlosungs-beitritt)
- [Hold my beer (Zustandsautomat)](#hold-my-beer-zustandsautomat)

## Start

Vom Geräte-Boot bis betriebsbereit, inklusive automatischem Neustart nach einem Absturz.

```mermaid
flowchart TD
    Boot["Gerät bootet"] -->|optionaler Autostart| Proc[App startet]
    Crash["Unerwarteter Fehler"] --> Persist["Fehler protokollieren"]
    Persist --> Restart["Nach kurzer Verzögerung neu starten"]
    Restart --> Proc
    Proc --> Init["App initialisieren: Startzeit, Debug-Log, OpenAI-Key aus env übernehmen, Profil seeden"]
    Init --> Focus{Roboter-Fokus erhalten}
    Focus -->|ja| Ready["Sprache (Start Deutsch), Spracherkennung + Watchdog, Attract bereit"]
    Focus -->|nein| Refused["Wartet auf Fokus"]
    Ready --> CrashShown{Letzte Absturzmeldung vorhanden}
    CrashShown -->|ja| ShowCrash["Einmalig im Debug-Log zeigen"]
    CrashShown -->|nein| Listen[Auf Sprache hören]
    ShowCrash --> Listen
```

## Sprache → Aktions-Dispatch

Vom erkannten Sprachbefehl bis zur Ausführung, inklusive der mehrstufigen Rückfälle
(Klassifizierung, Offline-Schlüsselwörter).

```mermaid
flowchart TD
    Mic["Spracherkennung (Google)"] --> Result[Erkannter Text]
    Result --> Empty{leer}
    Empty -->|ja| End1[Ende]
    Empty -->|nein| Online{online}
    Online -->|nein| KwOff[Offline-Schlüsselwort-Abgleich]
    KwOff -->|Treffer| Exec["Funktion ausführen"]
    KwOff -->|kein Treffer| SayOff["Ansage: keine Verbindung"]
    Online -->|ja| Think[Denkpause starten]
    Think --> Combined["Kombinierter Turn: Streaming mit Marker-Routing"]
    Combined --> Marker{"Marker [[action:NAME]]"}
    Marker -->|spezialisierte Funktion| Exec
    Marker -->|nur Text / SayAction| Say["Satzweise sprechen + Historie"]
    Combined -->|Netzwerkfehler nach Wiederholung| KwErr[Klassifizierung als Rückfall]
    KwErr -->|Funktion erkannt| Exec
    KwErr -->|nichts| KwLast["Offline-Schlüsselwörter oder 'nicht verstanden'"]
    Exec --> Stop[Denkpause stoppen]
    Say --> Stop
```

## Modellauswahl & Anbieter-Rückfall

Wie pro Aufgabe Anbieter und Modell bestimmt werden und was bei Modell- oder
Netzwerkfehlern passiert. Anbieter, Modell und API-Keys sind je Aufgabe im
Admin-Bereich einstellbar.

```mermaid
flowchart TD
    Need["Aufgabe (Conversation / Documentation / Generation / Classification / Rewrite)"] --> Pick["Anbieter + Modell aus Einstellungen (Standard: OpenAI)"]
    Pick --> Breaker{Circuit offen}
    Breaker -->|ja, nach 3 Fehlern für 30 s| FailFast["Schnell scheitern → Rückfall"]
    Breaker -->|nein| Call["Anfrage an Anbieter senden"]
    Call --> Resp{Antwort}
    Resp -->|OK| Done["Antwort verwenden, Erfolg vermerken"]
    Resp -->|Modellfehler 400/403/404| Next["Nächstes Modell desselben Anbieters"]
    Next --> Call
    Resp -->|Netzwerk/Timeout/Serverfehler| Fail["Fehler vermerken → Offline-Rückfall"]
```

## Profil & Wissensbasis

Wie das aktive Profil die Persona und eine optionale Wissensbasis in den Systemprompt
einspeist. Profile werden im Admin-Bereich gepflegt und aktiviert.

```mermaid
flowchart TD
    subgraph Admin["Admin: Profil pflegen"]
        A1["Profil anlegen / klonen"] --> A2["Grundinstruktionen bearbeiten"]
        A2 --> A3["Ressourcen hinzufügen: Text, Markdown, URL, PDF"]
        A3 --> A4["Zusammenfassung neu erstellen (verdichtet die Ressourcen)"]
        A4 --> A5["Profil aktivieren"]
    end
    subgraph Runtime["Bei jeder Anfrage"]
        R1["Aktives Profil laden"] --> R2{Grundinstruktionen gesetzt}
        R2 -->|ja| R3["diese als Persona verwenden"]
        R2 -->|nein| R4["Basis-Persona (instructions.md)"]
        R3 --> R5{Wissensbasis vorhanden}
        R4 --> R5
        R5 -->|ja| R6["Abschnitt 'Wissensbasis' anhängen"]
        R5 -->|nein| R7["Systemprompt fertigstellen"]
        R6 --> R7
    end
    A5 -.-> R1
```

## Gespräch & Sprachwechsel

Freies Gespräch via Streaming und Sprachwechsel (automatisch über Marker oder per
Befehl). Die Historie umfasst die letzten 10 Gesprächseinträge.

```mermaid
flowchart TD
    subgraph Conv["Gespräch (Streaming)"]
        O1["Systemprompt bauen: Persona + Wissensbasis + Funktionsliste + Marker-Anweisung + Stimmung + Verlosungs-Hinweis"] --> O2[Stream lesen]
        O2 --> O3["Marker [[lang:CODE]] und [[action:NAME]] auslesen"]
        O3 --> O4["Sätze extrahieren, je Satz mit passender Stimme sprechen"]
        O4 --> O5{Erfolg}
        O5 -->|ja| O6["Historie aktualisieren"]
        O5 -->|Fehler| O7[Rückfall]
    end
    subgraph Lang["Sprachwechsel"]
        LA["automatisch: Modell setzt [[lang:CODE]] → Stimme je Satz"]
        LC["per Befehl: gewünschte Sprache erkennen"]
        LC --> LD["Erkennungssprache umstellen + Bestätigung sprechen"]
    end
```

## Navigation / Raumscan

Raum-Scan, Lokalisierung und Fahrt zu Wegpunkten. Pepper fährt beim Scannen nicht
autonom durch den Raum, sondern dreht sich nur an Ort; zwischen den Positionen wird er
von Hand geschoben.

```mermaid
flowchart TD
    subgraph Scan["Raum-Scan"]
        S1["Scan starten: Mapping beginnt, Live-Karte im Vollbild"] --> S2["Pepper dreht sich selbst 4× 90° (erste Position)"]
        S2 --> S3["Operator schiebt Pepper, dann 'Position erfassen' → erneut 4× 90°"]
        S3 --> S4["Stopp per STOP-Button oder Sprache ('stopp'/'fertig')"]
        S4 --> S5["Karte benennen und speichern"]
    end
    subgraph Loc["Lokalisierung"]
        L1["Scan aktivieren: Karte laden, Drehung 8× 45°"] --> L2{Status}
        L2 -->|lokalisiert| L3["bereit für Wegpunkte und Fahrten"]
        L2 -->|Zeitlimit (Standard 40 s)| L4["Abbruch + Hinweis"]
        L3 -->|Orientierung verloren| L5["anhalten + Hinweis"]
    end
    subgraph Drive["Wegpunkte"]
        D1["Wegpunkt speichern (nur wenn lokalisiert), optional Fotostand"]
        D2["Hinfahren: autonome Fahrt mit Hindernisvermeidung"]
    end
    S5 --> L1
    L3 --> D1
    L3 --> D2
```

## Lotse-Modus

Besucher lassen sich per Sprache zu einem Wegpunkt führen. Voraussetzung ist ein
aktiver, lokalisierter Scan.

```mermaid
flowchart TD
    G1["Sprachbefehl: 'bring mich zu …'"] --> G2{lokalisiert und Scan aktiv}
    G2 -->|nein| G3["Hinweis: Betreuer muss zuerst einen Scan aktivieren"]
    G2 -->|ja| G4["Ziel unscharf gegen Wegpunkte abgleichen (de/en)"]
    G4 -->|kein Treffer| G5["verfügbare Wegpunkte aufzählen"]
    G4 -->|Treffer| G6["'Folge mir, ich bringe dich zu …' + autonome Fahrt"]
    G6 --> G7{Ergebnis}
    G7 -->|angekommen| G8["Ankunft melden"]
    G7 -->|'stopp' unterwegs| G9["anhalten"]
    G7 -->|Orientierung verloren / Zeitlimit| G10["anhalten + Hinweis"]
```

## Attract-Modus

Leerlaufverhalten. Standardmässig aktiv, im Admin-Bereich umschaltbar.

```mermaid
stateDiagram-v2
    [*] --> Idle
    Idle --> Attract: aktiv und Leerlauf (Standard 2 min) und kein Overlay und frei
    Attract --> Idle: Interaktion oder Overlay oder beschäftigt oder ausgeschaltet
    state Attract {
        [*] --> Roaming
        Roaming --> Greeting: Person in rund 1,5 m Nähe
        Greeting --> Roaming: nach Begrüssung
        Roaming --> Roaming: langsam weiterfahren, bei Hindernis ausweichen
    }
```

## Tanz-Ablauf

Tänze werden in der Admin-Tanzbibliothek erzeugt (iTunes + Audio-Analyse + generierte
Choreografie) und beim Sprachbefehl nur abgespielt.

```mermaid
flowchart TD
    subgraph Create["Erstellen (Admin-Tanzbibliothek)"]
        C1["Song planen (Stimmung, Einstiegspunkt)"] --> C2["iTunes-Suche: Titel + 30-s-Vorschau"]
        C2 --> C3{schon gespeichert}
        C3 -->|ja| C4[wiederverwenden]
        C3 -->|nein| C5["Vorschau analysieren: Tempo (BPM) und markanter Teil"]
        C5 --> C6["Choreografie generieren (an Beat gekoppelt) + validieren + glätten"]
        C6 --> C7["Animation + Vorschau-Audio lokal speichern"]
    end
    subgraph Play["Abspielen (Sprachbefehl 'tanze')"]
        P1["Tanz wählen: Namenstreffer, Standard, Favorit oder Zufall"] --> P2{Tanz vorhanden}
        P2 -->|ja| P3["Audio ab markantem Teil + Choreografie (5 bis 35 s)"]
        P3 -->|Fehler| P4
        P2 -->|Bibliothek leer| P4["Eigen-Tanz: 'Six… seven!'"]
    end
```

## Selfie

Aufnahme mit Vorschau, optionaler Wiederholung, lokalem Download per QR-Code und
optionaler Anbindung an eine laufende Verlosung.

```mermaid
flowchart TD
    F1["Sprachbefehl: 'Selfie'"] --> F2{externe Kamera aktiv und erreichbar}
    F2 -->|ja| F3["auf 'Start' warten, herunterzählen, externe Kamera auslösen"]
    F2 -->|nein| F4["herunterzählen, eigene Kamera auslösen"]
    F3 --> F5["Motiv einfügen, Vorschau zeigen"]
    F4 --> F5
    F5 --> F6{Entscheidung}
    F6 -->|Nochmal (max. 3 Aufnahmen)| F2
    F6 -->|Speichern / Zeitablauf| F7["lokal speichern"]
    F7 --> F8["token-geschützten Webserver starten, QR-Code(s) zeigen"]
    F8 --> F9{aktive Verlosung}
    F9 -->|ja| F10["Beitritt anbieten"]
    F9 -->|nein| F11[Ende]
```

## Verlosungs-Beitritt

Schritt-für-Schritt-Erfassung über das Tablet mit Validierung, Duplikat- und
Statusprüfung.

```mermaid
flowchart TD
    J1[Beitritt starten] --> J2["Name erfassen"]
    J2 --> J3["E-Mail erfassen + Format prüfen"]
    J3 --> J3d{E-Mail schon eingetragen}
    J3d -->|ja| J3
    J3d -->|nein| J4{Telefon erforderlich}
    J4 -->|ja| J5["Telefon erfassen + Duplikat prüfen"]
    J4 -->|nein| J6
    J5 --> J6{Selfie erforderlich}
    J6 -->|ja| J7["Selfie-Flow, Selfie verknüpfen"]
    J6 -->|nein| J8
    J7 --> J8["Absenden: erneut Status + Duplikate prüfen"]
    J8 --> J9{Verlosung noch aktiv}
    J9 -->|ja| J10["Eintrag speichern: 'Du bist dabei!'"]
    J9 -->|nein| J11["Hinweis: Verlosung nicht mehr aktiv"]
```

## Hold my beer (Zustandsautomat)

Zustandsautomat der Hold-Session: 15 s Wartezeit, ruhige Halte-Hand, Eskalationen,
5-Sekunden-Countdown vor der Rückgabe.

```mermaid
stateDiagram-v2
    [*] --> Idle
    Idle --> Accept: 'Hold my beer'
    Accept --> WaitForObject: Arm heben, offene Hand
    WaitForObject --> NoObject: 15 s ohne Auflegen
    NoObject --> Ende: Hand bleibt, Verabschiedung
    WaitForObject --> Holding: bestätigt per Berührungssensor oder Tablet → Hand schliessen
    Holding --> Holding: Sprüche nach 1/3/5 min, ab 10 min Aufforderung, Hand bleibt ruhig
    Holding --> Countdown: 'stopp' oder STOP-Button
    Countdown --> Release: 5-Sekunden-Countdown
    Release --> Bye: Hand öffnen, Verabschiedung, Zähler
    Bye --> Ende
    Ende --> [*]
```
