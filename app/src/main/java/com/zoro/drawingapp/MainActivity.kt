package com.zoro.drawingapp

import android.app.Dialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton

class MainActivity : AppCompatActivity() {
    private  var drawingView : Drawing? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        drawingView = findViewById(R.id.Drawing_view)
        drawingView?.setSizeForBrush(20.toFloat())

        val ib_brush: ImageButton= findViewById(R.id.ib_brush)
        ib_brush.setOnClickListener{
            showBrushSizeChooserDialog()
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
           val mediumBtn :ImageButton = brushDialog.findViewById(R.id.medium_brush)
           mediumBtn.setOnClickListener{
               drawingView?.setSizeForBrush(20.toFloat())
               brushDialog.dismiss()
           }
           val largeBtn :ImageButton = brushDialog.findViewById(R.id.large_brush)
           largeBtn.setOnClickListener{
               drawingView?.setSizeForBrush(30.toFloat())
               brushDialog.dismiss()
           }
           brushDialog.show()
       }
}