package idv.fan.iisigroup.android.test.network

import okhttp3.Interceptor
import okhttp3.Response
import java.net.ConnectException
import java.net.SocketException
import java.net.UnknownHostException

class NetworkErrorInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        return try {
            chain.proceed(chain.request())
        } catch (e: UnknownHostException) {
            throw NetworkException(NETWORK_ERROR_MESSAGE, e)
        } catch (e: SocketException) {
            throw NetworkException(NETWORK_ERROR_MESSAGE, e)
        } catch (e: ConnectException) {
            throw NetworkException(NETWORK_ERROR_MESSAGE, e)
        }
    }

    companion object {
        private const val NETWORK_ERROR_MESSAGE = "網路異常，請確認網路連線後再試"
    }
}
