package com.example.myapplication.Activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.example.myapplication.Domain.TutorModel
import com.example.myapplication.R
import com.example.myapplication.databinding.ActivityDetailBinding

class DetailActivity : BaseActivity() {
    private lateinit var binding: ActivityDetailBinding
    private lateinit var item:TutorModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        getBundle()


    }

    private fun getBundle() {
        item=intent.getParcelableExtra("object")!!

        binding.apply {
            titleTxtdetail.text=item.Name
            specialtxtdetail.text=item.Special
            studentTxt.text= item.Students
            bioTxt.text=item.Biography
            addressTxt.text = item.Address
            experienceTxt.text=item.Experience.toString()+" Years"
            ratingTxt.text= "${item.Rating}"
            backbtn.setOnClickListener{ finish() }

            websiteBtn.setOnClickListener{
                val i =Intent(Intent.ACTION_VIEW)
                i.setData(Uri.parse(item.Site))
                startActivity(i)
            }

            Log.d("DetailActivity", "Received Tutor Model: $item")  // Log the entire TutorModel object


            messageBtn.setOnClickListener{
                val uri= Uri.parse("smsto:${item.Mobile}")
                val intent=Intent(Intent.ACTION_SENDTO,uri)
                intent.putExtra("sms_body","the SMS text")
                startActivity(intent)
            }

            makeBtn.setOnClickListener {
                Log.d("DetailActivity", "Sending Tutor ID: ${item.Id}, Tutor Name: ${item.Name}")

                val intent = Intent(this@DetailActivity, CalendarActivity::class.java)
                intent.putExtra("tutorId",item.Id)
                intent.putExtra("tutorName",item.Name)
                startActivity(intent)

                Log.d("DetailActivity", "Sending Tutor ID: ${item.Id}, Tutor Name: ${item.Name}")
                Log.d("DetailActivity", "Tutor ID: ${item.Id}, Tutor Name: ${item.Name}")


            }


            Glide.with(this@DetailActivity)
                .load(item.Picture)
                .into(profileimg)

        }
    }
}