package com.cleaner.tvbox

import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.os.Environment
import android.os.StatFs
import android.provider.Settings
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import java.text.DecimalFormat

class MainActivity : Activity() {

    private lateinit var txtStatus: TextView
    private lateinit var txtRam: TextView
    private lateinit var txtStorage: TextView
    private lateinit var txtAppList: TextView
    private lateinit var btnLimpar: Button
    private lateinit var btnPermissao: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val scrollLayout = ScrollView(this).apply {
            setBackgroundColor(Color.parseColor("#0F172A"))
            isFillViewport = true
        }

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(30, 30, 30, 30)
        }

        val txtTitle = TextView(this).apply {
            text = "⚡ OTIMIZADOR TV BOX FORTE"
            textSize = 20f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.parseColor("#38BDF8"))
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, 15)
        }

        txtStatus = TextView(this).apply {
            text = "Sistema pronto para otimização profunda"
            textSize = 14f
            setTextColor(Color.parseColor("#F8FAFC"))
            gravity = Gravity.CENTER
            setPadding(0, 5, 0, 15)
        }

        txtRam = TextView(this).apply {
            textSize = 15f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.parseColor("#10B981"))
            gravity = Gravity.CENTER
            setPadding(0, 5, 0, 5)
        }

        txtStorage = TextView(this).apply {
            textSize = 13f
            setTextColor(Color.parseColor("#94A3B8"))
            gravity = Gravity.CENTER
            setPadding(0, 5, 0, 15)
        }

        // Botão para abrir as configurações de Acessibilidade do Android
        btnPermissao = Button(this).apply {
            text = "⚙️ 1. ATIVAR PERMISSÃO DE ACESSIBILIDADE"
            textSize = 13f
            setTextColor(Color.WHITE)
            setBackgroundColor(Color.parseColor("#D97706"))
            setPadding(15, 15, 15, 15)
            setOnClickListener {
                try {
                    startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                } catch (e: Exception) {}
            }
        }

        btnLimpar = Button(this).apply {
            text = "🚀 2. EXECUTAR LIMPEZA FORÇADA"
            textSize = 15f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.WHITE)
            setBackgroundColor(Color.parseColor("#DC2626"))
            setPadding(20, 20, 20, 20)
            setOnClickListener {
                executarLimpeza()
            }
        }

        txtAppList = TextView(this).apply {
            text = "Passo 1: Clique no botão laranja para ativar a permissão.\nPasso 2: Clique no botão vermelho para limpar."
            textSize = 12f
            setTextColor(Color.parseColor("#CBD5E1"))
            gravity = Gravity.START
            setPadding(10, 20, 10, 10)
        }

        layout.addView(txtTitle)
        layout.addView(txtStatus)
        layout.addView(txtRam)
        layout.addView(txtStorage)
        layout.addView(btnPermissao)
        layout.addView(btnLimpar)
        layout.addView(txtAppList)
        scrollLayout.addView(layout)
        setContentView(scrollLayout)

        atualizarHardware("Sistema pronto.")
    }

    data class InfoRam(val livreMB: Long, val totalMB: Long)

    private fun lerMemoriaRam(): InfoRam {
        val actManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memInfo = ActivityManager.MemoryInfo()
        actManager.getMemoryInfo(memInfo)
        return InfoRam(memInfo.availMem / (1024 * 1024), memInfo.totalMem / (1024 * 1024))
    }

    private fun atualizarHardware(msg: String) {
        val ram = lerMemoriaRam()
        txtStatus.text = msg
        txtRam.text = "RAM Livre: ${ram.livreMB} MB / ${ram.totalMB} MB"
        txtStorage.text = "Gerenciador de Sistema Ativo"
    }

    private fun executarLimpeza() {
        val actManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val pm = packageManager
        val fechados = mutableListOf<String>()
        var contador = 0

        val pacotes = pm.getInstalledApplications(0)
        for (app in pacotes) {
            if (app.packageName != packageName && (app.flags and android.content.pm.ApplicationInfo.FLAG_SYSTEM) == 0) {
                try {
                    val nome = pm.getApplicationLabel(app).toString()
                    actManager.killBackgroundProcesses(app.packageName)
                    fechados.add(nome)
                    contador++
                } catch (e: Exception) {}
            }
        }

        System.gc()
        atualizarHardware("🔥 Limpeza forçada executada!")

        txtAppList.text = if (fechados.isNotEmpty()) {
            "📦 Apps encerrados ($contador):\n• " + fechados.joinToString("\n• ")
        } else {
            "📦 Nenhum app adicional para encerrar."
        }
    }
}
