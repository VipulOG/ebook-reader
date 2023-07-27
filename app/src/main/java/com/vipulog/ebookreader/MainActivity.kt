package com.vipulog.ebookreader

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.vipulog.ebookreader.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.openFile.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.type = "*/*"
            intent.putExtra(
                Intent.EXTRA_MIME_TYPES, arrayOf(
                    "application/epub+zip",
                    "application/x-mobipocket-ebook",
                    "application/vnd.amazon.ebook",
                    "application/fb2+zip",
                    "application/vnd.comicbook+zip"
                )
            )
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            openFileLauncher.launch(intent)
        }

        binding.openUrl.setOnClickListener {
            val tag = OpenBookFromUrlBottomSheet.TAG
            OpenBookFromUrlBottomSheet.newInstance().show(supportFragmentManager, tag)
        }
    }

    private val openFileLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                val data: Intent? = it.data
                if (data != null && data.data != null) {
                    val uri = data.data
                    val intent = Intent(this, ReaderActivity::class.java)
                    intent.data = uri
                    startActivity(intent)
                }
            }
        }
}