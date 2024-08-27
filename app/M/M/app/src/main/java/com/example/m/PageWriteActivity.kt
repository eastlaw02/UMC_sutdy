package com.example.m

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.m.databinding.ActivityPageWriteBinding

class PageWriteActivity : AppCompatActivity() {

    lateinit var binding: ActivityPageWriteBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPageWriteBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}