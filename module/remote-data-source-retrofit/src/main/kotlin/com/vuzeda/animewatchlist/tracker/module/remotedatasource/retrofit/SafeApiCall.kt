package com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit

import com.vuzeda.animewatchlist.tracker.module.domain.DataError
import com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.service.ChiakiRequestException
import com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.service.MalEpisodeListRequestException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import retrofit2.HttpException
import java.io.IOException

private const val BASE_RETRY_DELAY_MS = 500L
private const val MAX_RETRY_ATTEMPTS = 3
private val RETRYABLE_HTTP_CODES = setOf(429, 502, 503, 504)

private fun exponentialBackoffMs(attempt: Int): Long = BASE_RETRY_DELAY_MS * (1L shl attempt)

@Suppress("TooGenericExceptionCaught")
internal suspend fun <T> safeApiCall(block: suspend () -> T): Result<T> {
    var attempt = 0
    while (true) {
        try {
            return Result.success(block())
        } catch (e: CancellationException) {
            throw e
        } catch (e: IOException) {
            return Result.failure(DataError.Network(throwable = e))
        } catch (e: HttpException) {
            if (e.code() !in RETRYABLE_HTTP_CODES || attempt >= MAX_RETRY_ATTEMPTS) {
                return Result.failure(mapHttpException(e) as Throwable)
            }
            delay(e.retryAfterMs() ?: exponentialBackoffMs(attempt))
            attempt++
        } catch (e: ChiakiRequestException) {
            if (e.statusCode !in RETRYABLE_HTTP_CODES || attempt >= MAX_RETRY_ATTEMPTS) {
                return Result.failure(mapStatusCodeException(e.statusCode, e) as Throwable)
            }
            delay(exponentialBackoffMs(attempt))
            attempt++
        } catch (e: MalEpisodeListRequestException) {
            if (e.statusCode !in RETRYABLE_HTTP_CODES || attempt >= MAX_RETRY_ATTEMPTS) {
                return Result.failure(mapStatusCodeException(e.statusCode, e) as Throwable)
            }
            delay(exponentialBackoffMs(attempt))
            attempt++
        } catch (e: Exception) {
            return Result.failure(DataError.Unknown(throwable = e))
        }
    }
}

private fun mapHttpException(e: HttpException): DataError = when (e.code()) {
    404 -> DataError.NotFound(errorMessage = e.message())
    429 -> DataError.RateLimited(retryAfterMs = e.retryAfterMs())
    else -> DataError.Network(throwable = e)
}

private fun mapStatusCodeException(statusCode: Int, throwable: Throwable): DataError = when (statusCode) {
    404 -> DataError.NotFound(errorMessage = throwable.message ?: "Not found")
    429 -> DataError.RateLimited(retryAfterMs = null)
    else -> DataError.Network(throwable = throwable)
}

private fun HttpException.retryAfterMs(): Long? =
    response()?.headers()?.get("Retry-After")?.toLongOrNull()?.let { it * 1_000L }
