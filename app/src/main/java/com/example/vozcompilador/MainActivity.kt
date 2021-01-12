package com.example.vozcompilador

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.speech.RecognizerIntent
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageButton
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.net.Socket
import java.net.UnknownHostException
import java.nio.charset.Charset
import java.util.*

private const val CODE = 10

class MainActivity : AppCompatActivity() {

    private var txtIp: EditText ?= null
    private var txtPort: EditText ?= null
    private var mSocket: Socket ?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val chk = findViewById<CheckBox>(R.id.chk)
        val btn = findViewById<ImageButton>(R.id.btn)
        txtPort = findViewById<EditText>(R.id.txtPort)
        txtIp = findViewById<EditText>(R.id.txtIp)

        chk.setOnClickListener {
            txtPort?.isEnabled = chk.isChecked
        }

        btn.setOnClickListener {
            getVoice()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CODE && resultCode == RESULT_OK && data != null) {
            val result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            result?.let {
                send(it[0])
            }
        }
    }

    private fun send(text: String) {
        val address: String = txtIp?.text.toString().trim()
        val port: Int = txtPort?.text.toString().trim().toInt()

        Thread {
            try {
                mSocket = Socket(address, port)
                val writer: OutputStream = mSocket!!.getOutputStream()
                writer.write((text).toByteArray(Charset.defaultCharset()))
                writer.close()
                mSocket?.close()
            } catch (e: UnknownHostException) {
                runOnUiThread {
                    AlertDialog.Builder(this)
                            .setTitle("Atención")
                            .setMessage("Algo salió mal, porfavor verifique su conexión a internet y que tanto el puerto como la ip sean correctos")
                            .setPositiveButton("OK") { _, _ -> }
                            .show()
                }
            } catch (e: IOException) {
                runOnUiThread {
                    AlertDialog.Builder(this)
                            .setTitle("Atención")
                            .setMessage("Algo salió mal, Error al intentar enviar los datos, porfavor verifique su conexión a internet y que tanto el puerto como la ip sean correctos")
                            .setPositiveButton("OK") { _, _ -> }
                            .show()
                }
            }
        }.start()
    }

    private fun getVoice() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)

        intent.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())

        if (intent.resolveActivity(packageManager) != null) {
            startActivityForResult(intent, CODE)
        } else {
            AlertDialog.Builder(this)
                    .setTitle("Atención")
                    .setMessage("La aplicación no tiene permiso de utilizar el microfono o algo falló")
                    .setPositiveButton("OK"){ _, _->}
                    .show()
        }
    }
}