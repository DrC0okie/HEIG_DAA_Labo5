package ch.heigvd.daa.labo5.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import ch.heigvd.daa.labo5.R
import com.bumptech.glide.Glide

class FullScreenImageActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_full_screen_image)

        val imageUrl = intent.getStringExtra("IMAGE_URL")

        // Use Glide to load the image
        Glide.with(this).load(imageUrl).into(findViewById(R.id.fullScreenImageView))
    }
}