# Pepper – Tutorials

> Schritt-für-Schritt-Anleitungen für Bediener und Entwickler. Diese Datei gehört
> zur Entwickler-/Bediener-Doku (wie die [`README.md`](README.md)) und wird von
> Pepper **nicht** vorgelesen.

## Inhalt

- [Raum einrichten: Scan & Wegpunkte per Tablet einlernen](#raum-einrichten-scan--wegpunkte-per-tablet-einlernen)

## Raum einrichten: Scan & Wegpunkte per Tablet einlernen

So bringst du Pepper bei, sich in einem Raum zu orientieren und gespeicherte
**Wegpunkte** (z. B. einen Fotostand) selbstständig anzufahren. Der Ablauf ist
bewusst einfach gehalten: **wenig Bewegung, schneller Scan, Wegpunkte werden per
Tablet eingelernt** (kein autonomes Herumfahren).

> **Wichtig zum Verständnis:** Pepper **bewegt sich beim Scan nicht von selbst**.
> Während des Mappings (`LocalizeAndMap`) kann die Fahr-Basis nicht autonom
> bewegt werden (ein paralleler `GoTo` scheitert mit „Move task not started“, und
> `Animate`/`.qianim` steuert nur Gelenke, nicht die Räder). Deshalb **drehst und
> schiebst du Pepper beim Scannen von Hand** – es gibt bewusst keine Bewegungs-/
> Jog-Tasten mehr. Erst wenn die Karte gespeichert und aktiviert ist, fährt Pepper
> über `GoTo` zuverlässig zu Wegpunkten.

### 1. Navigation öffnen

1. Unten links auf **Admin** tippen (oder „Admin“ sagen) und die **PIN** eingeben.
2. Im Kachelmenü **Navigation** wählen.

### 2. Raum scannen

1. Oben einen **Namen** für den Scan eingeben (z. B. „Empfang“).
2. **„Scan starten“** tippen. Pepper beginnt zu mappen und zeigt die **Live-Karte
   im Vollbild**, die laufend wächst.
3. **Drehe Pepper langsam von Hand einmal um die eigene Achse**, damit er den
   Umkreis erfasst. Für grössere oder verwinkelte Räume zusätzlich ein Stück
   hinschieben (um Ecken, in Nischen) und dort erneut drehen. An markanten Stellen
   **„Aktuelle Position erfassen“** tippen, damit die Live-Karte den Bereich sicher
   aufnimmt.
4. **Möglichst freie Fläche, kurzer Scan:** keine Personen im Sichtfeld, sonst
   landen sie als „Geister-Hindernisse“ in der Karte. Kontrolliere das laufend an
   der Live-Karte und scanne im Zweifel neu.
5. Zum Beenden entweder den grossen **STOP-Button** drücken **oder** „**Stopp**“
   (bzw. „Halt“, „Fertig“) sagen. Die Karte wird gespeichert.

### 3. Karte aktivieren (lokalisieren)

1. In der Liste **Raum-Scans** den gespeicherten Scan auf **„Aktivieren“** tippen.
2. Pepper lokalisiert sich in der Karte; der Status wechselt auf
   **„Lokalisiert in …“**. (Steht Pepper noch ungefähr am Scan-Startpunkt, klappt
   das am schnellsten.)

### 4. Wegpunkte einlernen

1. Schiebe/drehe Pepper **von Hand** zur gewünschten Stelle. Weil die Karte aktiv
   und lokalisiert ist, kennt Pepper dabei laufend seine Position.
2. Einen **Namen** für den Wegpunkt eingeben. Für einen Foto-Standort zusätzlich
   **„Fotostand“** ankreuzen – dann fährt Pepper z. B. beim Selfie automatisch
   dorthin.
3. **„Aktuelle Position speichern“** tippen. Wiederhole das für jede wichtige
   Stelle.

### 5. Wegpunkt anfahren

- Im Bereich **Wegpunkte** beim gewünschten Eintrag **„Hierhin fahren“** tippen –
  Pepper navigiert autonom dorthin (mit Hindernisvermeidung).
- Im normalen Betrieb kann ein Besucher das auch per Sprache auslösen, z. B.
  „**Bring mich zum Fotostand**“ (siehe [Lotse-Modus](README.md#lotse-modus)).

### Tipps

- **Schneller Scan:** Eine einzige langsame Drehung reicht meist. Je sauberer die
  Umgebung erfasst ist, desto zuverlässiger die spätere Lokalisierung.
- **Neu lokalisieren:** Verliert Pepper die Orientierung, stell ihn näher an einen
  bekannten Bereich und aktiviere den Scan erneut.
- **Mehrere Räume:** Pro Raum einen eigenen Scan anlegen und jeweils den passenden
  aktivieren.
