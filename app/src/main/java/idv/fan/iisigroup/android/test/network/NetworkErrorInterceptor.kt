package idv.fan.iisigroup.android.test.network

import android.content.Context
import idv.fan.iisigroup.android.test.R
import okhttp3.Interceptor
import okhttp3.Response
import java.net.ConnectException
import java.net.SocketException
import java.net.UnknownHostException

class NetworkErrorInterceptor(private val context: Context) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        return try {
            chain.proceed(chain.request())
        } catch (e: UnknownHostException) {
            throw NetworkException(context.getString(R.string.error_network), e)
        } catch (e: SocketException) {
            throw NetworkException(context.getString(R.string.error_network), e)
        } catch (e: ConnectException) {
            throw NetworkException(context.getString(R.string.error_network), e)
        }
    }
}
