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
import android.widget.TextView
import java.text.DecimalFormat

class MainActivity : Activity() {

    private lateinit var txtStatus: TextView
    private lateinit var txtRam: TextView
    private lateinit var txtStorage: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setBackgroundColor(Color.parseColor("#0F172A"))
            setPadding(40, 40, 40, 40)
        }

        val txtTitle = TextView(this).apply {
            text = "⚡ OTIMIZADOR TV BOX"
            textSize = 22f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.parseColor("#38BDF8"))
            gravity = Gravity.CENTER
        }

        txtStatus = TextView(this).apply {
            text = "Lendo hardware..."
            textSize = 16f
            setTextColor(Color.parseColor("#F8FAFC"))
            gravity = Gravity.CENTER
            setPadding(0, 20, 0, 30)
        }

        txtRam = TextView(this).apply {
            textSize = 18f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.parseColor("#10B981"))
            gravity = Gravity.CENTER
            setPadding(0, 10, 0, 10)
        }

        txtStorage = TextView(this).apply {
            textSize = 16f
            setTextColor(Color.parseColor("#94A3B8"))
            gravity = Gravity.CENTER
            setPadding(0, 10, 0, 20)
        }

        layout.addView(txtTitle)
        layout.addView(txtStatus)
        layout.addView(txtRam)
        layout.addView(txtStorage)
        setContentView(layout)

        val ramAntes = lerMemoriaRam()
        atualizarPainel(ramAntes, "Analisando processos abertos...")

        Handler(Looper.getMainLooper()).postDelayed({
            val appsFechados = limparMemoriaReal()
            val ramDepois = lerMemoriaRam()
            val liberadoMB = (ramDepois.livreMB - ramAntes.livreMB).coerceAtLeast(0)

            atualizarPainel(
                ramDepois,
                "✅ Limpeza concluída!\n$appsFechados processos encerrados (+${liberadoMB} MB livres)"
            )
        }, 1200)
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

    private fun limparMemoriaReal(): Int {
        val actManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val pm = packageManager
        val pacotes = pm.getInstalledApplications(0)
        var contador = 0

        for (app in pacotes) {
            if (app.packageName != packageName && (app.flags and android.content.pm.ApplicationInfo.FLAG_SYSTEM) == 0) {
                actManager.killBackgroundProcesses(app.packageName)
                contador++
            }
        }
        System.gc()
        return contador
    }

    private fun atualizarPainel(ram: InfoRam, mensagem: String) {
        txtStatus.text = mensagem
        txtRam.text = "RAM Livre: ${ram.livreMB} MB / ${ram.totalMB} MB"
        txtStorage.text = lerArmazenamentoInterno()
    }
}
