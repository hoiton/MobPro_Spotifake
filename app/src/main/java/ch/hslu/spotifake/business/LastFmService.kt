package ch.hslu.spotifake.business

import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.Call
import okhttp3.Callback
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
import javax.inject.Inject
import javax.inject.Named
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class LastFmService @Inject constructor(
    private val client: OkHttpClient,
    @Named("LastFmApiKey") private val apiKey: String
) {

    suspend fun getLargestAlbumArtUrl(
        artist: String,
        track: String
    ): String? = suspendCancellableCoroutine { cont ->
        val url = HttpUrl.Builder()
            .scheme("https")
            .host("ws.audioscrobbler.com")
            .addPathSegment("2.0")
            .addQueryParameter("method", "track.getInfo")
            .addQueryParameter("api_key", apiKey)
            .addQueryParameter("artist", artist)
            .addQueryParameter("track", track)
            .addQueryParameter("format", "json")
            .build()

        val request = Request.Builder().url(url).build()
        val call = client.newCall(request)
        cont.invokeOnCancellation { call.cancel() }

        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) =
                cont.resumeWithException(e)

            override fun onResponse(call: Call, response: Response) {
                try {
                    if (!response.isSuccessful) {
                        cont.resume(null)
                        return
                    }
                    val body = response.body?.string().orEmpty()
                    val trackObj = JSONObject(body).optJSONObject("track")
                    val images = trackObj
                        ?.optJSONObject("album")
                        ?.optJSONArray("image")

                    val sizeOrder = listOf("small", "medium", "large", "extralarge", "mega")
                    var largestUrl: String? = null

                    if (images != null) {
                        for (size in sizeOrder.asReversed()) {
                            for (i in 0 until images.length()) {
                                val img = images.getJSONObject(i)
                                if (img.getString("size") == size) {
                                    val text = img.getString("#text")
                                    if (text.isNotBlank()) {
                                        largestUrl = text
                                        break
                                    }
                                }
                            }
                            if (largestUrl != null) break
                        }
                    }
                    cont.resume(largestUrl)
                } catch (e: Exception) {
                    cont.resumeWithException(e)
                }
            }
        })
    }
}
