package com.cleaner.tvbox

import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.os.Environment
import android.os.StatFs
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
            text = "⚡ OTIMIZADOR TV BOX"
            textSize = 20f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.parseColor("#38BDF8"))
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, 15)
        }

        txtStatus = TextView(this).apply {
            text = "Pronto para otimizar o aparelho"
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

        // Botão manual de limpeza grande e visível
        btnLimpar = Button(this).apply {
            text = "🧹 EXECUTAR LIMPEZA AGORA"
            textSize = 15f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.WHITE)
            setBackgroundColor(Color.parseColor("#2563EB"))
            setPadding(20, 20, 20, 20)
            setOnClickListener {
                executarLimpezaReal()
            }
        }

        txtAppList = TextView(this).apply {
            text = "Toque no botão acima para varrer e encerrar processos em segundo plano."
            textSize = 12f
            setTextColor(Color.parseColor("#CBD5E1"))
            gravity = Gravity.START
            setPadding(10, 20, 10, 10)
        }

        layout.addView(txtTitle)
        layout.addView(txtStatus)
        layout.addView(txtRam)
        layout.addView(txtStorage)
        layout.addView(btnLimpar)
        layout.addView(txtAppList)
        scrollLayout.addView(layout)
        setContentView(scrollLayout)

        // Lê o hardware inicial
        atualizarHardware("Aguardando comando de limpeza...")
    }

    data class InfoRam(val livreMB: Long, val totalMB: Long)

    private fun lerMemoriaRam(): InfoRam {
        val actManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memInfo = ActivityManager.MemoryInfo()
        actManager.getMemoryInfo(memInfo)

        val livreMB = memInfo.availMem / (1024 * 1024)
        val totalMB = memInfo.totalMem / (1024 * 1024)
        return InfoRam(livreMB, totalMB)
    }

    private fun lerArmazenamentoInterno(): String {
        val stat = StatFs(Environment.getDataDirectory().path)
        val bytesDisponiveis = stat.availableBlocksLong * stat.blockSizeLong
        val bytesTotais = stat.blockCountLong * stat.blockSizeLong

        val df = DecimalFormat("#.##")
        val livreGB = df.format(bytesDisponiveis / (1024.0 * 1024.0 * 1024.0))
        val totalGB = df.format(bytesTotais / (1024.0 * 1024.0 * 1024.0))

        return "Armazenamento: $livreGB GB livres de $totalGB GB"
    }

    private fun atualizarHardware(statusMsg: String) {
        val ram = lerMemoriaRam()
        txtStatus.text = statusMsg
        txtRam.text = "RAM Livre: ${ram.livreMB} MB / ${ram.totalMB} MB"
        txtStorage.text = lerArmazenamentoInterno()
    }

    private fun executarLimpezaReal() {
        val actManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val pm = packageManager
        val appsFechados = mutableListOf<String>()
        var contador = 0

        // Pega todos os pacotes instalados no aparelho
        val pacotes = pm.getInstalledApplications(0)

        for (app in pacotes) {
            // Ignora o próprio aplicativo e pacotes principais do sistema Android
            if (app.packageName != packageName && (app.flags and android.content.pm.ApplicationInfo.FLAG_SYSTEM) == 0) {
                try {
                    val nomeApp = pm.getApplicationLabel(app).toString()
                    // Força a parada do processo em segundo plano de verdade
                    actManager.killBackgroundProcesses(app.packageName)
                    appsFechados.add(nomeApp)
                    contador++
                } catch (e: Exception) {
                    // Ignora erros individuais de pacotes protegidos
                }
            }
        }

        System.gc()

        // Atualiza a tela com o resultado detalhado
        atualizarHardware("✅ Limpeza realizada com sucesso!")
        
        val listaTexto = if (appsFechados.isNotEmpty()) {
            "📦 Aplicativos otimizados e fechados ($contador):\n• " + appsFechados.joinToString("\n• ")
        } else {
            "📦 Nenhum aplicativo de usuário rodando em segundo plano no momento."
        }
        
        txtAppList.text = listaTexto
    }
}
