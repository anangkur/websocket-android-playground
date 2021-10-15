package com.anangkur.websocketplayground

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.anangkur.websocketplayground.databinding.ActivityMainBinding
import com.anangkur.websocketplayground.model.Message
import com.anangkur.websocketplayground.services.EchoService
import com.google.android.material.snackbar.Snackbar
import com.tinder.scarlet.*
import com.tinder.scarlet.Message.Bytes
import com.tinder.scarlet.Message.Text
import com.tinder.scarlet.WebSocket.Event.*
import com.tinder.scarlet.lifecycle.android.AndroidLifecycle
import com.tinder.scarlet.streamadapter.rxjava2.RxJava2StreamAdapterFactory
import com.tinder.scarlet.websocket.okhttp.newWebSocketFactory
import io.reactivex.android.schedulers.AndroidSchedulers
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import com.tinder.scarlet.Message as MessageScarlet


class MainActivity : AppCompatActivity() {

    companion object {
        private const val ECHO_URL = "wss://websocket-echo.glitch.me"
    }

    private lateinit var binding: ActivityMainBinding

    private lateinit var webSocketService: EchoService

    private lateinit var adapter: ChatMessageAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)

        setContentView(binding.root)

        setupToolbar()
        setupEditTextMessage()
        setupRecyclerViewMessage()
        setupButtonSend(binding.etMessage.text.toString())

        setupWebSocketService()

        observeConnection()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        binding.toolbar.title = "connecting.."
    }

    private fun setupRecyclerViewMessage() {
        adapter = ChatMessageAdapter()
        binding.recyclerMessage.apply {
            adapter = this@MainActivity.adapter
            itemAnimator = DefaultItemAnimator()
            layoutManager = LinearLayoutManager(this@MainActivity, RecyclerView.VERTICAL, false)
        }
    }

    private fun setupWebSocketService() {
        webSocketService = provideWebSocketService(
            scarlet = provideScarlet(
                client = provideOkhttp(),
                lifecycle = provideLifeCycle(),
                streamAdapterFactory = provideStreamAdapterFactory(),
            )
        )
    }

    @SuppressLint("CheckResult")
    private fun observeConnection() {
        webSocketService.observeConnection()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ response ->
                Log.d("observeConnection", response.toString())
                onReceiveResponseConnection(response)
            }, { error ->
                Log.e("observeConnection", error.message.orEmpty())
                Snackbar.make(binding.root, error.message.orEmpty(), Snackbar.LENGTH_SHORT).show()
            })
    }

    private fun onReceiveResponseConnection(response: WebSocket.Event) {
        when (response) {
            is OnConnectionOpened<*> -> changeToolbarTitle("connection opened")
            is OnConnectionClosed -> changeToolbarTitle("connection closed")
            is OnConnectionClosing -> changeToolbarTitle("closing connection..")
            is OnConnectionFailed -> changeToolbarTitle("connection failed")
            is OnMessageReceived -> handleOnMessageReceived(response.message)
        }
    }

    private fun handleOnMessageReceived(message: MessageScarlet) {
        adapter.addItem(Message(message.toValue(), false))
        binding.etMessage.setText("")
    }

    private fun MessageScarlet.toValue(): String {
        return when (this) {
            is Text -> value
            is Bytes -> value.toString()
        }
    }

    private fun changeToolbarTitle(title: String) {
        binding.toolbar.title = title
    }

    private fun setupEditTextMessage() {
        binding.etMessage.doAfterTextChanged {
            setupButtonSend(it.toString())
        }
    }

    private fun setupButtonSend(message: String) {
        binding.btnSend.isEnabled = message.isNotBlank()
        binding.btnSend.setOnClickListener { sendMessage(message) }
    }

    private fun sendMessage(message: String) {
        webSocketService.sendMessage(message)
        adapter.addItem(Message(message = message, isFromSender = true))
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
            .webSocketFactory(client.newWebSocketFactory(ECHO_URL))
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