package idv.fan.iisigroup.android.test

import android.content.Context
import okhttp3.OkHttpClient

fun initFlipper(context: Context) = Unit

fun OkHttpClient.Builder.addFlipperNetworkInterceptor(): OkHttpClient.Builder = this
