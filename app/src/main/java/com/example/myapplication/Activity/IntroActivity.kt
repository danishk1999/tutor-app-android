package com.example.myapplication.Activity

import android.content.Intent
import android.os.Bundle
import com.example.myapplication.databinding.ActivityIntroBinding

class IntroActivity : BaseActivity() {
    private lateinit var binding: ActivityIntroBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityIntroBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //set OnclickListener for the "Get Started Button"
        binding.apply {
            startbutton.setOnClickListener{
                startActivity(Intent(this@IntroActivity, LoginActivity::class.java))
            }

        }

    }
}