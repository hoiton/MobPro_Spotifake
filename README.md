# Spotifake
## Beschreibung
Spotifake ist ein lokaler Musik-Player, der es ermöglicht, Musikdateien direkt auf deinem Gerät abzuspielen.
Du kannst Tracks zu selbst erstellten Playlists hinzufügen und diese nach Belieben abspielen.

### LastFM API
Diese App verwendet die LastFM API, um Cover-Art für deine Musik zu erhalten.
Im build.gradle (Module: app) wird der API-Schlüssel `f80e715081b881a0f82faa5b2b84ebae` verwendet.

Falls dieser nicht funktionieren sollte, kannst du ihn durch einen eigenen ersetzen.
Um einen LastFM API-Schlüssel zu erhalten, musst du ein Konto auf [lastFM](https://www.last.fm/api/account/create) erstellen
und dann einen API-Account auf [lastFM API](https://www.last.fm/api/) anfordern.

## Eingesetzte Technische Anforderungen
- Kommunikation über HTTP (LastFM API -> `LastFmService`)
- Verwendung eines sinnvollen Foreground-Services (`AudioPlayerService` für Media-Playback)
- Lokale Persistenz mittels Datenbank (`MusicDatabase`)
- Verwendung von einfacher Nebenläufigkeit (für Datenbankzugriffe)
- Verwendung von Dependency Injection
- Verwendung von App Bar (Bottom Bar)