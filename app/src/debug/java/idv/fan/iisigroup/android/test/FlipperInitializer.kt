package idv.fan.iisigroup.android.test

import android.content.Context
import com.facebook.flipper.android.AndroidFlipperClient
import com.facebook.flipper.android.utils.FlipperUtils
import com.facebook.flipper.plugins.network.FlipperOkhttpInterceptor
import com.facebook.flipper.plugins.network.NetworkFlipperPlugin
import com.facebook.soloader.SoLoader
import okhttp3.OkHttpClient

private val networkPlugin = NetworkFlipperPlugin()

fun initFlipper(context: Context) {
    SoLoader.init(context, false)
    if (FlipperUtils.shouldEnableFlipper(context)) {
        AndroidFlipperClient.getInstance(context).apply {
            addPlugin(networkPlugin)
            start()
        }
    }
}

fun OkHttpClient.Builder.addFlipperNetworkInterceptor(): OkHttpClient.Builder =
    addNetworkInterceptor(FlipperOkhttpInterceptor(networkPlugin))
