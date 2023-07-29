package com.vipulog.ebookreader

import android.content.Context
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream


fun saveData(fileName: String, data: Any?, context: Context) {
    val fos: FileOutputStream = context.openFileOutput(fileName, Context.MODE_PRIVATE)
    val os = ObjectOutputStream(fos)
    os.writeObject(data)
    os.close()
    fos.close()
}


@Suppress("UNCHECKED_CAST")
fun <T> loadData(fileName: String, context: Context): T? {
    if (context.fileList() == null || !context.fileList().contains(fileName)) return null
    val fileIS: FileInputStream = context.openFileInput(fileName)
    val objIS = ObjectInputStream(fileIS)
    val data = objIS.readObject() as T
    objIS.close()
    fileIS.close()
    return data
}