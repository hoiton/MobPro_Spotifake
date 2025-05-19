package ch.hslu.spotifake.ui.navigation

enum class SpotifakeScreens(val route: String) {
    Player("player"),
    Upload("upload"),
    Playlists("playlist"),
    Track("track/{playlistId}"),
    Library("library"),
}