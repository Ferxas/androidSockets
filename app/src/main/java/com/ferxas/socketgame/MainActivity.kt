package com.ferxas.socketgame

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.*
import java.net.ServerSocket
import java.net.Socket

class MainActivity : ComponentActivity() {

    private val port = 12345 // Puerto para la conexión

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SocketApp()
        }
    }

    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    @Composable
    fun SocketApp() {
        var log by remember { mutableStateOf("Logs will appear here") }

        Scaffold(
            content = {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Button(onClick = { startServer { message -> log += "\n$message" } }) {
                        Text(text = "Start Server")
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { connectAsClient { message -> log += "\n$message" } }) {
                        Text(text = "Connect as Client")
                    }
                    Spacer(modifier = Modifier.height(32.dp))
                    Text(
                        text = log,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        )
    }

    private fun startServer(logCallback: (String) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val serverSocket = ServerSocket(port)
                logCallback("Server started. Waiting for connection...")
                val socket = serverSocket.accept()
                logCallback("Client connected!")
                handleCommunication(socket, logCallback)
            } catch (e: IOException) {
                logCallback("Error starting server: ${e.message}")
            }
        }
    }

    private fun connectAsClient(logCallback: (String) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val socket = Socket("127.0.0.1", port) // Aquí necta al localhost
                logCallback("Connected to server!")
                handleCommunication(socket, logCallback)
            } catch (e: IOException) {
                logCallback("Error connecting as client: ${e.message}")
            }
        }
    }

    private fun handleCommunication(socket: Socket, logCallback: (String) -> Unit) {
        try {
            val input = BufferedReader(InputStreamReader(socket.getInputStream()))
            val output = PrintWriter(socket.getOutputStream(), true)

            // Enviar mensaje inicial
            output.println("Hello from ${if (socket.isClosed) "Server" else "Client"}")

            // Leer mensajes
            var message: String? = null // Lo inicializo como nullable
            while (socket.isConnected && input.readLine().also { message = it } != null) {
                logCallback("Received: $message")
            }

        } catch (e: IOException) {
            logCallback("Communication error: ${e.message}")
        } finally {
            socket.close()
        }
    }

}
