package com.mojolauncher.android

import android.app.*
import android.content.*
import android.net.Uri
import android.os.Bundle
import android.view.WindowManager
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import java.io.File

data class GameVersion(val id: String, val jar: File)

class MainActivity : AppCompatActivity() {
    private lateinit var root: LinearLayout
    private lateinit var versionsBox: LinearLayout
    private val versions = mutableListOf<GameVersion>()
    private var selected: GameVersion? = null
    private val prefs by lazy { getSharedPreferences("launcher", MODE_PRIVATE) }

    private val pickJar = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) importJar(uri)
    }

    override fun onCreate(state: Bundle?) {
        super.onCreate(state)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        buildUi()
        scanVersions()
    }

    private fun buildUi() {
        root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(28, 24, 28, 24)
        }
        root.addView(TextView(this).apply { text = "Mojolauncher-Android"; textSize = 28f })
        root.addView(TextView(this).apply {
            text = "Minecraft Java • sürüm / profil / mod / performans"
            textSize = 14f
        })
        versionsBox = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        root.addView(versionsBox, LinearLayout.LayoutParams(-1, 0, 1f))

        val row = LinearLayout(this)
        row.addView(Button(this).apply {
            text = "JAR Ekle"
            setOnClickListener {
                pickJar.launch(arrayOf("application/java-archive", "application/octet-stream", "*/*"))
            }
        }, LinearLayout.LayoutParams(0, -2, 1f))
        row.addView(Button(this).apply {
            text = "Ayarlar"
            setOnClickListener { showSettings() }
        }, LinearLayout.LayoutParams(0, -2, 1f))
        root.addView(row)
        setContentView(root)
    }

    private fun scanVersions() {
        val base = File(filesDir, "Minecraft/versions")
        if (!base.exists()) base.mkdirs()
        val found = mutableListOf<GameVersion>()
        base.listFiles()?.filter { it.isDirectory }?.forEach { dir ->
            val jar = File(dir, "Minecraft_\${dir.name}.jar")
            if (jar.isFile) found += GameVersion(dir.name, jar)
        }
        versions.clear()
        versions.addAll(found.sortedByDescending { it.id })
        renderVersions()
    }

    private fun renderVersions() {
        versionsBox.removeAllViews()
        if (versions.isEmpty()) {
            versionsBox.addView(TextView(this).apply {
                text = "Henüz sürüm yok. “JAR Ekle” ile kendi Minecraft_<sürüm>.jar dosyanı ekle."
                textSize = 16f
            })
            return
        }
        versions.forEach { v ->
            versionsBox.addView(Button(this).apply {
                text = (if (selected?.id == v.id) "✓ " else "") + "Minecraft \${v.id}"
                setOnClickListener { selected = v; launchSelected(v); renderVersions() }
            })
        }
    }

    private fun importJar(uri: Uri) {
        val name = (uri.lastPathSegment ?: "Minecraft_Unknown.jar").substringAfterLast('/')
        val match = Regex("Minecraft_(.+)\\.jar", RegexOption.IGNORE_CASE).find(name)
        val id = match?.groupValues?.get(1)
            ?: Regex("([0-9]+(?:\\.[0-9]+){1,3})").find(name)?.value
            ?: "custom"
        val dir = File(filesDir, "Minecraft/versions/\$id").apply { mkdirs() }
        val out = File(dir, "Minecraft_\$id.jar")
        contentResolver.openInputStream(uri)?.use { input ->
            out.outputStream().use { output -> input.copyTo(output) }
        }
        selected = GameVersion(id, out)
        prefs.edit().putString("lastVersion", id).apply()
        scanVersions()
        Toast.makeText(this, "Eklendi: Minecraft_\$id.jar", Toast.LENGTH_LONG).show()
    }

    private fun launchSelected(v: GameVersion) {
        prefs.edit().putString("lastVersion", v.id).apply()
        val plan = File(v.jar.parentFile, "launch.json")
        if (plan.isFile) {
            Toast.makeText(this, "Minecraft \${v.id}: launch.json bulundu; çalışma planı hazır.", Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(this,
                "Seçildi: Minecraft_\${v.id}.jar. Android'de gerçek çalıştırma için uyumlu Java + LWJGL + kütüphane/runtime gerekir.",
                Toast.LENGTH_LONG).show()
        }
    }

    private fun showSettings() {
        val box = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(30, 10, 30, 10)
        }
        fun field(label: String, key: String, def: String) {
            box.addView(EditText(this).apply {
                hint = label
                setText(prefs.getString(key, def))
                tag = key
            })
        }
        field("RAM MB", "ram", "2048")
        field("FPS limiti", "fps", "60")
        field("Çözünürlük ölçeği %", "scale", "100")
        field("JVM argümanları", "jvmArgs", "-Xms512m -Xmx2048m")
        val left = Switch(this).apply {
            text = "Solak kontrol"
            isChecked = prefs.getBoolean("leftHanded", false)
        }
        box.addView(left)
        AlertDialog.Builder(this).setTitle("Performans + Kontroller").setView(box)
            .setPositiveButton("Kaydet") { _, _ ->
                for (i in 0 until box.childCount) {
                    val e = box.getChildAt(i)
                    if (e is EditText) prefs.edit().putString(e.tag as String, e.text.toString()).apply()
                }
                prefs.edit().putBoolean("leftHanded", left.isChecked).apply()
            }.setNegativeButton("İptal", null).show()
    }
}
