# Spotifake
## Description
Spotifake ist ein lokaler Musik-Player, der es ermöglicht, Musikdateien auf dem Gerät abzuspielen.
Tracks können eigens verwalteten Playlists hinzugefügt und aus diesen abgespielt werden.

### LastFM API
This app uses the LastFM API to get cover art for your music.
To get LastFM API working, the API key is read from the environment variable `LASTFM_API_KEY`.
To get the lastFM API key, you need to create an account on [lastFM](https://www.last.fm/api/account/create)
and then create an API key on [lastFM API](https://www.last.fm/api/).

## Eingesetzte Technische Anforderungen
- Kommunikation über HTTP (LastFM API -> `LastFmService`)
- Verwendung eines sinnvollen Foreground-Services (`AudioPlayerService` für Media-Playback)
- Lokale Persistenz mittels Datenbank (`MusicDatabase`)
- Verwendung von einfacher Nebenläufigkeit (für Datenbankzugriffe) 
- Verwendung von Dependency Injection
- Verwendung von App Bar (Bottom Bar)