package idv.fan.iisigroup.android.test.network

import android.content.Context
import idv.fan.iisigroup.android.test.R
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test
import java.net.ConnectException
import java.net.UnknownHostException

class NetworkErrorInterceptorTest {

    private lateinit var context: Context
    private lateinit var chain: Interceptor.Chain
    private lateinit var request: Request
    private lateinit var interceptor: NetworkErrorInterceptor

    private val networkErrorMessage = "網路異常，請確認網路連線後再試"

    @Before
    fun setUp() {
        context = mockk()
        chain = mockk()
        request = mockk()

        every { context.getString(R.string.error_network) } returns networkErrorMessage
        every { chain.request() } returns request

        interceptor = NetworkErrorInterceptor(context)
    }

    // -------------------------------------------------------------------------
    // 1. ConnectException throws NetworkException
    // -------------------------------------------------------------------------
    @Test
    fun `ConnectException throws NetworkException`() {
        every { chain.proceed(any()) } throws ConnectException("Connection refused")

        val thrown = assertThrows(NetworkException::class.java) {
            interceptor.intercept(chain)
        }

        assertNotNull(thrown)
        assert(thrown.message == networkErrorMessage)
    }

    // -------------------------------------------------------------------------
    // 2. UnknownHostException throws NetworkException
    // -------------------------------------------------------------------------
    @Test
    fun `UnknownHostException throws NetworkException`() {
        every { chain.proceed(any()) } throws UnknownHostException("Host not found")

        val thrown = assertThrows(NetworkException::class.java) {
            interceptor.intercept(chain)
        }

        assertNotNull(thrown)
        assert(thrown.message == networkErrorMessage)
    }

    // -------------------------------------------------------------------------
    // 3. normal request returns response without exception
    // -------------------------------------------------------------------------
    @Test
    fun `normal request returns response without exception`() {
        val mockResponse: Response = mockk()
        every { chain.proceed(any()) } returns mockResponse

        val result = interceptor.intercept(chain)

        assertNotNull(result)
        verify(exactly = 1) { chain.proceed(request) }
    }
}
