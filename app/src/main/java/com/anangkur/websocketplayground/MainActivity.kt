package com.anangkur.websocketplayground

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.anangkur.websocketplayground.databinding.ActivityMainBinding
import com.anangkur.websocketplayground.services.EchoService
import com.tinder.scarlet.Lifecycle
import com.tinder.scarlet.Scarlet
import com.tinder.scarlet.StreamAdapter
import com.tinder.scarlet.lifecycle.android.AndroidLifecycle
import com.tinder.scarlet.streamadapter.rxjava2.RxJava2StreamAdapterFactory
import com.tinder.scarlet.websocket.okhttp.newWebSocketFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor

class MainActivity : AppCompatActivity() {

    companion object {
        private const val URL_ECHO_BY_KAAZING = "wss://demos.kaazing.com/echo"
    }

    private lateinit var binding: ActivityMainBinding

    private lateinit var webSocketService: EchoService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)

        webSocketService = provideWebSocketService(
            scarlet = provideScarlet(
                client = provideOkhttp(),
                lifecycle = provideLifeCycle(),
                streamAdapterFactory = provideStreamAdapterFactory(),
            )
        )
    }

    // TODO: 14/10/21 implement dependency injection and move it from activity
    private fun provideWebSocketService(scarlet: Scarlet) = scarlet.create(EchoService::class.java)

    // TODO: 14/10/21 implement dependency injection and move it from activity
    private fun provideScarlet(
        client: OkHttpClient,
        lifecycle: Lifecycle,
        streamAdapterFactory: StreamAdapter.Factory,
    ) =
        Scarlet.Builder()
            .webSocketFactory(client.newWebSocketFactory(URL_ECHO_BY_KAAZING))
            .lifecycle(lifecycle)
            .addStreamAdapterFactory(streamAdapterFactory)
            .build()

    // TODO: 14/10/21 implement dependency injection and move it from activity
    private fun provideOkhttp() =
        OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BASIC))
            .build()

    // TODO: 14/10/21 implement dependency injection and move it from activity
    private fun provideLifeCycle() = AndroidLifecycle.ofApplicationForeground(application)

    // TODO: 14/10/21 implement dependency injection and move it from activity
    private fun provideStreamAdapterFactory() = RxJava2StreamAdapterFactory()
}