package com.vipulog.ebookreader

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.vipulog.ebookreader.databinding.ActivityMainBinding


class MainActivity : Activity() {
    private lateinit var binding: ActivityMainBinding
    private val pickEbookFileRequestCode: Int = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.openFile.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.type = "*/*"
            intent.putExtra(Intent.EXTRA_MIME_TYPES, arrayOf(
                "application/epub+zip",
                "application/x-mobipocket-ebook",
                "application/vnd.amazon.ebook",
                "application/fb2+zip",
                "application/vnd.comicbook+zip"
            ))
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            startActivityForResult(intent, pickEbookFileRequestCode)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == pickEbookFileRequestCode && resultCode == RESULT_OK) {
            if (data != null && data.data != null) {
                val uri = data.data
                val intent = Intent(this, ReaderActivity::class.java).setAction(Intent.ACTION_VIEW)
                intent.data = uri
                startActivity(intent)
            }
        }
    }
}