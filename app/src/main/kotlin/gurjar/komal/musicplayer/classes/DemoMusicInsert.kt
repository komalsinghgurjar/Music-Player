package gurjar.komal.musicplayer.classes

import android.Manifest
import android.app.Activity
//import android.content.Context
import android.content.pm.PackageManager
//import android.os.Environment
//import java.io.File
//import java.io.FileOutputStream
import java.io.IOException
import android.util.Log

import android.widget.Toast



import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream




class DemoMusicInsert(private val context: Context) {

    companion object {
        const val PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE = 100
    }
	
	fun toast(msg: String) {
    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
}


    fun copyDemoMp3ToDownloadsFolderOnce() {
        if (context.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED
        ) {
            // Permission not granted, request it
            (context as Activity).requestPermissions(
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE
            )
        } else {
			
            // Permission granted, proceed with copying the file
            val fileName = "demo.mp3"
			copyFileFromAssetsToMusicFolder(context, fileName)
        }
    }

    

    fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission granted, call the method to copy the file
                    copyDemoMp3ToDownloadsFolderOnce()
                } else {
                    // Permission denied, close the app
                    (context as Activity).finish()
                }
            }
            else -> {
                // Handle other permissions if needed
            }
        }
    }
	
	
	
	
	fun copyFileFromAssetsToMusicFolder(context: Context, fileName: String) {
    val assetManager = context.assets
    val inputStream: InputStream

    try {
        inputStream = assetManager.open(fileName)
        val musicFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC)
		
		if (!musicFolder.exists()) {
                    musicFolder.mkdirs()
                }

        val outputFile = File(musicFolder, fileName)
		
		if (outputFile.exists()) {
                // File already exists, return early
                return
            }

        val outputStream = FileOutputStream(outputFile)
        inputStream.use { input ->
            outputStream.use { output ->
                input.copyTo(output)
            }
        }

        // Insert file into MediaStore
        val resolver: ContentResolver = context.contentResolver
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, "audio/mpeg")
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_MUSIC)
        }
        val uri: Uri? = resolver.insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, contentValues)

        // Notify media scanner about the new file
        MediaScannerConnection.scanFile(context, arrayOf(outputFile.absolutePath), null) { path, uri ->
            // Scanned
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}


}
