package idv.fan.iisigroup.android.test

import android.content.Context
import okhttp3.OkHttpClient

object FlipperInitializer {

    fun init(context: Context) = Unit

    fun OkHttpClient.Builder.addFlipperNetworkInterceptor(): OkHttpClient.Builder = this
}
