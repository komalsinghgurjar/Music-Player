package gurjar.komal.musicplayer.classes

import android.Manifest
import android.app.Activity
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import android.Manifest.permission.WRITE_MEDIA_AUDIO


class DemoMusicInsert(private val context: Context) {

    companion object {
        const val PERMISSION_REQUEST_WRITE_MEDIA_AUDIO = 100
    }

    private fun toast(msg: String) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
    }

    fun copyDemoMp3ToMusicFolderOnce() {
        // Check and request permission if needed
        if (!checkWriteMediaAudioPermission()) {
            requestWriteMediaAudioPermission()
            return
        }

        val fileName = "demo.mp3"
        copyFileFromAssetsToMusicFolder(context, fileName)
    }

    private fun checkWriteMediaAudioPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            PermissionChecker.checkSelfPermission(context, Manifest.permission.WRITE_MEDIA_AUDIO) == PermissionChecker.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_MEDIA_AUDIO) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun requestWriteMediaAudioPermission() {
        ActivityCompat.requestPermissions(
            context as Activity,
            arrayOf(Manifest.permission.WRITE_MEDIA_AUDIO),
            PERMISSION_REQUEST_WRITE_MEDIA_AUDIO
        )
    }

    fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == PERMISSION_REQUEST_WRITE_MEDIA_AUDIO) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                copyDemoMp3ToMusicFolderOnce()
            } else {
                toast("Unable to copy file: Permission denied.")
            }
        }
    }

    private fun copyFileFromAssetsToMusicFolder(context: Context, fileName: String) {
        val assetManager = context.assets
        val inputStream: InputStream

        try {
            inputStream = assetManager.open(fileName)

            // Use Scoped Storage API for Android 13+
            val musicFolder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                MediaStore.getExternalContentDirectory(Environment.DIRECTORY_MUSIC)
            } else {
                // Use legacy approach for older versions
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC)
            }

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
                put(MediaStore.Audio.Media.DISPLAY_NAME, fileName)
                put(MediaStore.Audio.Media.MIME_TYPE, "audio/mpeg")
                put(MediaStore.Audio.Media.RELATIVE_PATH, Environment.DIRECTORY_MUSIC)
            }
            val uri: Uri? = resolver.insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, contentValues)

            // Notify media scanner about the new file
            MediaScannerConnection.scanFile(context, arrayOf(outputFile.absolutePath), null) { _, _ ->
                // Scanned
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}
