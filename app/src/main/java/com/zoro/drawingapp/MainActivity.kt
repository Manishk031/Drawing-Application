package com.zoro.drawingapp

import android.Manifest
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.media.Image
import android.media.MediaScannerConnection
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.service.voice.VoiceInteractionSession.ActivityId
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.get
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

class MainActivity : AppCompatActivity() {
    private  var drawingView : Drawing? = null
    private  var  mImageButtonCurrentPaint:ImageButton? = null
    var customProgressDialog: Dialog? = null


    val openGalleryLauncher:ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK && result.data != null) {
                val imageBackground: ImageView = findViewById(R.id.iv_background)
                imageBackground.setImageURI(result.data?.data)
            }
        }
    /** create an ActivityResultLauncher with MultiplePermissions since we are requesting
     * both read and write
     */

    val requestPermission: ActivityResultLauncher<Array<String>> =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()){
            permission ->
             permission.entries.forEach{
                       val permissionName = it.key
                 val isGranted = it.value
                 if(isGranted)
                 {
                     Toast.makeText(
                         this@MainActivity,
                         "Permission granted now you can read the storage files.",
                         Toast.LENGTH_LONG
                     ).show()

                     val pickIntent = Intent(Intent.ACTION_PICK,
                         MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                     openGalleryLauncher.launch(pickIntent)
                 }else {
                             if(permissionName==Manifest.permission.READ_EXTERNAL_STORAGE)
                                 Toast.makeText(
                                     this@MainActivity,
                                     "Permission granted now you can read the storage files.",
                                     Toast.LENGTH_LONG
                                 ).show()
                 }
            }
        }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        drawingView = findViewById(R.id.Drawing_view)
        drawingView?.setSizeForBrush(20.toFloat())

        val linearLayoutPaintColor = findViewById<LinearLayout>(R.id.ll_paint_color)

        mImageButtonCurrentPaint = linearLayoutPaintColor[1] as ImageButton
        mImageButtonCurrentPaint!!.setImageDrawable(
            ContextCompat.getDrawable(this, R.drawable.pallet_pressedl)
            /* we overrode the  normal pallet to be the pallet_pressed */
        )

        val ib_brush: ImageButton= findViewById(R.id.ib_brush)
        ib_brush.setOnClickListener{
            showBrushSizeChooserDialog()
        }
            val ibGallery:ImageButton = findViewById(R.id.ib_gallery)
        ibGallery.setOnClickListener{
            requestStoragePermission()
        }

        val ibUndo: ImageButton = findViewById(R.id.ib_undo)
        ibUndo.setOnClickListener {
            // This is for undo recent stroke.
            drawingView?.onClickUndo()
        }

        val ibSave : ImageButton = findViewById(R.id.ib_save)
        ibSave.setOnClickListener{

            if(isReadStorageAllowed()){
                showProgressDialog()
                lifecycleScope.launch {
                    val flDrawingView: FrameLayout = findViewById(R.id.fl_drawing_view_container)
                     // val  myBitmap:Bitmap = getBitmapFromView(flDrawingView)
                    saveBitmapFile(getBitmapFromView(flDrawingView))
                }
            }
        }


            }
       private   fun showBrushSizeChooserDialog()
       {
           var brushDialog = Dialog(this)
           brushDialog.setContentView(R.layout.dialog_brush_size)
           brushDialog.setTitle("Brush Size: ")

               // small btn
               val smallBtn :ImageButton = brushDialog.findViewById(R.id.small_brush)
               smallBtn.setOnClickListener{
               drawingView?.setSizeForBrush(10.toFloat())
               brushDialog.dismiss()
           }
           // medium btn
           val mediumBtn :ImageButton = brushDialog.findViewById(R.id.medium_brush)
           mediumBtn.setOnClickListener{
               drawingView?.setSizeForBrush(20.toFloat())
               brushDialog.dismiss()
           }
           //large btn
           val largeBtn :ImageButton = brushDialog.findViewById(R.id.large_brush)
           largeBtn.setOnClickListener{
               drawingView?.setSizeForBrush(30.toFloat())
               brushDialog.dismiss()
           }
           brushDialog.show()
       }

    fun paintClicked(view: View){
        if(view!=mImageButtonCurrentPaint){
            val imageButton = view as ImageButton
            val colorTag = imageButton.tag.toString()
            drawingView?.setColor(colorTag)

            imageButton.setImageDrawable(
                ContextCompat.getDrawable(this, R.drawable.pallet_pressedl))

            mImageButtonCurrentPaint?.setImageDrawable(
                ContextCompat.getDrawable(this, R.drawable.pallet_normal))
            mImageButtonCurrentPaint = view
        }

    }

    private fun isReadStorageAllowed(): Boolean {
        //Getting the permission status
        // Here the checkSelfPermission is
        /*
         * Determine whether <em>you</em> have been granted a particular permission.
         *
         * @param permission The name of the permission being checked.
         *
         */
        val result = ContextCompat.checkSelfPermission(
            this, Manifest.permission.READ_EXTERNAL_STORAGE)
        /*
         *
         * @return {@link android.content.pm.PackageManager#PERMISSION_GRANTED} if you have the
         * permission, or {@link android.content.pm.PackageManager#PERMISSION_DENIED} if not.
         *
         */
        //If permission is granted returning true and If permission is not granted returning false
        return result == PackageManager.PERMISSION_GRANTED
    }


  //create a method to requestStorage permission
    private fun requestStoragePermission(){
      // Check if the permission was denied and show rationale

        if (
            ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
        ){

            showRationaleDialog("Kids Drawing App","Kids Drawing App " +
                    "needs to Access Your External Storage")
        }
        else {

            requestPermission.launch(
                arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    //TODO - Add writing external storage permission.
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
            )
        }

    }
    /**  create rationale dialog
     * Shows rationale dialog for displaying why the app needs permission
     * Only shown if the user has denied the permission request previously
     */
    private fun showRationaleDialog(
        title: String,
        message: String,
    ) {
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setTitle(title)
            .setMessage(message)
            .setPositiveButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
        builder.create().show()
    }
    // TODO(Step 4 : Getting and bitmap Exporting the image to your phone storage.)
    /**
     * Create bitmap from view and returns it
     */
    private fun getBitmapFromView(view: View): Bitmap {


        val returnedBitmap = Bitmap.createBitmap(view.width, view.height,
            Bitmap.Config.ARGB_8888)

        val canvas = Canvas(returnedBitmap)

        val bgDrawable = view.background
        if (bgDrawable != null) {

            bgDrawable.draw(canvas)
        } else {

            canvas.drawColor(Color.WHITE)
        }

        view.draw(canvas)

        return returnedBitmap
    }




    // TODO(Step 2 : A method to save the image.)
    private suspend fun saveBitmapFile(mBitmap: Bitmap?):String{
        var result = ""
        withContext(Dispatchers.IO) {
            if (mBitmap != null) {


                try {
                    val bytes = ByteArrayOutputStream() // Creates a new byte array output stream.
                    // The buffer capacity is initially 32 bytes, though its size increases if necessary.

                    mBitmap.compress(Bitmap.CompressFormat.PNG, 90, bytes)
                    /*
                 * Write a compressed version of the bitmap to the specified outputstream.
                 * If this returns true, the bitmap can be reconstructed by passing a
                 * corresponding inputstream to BitmapFactory.decodeStream(). Note: not
                 * all Formats support all bitmap configs directly, so it is possible that
                 * the returned bitmap from BitmapFactory could be in a different bitdepth,
                 * and/or may have lost per-pixel alpha (e.g. JPEG only supports opaque
                 * pixels).
                 *
                 * @param format   The format of the compressed image
                 * @param quality  Hint to the compressor, 0-100. 0 meaning compress for
                 *                 small size, 100 meaning compress for max quality. Some
                 *                 formats, like PNG which is lossless, will ignore the
                 *                 quality setting
                 * @param stream   The outputstream to write the compressed data.
                 * @return true if successfully compressed to the specified stream.
                 */



                    val f = File(
                        externalCacheDir?.absoluteFile.toString()
                                + File.separator + "DrawingApp_" + System.currentTimeMillis() / 1000 + ".png"
                    )
                    // Here the Environment : Provides access to environment variables.
                    // getExternalStorageDirectory : returns the primary shared/external storage directory.
                    // absoluteFile : Returns the absolute form of this abstract pathname.
                    // File.separator : The system-dependent default name-separator character. This string contains a single character.

                    val fo = FileOutputStream(f) // Creates a file output stream to write to the file represented by the specified object.

                    fo.write(bytes.toByteArray()) // Writes bytes from the specified byte array to this file output stream.

                    fo.close() // Closes this file output stream and releases any system resources associated with this stream. This file output stream may no longer be used for writing bytes.
                    result = f.absolutePath // The file absolute path is return as a result.
                    //We switch from io to ui thread to show a toast
                    runOnUiThread {
                        cancelProgressDialog()
                        if (result.isNotEmpty()) {
                            Toast.makeText(
                                this@MainActivity,
                                "File saved successfully :$result",
                                Toast.LENGTH_SHORT
                            ).show()
                            shareImage(result)
                        } else {
                            Toast.makeText(
                                this@MainActivity,
                                "Something went wrong while saving the file.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                } catch (e: Exception) {
                    result = ""
                    e.printStackTrace()
                }
            }
        }
        return result
    }
    private fun showProgressDialog() {
        customProgressDialog = Dialog(this@MainActivity)

        /*Set the screen content from a layout resource.
        The resource will be inflated, adding all top-level views to the screen.*/
        customProgressDialog?.setContentView(R.layout.dialog_custom_progress)

        //Start the dialog and display it on screen.
        customProgressDialog?.show()
    }
    private fun cancelProgressDialog() {
        if (customProgressDialog != null) {
            customProgressDialog?.dismiss()
            customProgressDialog = null
        }
    }
    private  fun shareImage(result:String){
        /*MediaScannerConnection provides a way for applications to pass a
        newly created or downloaded media file to the media scanner service.
        The media scanner service will read metadata from the file and add
        the file to the media content provider.
        The MediaScannerConnectionClient provides an interface for the
        media scanner service to return the Uri for a newly scanned file
        to the client of the MediaScannerConnection class.*/
        MediaScannerConnection.scanFile(this@MainActivity, arrayOf(result), null )
        { path, uri ->
            // This is used for sharing the image after it has being stored in the storage.
            val shareIntent = Intent()
            shareIntent.action = Intent.ACTION_SEND
            shareIntent.putExtra(Intent.EXTRA_STREAM,uri)
            // A content: URI holding a stream of data associated with the Intent, used to supply the data being sent.
            shareIntent.type ="image/png" // The MIME type of the data being handled by this intent.
            startActivity(Intent.createChooser(shareIntent,"Share"))
        }
    }


}