package ch.hslu.spotifake.ui.navigation

enum class SpotifakeScreens(val route: String) {
    Player("player"),
    Upload("upload"),
    Playlist("playlist"),
    Track("track/{playlistId}"), // parameterized route
    Library("library")           // add this for PlaylistsScreen
}