package com.zoro.drawingapp

import android.app.Dialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.get

class MainActivity : AppCompatActivity() {
    private  var drawingView : Drawing? = null
    private  var  mImageButtonCurrentPaint:ImageButton? = null
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
}