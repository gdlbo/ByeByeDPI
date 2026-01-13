package io.github.dovecoteescapee.byedpi.utility

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.InetSocketAddress
import java.net.Proxy
import java.util.concurrent.TimeUnit

class SiteCheckUtils(
    private val proxyIp: String,
    private val proxyPort: Int
) {

    private fun createClient(timeout: Long) = OkHttpClient.Builder()
        .proxy(Proxy(Proxy.Type.SOCKS, InetSocketAddress(proxyIp, proxyPort)))
        .connectionPool(okhttp3.ConnectionPool(0, 1, TimeUnit.NANOSECONDS))
        .connectTimeout(timeout, TimeUnit.SECONDS)
        .readTimeout(timeout, TimeUnit.SECONDS)
        .writeTimeout(timeout, TimeUnit.SECONDS)
        .callTimeout(timeout, TimeUnit.SECONDS)
        .followSslRedirects(true)
        .followRedirects(true)
        .build()

    suspend fun checkSitesAsync(
        sites: List<String>,
        requestsCount: Int,
        requestTimeout: Long,
        fullLog: Boolean,
        onSiteChecked: ((String, Int, Int) -> Unit)? = null
    ): List<Pair<String, Int>> {
        return withContext(Dispatchers.IO) {
            val client = createClient(requestTimeout)
            sites.map { site ->
                async {
                    val successCount = checkSiteAccess(client, site, requestsCount)
                    if (fullLog) {
                        onSiteChecked?.invoke(site, successCount, requestsCount)
                    }
                    site to successCount
                }
            }.awaitAll()
        }
    }

    private suspend fun checkSiteAccess(
        client: OkHttpClient,
        site: String,
        requestsCount: Int
    ): Int = withContext(Dispatchers.IO) {
        var responseCount = 0

        val formattedUrl = if (site.startsWith("http://") || site.startsWith("https://")) site
        else "https://$site"
        
        val httpUrl = formattedUrl.toHttpUrlOrNull()
        if (httpUrl == null) {
            Log.e("SiteChecker", "Invalid URL: $formattedUrl")
            return@withContext 0
        }

        repeat(requestsCount) { attempt ->
            Log.i("SiteChecker", "Attempt ${attempt + 1}/$requestsCount for $site")

            try {
                val request = Request.Builder().url(httpUrl).build()
                client.newCall(request).execute().use { response ->
                    val body = response.body
                    val declaredLength = body?.contentLength() ?: -1L
                    // Use a small buffer to check if we can read anything, instead of loading everything
                    val source = body?.source()
                    val actualLength = if (source != null) {
                        if (declaredLength > 0) {
                            source.request(declaredLength)
                            source.buffer.size
                        } else {
                            // If length is unknown, just try to read a bit
                            source.request(1024)
                            source.buffer.size
                        }
                    } else 0L
                    
                    val responseCode = response.code

                    if (response.isSuccessful || (declaredLength <= 0 || actualLength >= declaredLength)) {
                        Log.i("SiteChecker", "Response for $site: $responseCode, Declared: $declaredLength, Actual: $actualLength")
                        responseCount++
                    } else {
                        Log.w("SiteChecker", "Block detected for $site, Declared: $declaredLength, Actual: $actualLength")
                    }
                }
            } catch (e: Exception) {
                Log.e("SiteChecker", "Error accessing $site: ${e.message}")
            }
        }

        responseCount
    }
}
