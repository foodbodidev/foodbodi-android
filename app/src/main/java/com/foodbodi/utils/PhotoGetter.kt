package com.foodbodi.utils

import android.content.Context
import android.content.Intent
import android.os.Parcelable
import android.net.Uri
import androidx.core.graphics.TypefaceCompatUtil.getTempFile
import android.provider.MediaStore
import android.content.ComponentName
import android.content.pm.ResolveInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import java.io.File
import android.R.attr.bitmap
import android.R.attr.data
import android.app.Activity
import android.graphics.ImageDecoder
import android.graphics.Matrix
import android.widget.ImageView
import android.widget.Toast
import androidx.core.app.NotificationCompat.getExtras
import androidx.exifinterface.media.ExifInterface
import com.foodbodi.apis.FoodBodiResponse
import com.foodbodi.apis.FoodbodiRetrofitHolder
import com.foodbodi.apis.UploadResponse
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList
import com.google.android.gms.common.util.IOUtils.toByteArray
import com.squareup.picasso.Picasso
import com.yalantis.ucrop.UCrop
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream
import java.net.URL


class PhotoGetter(context:Activity) {
    private val context = context
    var photo_name = Date().toString()
    companion object {
        fun bitmapToJPEG(bitmap: Bitmap) : ByteArray {
            val stream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
            val byteArray = stream.toByteArray()
            return byteArray
        }
        fun loadImageFromURL(mediaLink:String, imageView: ImageView) {
            Picasso.get().load(mediaLink).centerCrop().resize(imageView.width, imageView.height).into(imageView)

        }
    }

    private fun getCaptureImageOutputUri(): Uri? {
        var outputFileUri: Uri? = null
        val getImage = context.getExternalCacheDir()
        if (getImage != null) {
            outputFileUri = Uri.fromFile(File(getImage!!.getPath(), photo_name))
        }
        return outputFileUri
    }

    fun getImageUri(inContext: Context, inImage: Bitmap): Uri {
        val bytes = ByteArrayOutputStream()
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val path = MediaStore.Images.Media.insertImage(inContext.contentResolver, inImage, photo_name, null)
        return Uri.parse(path)
    }

    fun getCroppedImageOutputUri() : Uri? {
        val getImage = context.getExternalCacheDir()
        return Uri.fromFile(File(getImage!!.path, "cropped"))
    }

    fun getPickPhotoIntent() : Intent {
        // Determine Uri of camera image to save.
        val outputFileUri = getCaptureImageOutputUri()

        val allIntents = ArrayList<Intent>()
        val packageManager = context.getPackageManager()

        // collect all camera intents
        val captureIntent = Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE)
        val listCam = packageManager.queryIntentActivities(captureIntent, 0)
        for (res in listCam) {
            val intent = Intent(captureIntent)
            intent.component = ComponentName(res.activityInfo.packageName, res.activityInfo.name)
            intent.setPackage(res.activityInfo.packageName)
            if (outputFileUri != null) {
                intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri)
            }
            allIntents.add(intent)
        }

        // collect all gallery intents
        val galleryIntent = Intent(Intent.ACTION_GET_CONTENT)
        galleryIntent.type = "image/*"
        val listGallery = packageManager.queryIntentActivities(galleryIntent, 0)
        for (res in listGallery) {
            val intent = Intent(galleryIntent)
            intent.component = ComponentName(res.activityInfo.packageName, res.activityInfo.name)
            intent.setPackage(res.activityInfo.packageName)
            allIntents.add(intent)
        }

        // the main intent is the last in the list (fucking android) so pickup the useless one
        var mainIntent: Intent = allIntents.get(allIntents.size - 1)
        for (intent in allIntents) {
            if (intent.getComponent()!!.getClassName() == "com.android.documentsui.DocumentsActivity") {
                mainIntent = intent
                break
            }
        }
        allIntents.remove(mainIntent)

        // Create a chooser from the main intent
        val chooserIntent = Intent.createChooser(mainIntent, "Select source")

        // Add all other intents
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, allIntents.toTypedArray())

        return chooserIntent
    }

    fun getBitmap(data:Intent) : Bitmap? {
        var bitmap:Bitmap? = null
        var picUri = getPickImageResultUri(data)
        if (picUri != null) {
            bitmap = getBitmapFromURL(picUri)
        } else {
            bitmap = (data.getExtras()?.get("data") as Bitmap)
        }
        return bitmap
    }

    fun getBitmapFromURL(url:Uri):Bitmap? {
        try {
            var myBitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), url)
            //myBitmap = rotateImageIfRequired(myBitmap, picUri!!)
            myBitmap = getResizedBitmap(myBitmap, 500)
            return myBitmap

        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }

    fun getBitmapFromURLString(url:String) :Bitmap?{
        var uri:Uri = Uri.parse(url)
        return getBitmapFromURL(uri)
    }

    fun getPickImageResultUri(data: Intent?): Uri? {
        var isCamera = data == null; //from camera, data is null, nnot sure why, but it works that way

        if (isCamera) {
            // val photo:Bitmap = data?.extras?.get("data") as Bitmap
            return getCaptureImageOutputUri()
        } else {
            return data!!.data
        }

    }

    @Throws(IOException::class)
    private fun rotateImageIfRequired(img: Bitmap, selectedImage: Uri): Bitmap? {
            val ei = ExifInterface(selectedImage.path!!)
            val orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)

            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> return rotateImage(img, 90)
                ExifInterface.ORIENTATION_ROTATE_180 -> return rotateImage(img, 180)
                ExifInterface.ORIENTATION_ROTATE_270 -> return rotateImage(img, 270)
                else -> return img
            }
    }

    private fun rotateImage(img: Bitmap, degree: Int): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(degree.toFloat())
        val rotatedImg = Bitmap.createBitmap(img, 0, 0, img.width, img.height, matrix, true)
        img.recycle()
        return rotatedImg
    }

    fun getResizedBitmap(image: Bitmap, maxSize: Int): Bitmap {
        var width = image.width
        var height = image.height

        val bitmapRatio = width.toFloat() / height.toFloat()
        if (bitmapRatio > 0) {
            width = maxSize
            height = (width / bitmapRatio).toInt()
        } else {
            height = maxSize
            width = (height * bitmapRatio).toInt()
        }
        return Bitmap.createScaledBitmap(image, width, height, true)
    }
}