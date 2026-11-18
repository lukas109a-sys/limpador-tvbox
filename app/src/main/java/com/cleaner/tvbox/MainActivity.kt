package com.cleaner.tvbox

import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.os.StatFs
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import java.text.DecimalFormat

class MainActivity : Activity() {

    private lateinit var txtStatus: TextView
    private lateinit var txtRam: TextView
    private lateinit var txtStorage: TextView
    private lateinit var txtAppList: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val scrollLayout = ScrollView(this).apply {
            setBackgroundColor(Color.parseColor("#0F172A"))
            isFillViewport = true
        }

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(40, 40, 40, 40)
        }

        val txtTitle = TextView(this).apply {
            text = "⚡ OTIMIZADOR TV BOX"
            textSize = 20f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.parseColor("#38BDF8"))
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, 20)
        }

        txtStatus = TextView(this).apply {
            text = "Analisando processos abertos..."
            textSize = 15f
            setTextColor(Color.parseColor("#F8FAFC"))
            gravity = Gravity.CENTER
            setPadding(0, 10, 0, 20)
        }

        txtRam = TextView(this).apply {
            textSize = 16f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.parseColor("#10B981"))
            gravity = Gravity.CENTER
            setPadding(0, 5, 0, 5)
        }

        txtStorage = TextView(this).apply {
            textSize = 14f
            setTextColor(Color.parseColor("#94A3B8"))
            gravity = Gravity.CENTER
            setPadding(0, 5, 0, 15)
        }

        txtAppList = TextView(this).apply {
            text = "Buscando aplicativos..."
            textSize = 13f
            setTextColor(Color.parseColor("#CBD5E1"))
            gravity = Gravity.START
            setPadding(10, 10, 10, 10)
        }

        layout.addView(txtTitle)
        layout.addView(txtStatus)
        layout.addView(txtRam)
        layout.addView(txtStorage)
        layout.addView(txtAppList)
        scrollLayout.addView(layout)
        setContentView(scrollLayout)

        val ramAntes = lerMemoriaRam()
        atualizarPainel(ramAntes, "Detectando aplicativos ativos...", "")

        Handler(Looper.getMainLooper()).postDelayed({
            val resultado = limparEListarApps()
            val ramDepois = lerMemoriaRam()
            val liberadoMB = (ramDepois.livreMB - ramAntes.livreMB).coerceAtLeast(0)

            val mensagemFinal = "✅ Limpeza concluída!\n${resultado.contador} processos encerrados (+${liberadoMB} MB livres)"
            atualizarPainel(ramDepois, mensagemFinal, resultado.listaNomes)
        }, 1200)
    }

    data class InfoRam(val livreMB: Long, val totalMB: Long)
    data class ResultadoLimpeza(val contador: Int, val listaNomes: String)

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

    private fun limparEListarApps(): ResultadoLimpeza {
        val actManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val pm = packageManager
        val appsEncontrados = mutableListOf<String>()
        var contador = 0

        val runningProcesses = actManager.runningAppProcesses
        if (runningProcesses != null) {
            for (processInfo in runningProcesses) {
                if (processInfo.processName != packageName) {
                    try {
                        val pkgName = processInfo.pkgList?.firstOrNull() ?: processInfo.processName
                        val appInfo = pm.getApplicationInfo(pkgName, 0)
                        
                        if ((appInfo.flags and android.content.pm.ApplicationInfo.FLAG_SYSTEM) == 0) {
                            val nomeAmigavel = pm.getApplicationLabel(appInfo).toString()
                            if (!appsEncontrados.contains(nomeAmigavel)) {
                                appsEncontrados.add(nomeAmigavel)
                            }
                            actManager.killBackgroundProcesses(pkgName)
                            contador++
                        }
                    } catch (e: Exception) {
                    }
                }
            }
        }

        if (appsEncontrados.isEmpty()) {
            appsEncontrados.add("Nenhum app de usuário ativo em segundo plano.")
        }

        val textoLista = "📦 Apps fechados:\n• " + appsEncontrados.joinToString("\n• ")
        return ResultadoLimpeza(contador, textoLista)
    }

    private fun atualizarPainel(ram: InfoRam, status: String, apps: String) {
        txtStatus.text = status
        txtRam.text = "RAM Livre: ${ram.livreMB} MB / ${ram.totalMB} MB"
        txtStorage.text = lerArmazenamentoInterno()
        if (apps.isNotEmpty()) {
            txtAppList.text = apps
        }
    }
}
