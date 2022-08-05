package com.doczy.demoringtonecutter

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.masoudss.lib.WaveformSeekBar

class MainActivity : AppCompatActivity() {

    lateinit var seekBar: WaveformSeekBar
    lateinit var waveView: WaveView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        seekBar = findViewById(R.id.wave)
        waveView = findViewById(R.id.waveView)

        seekBar.setSampleFrom(R.raw.music2)

        val map = hashMapOf<Float, String>()
        map[seekBar.maxProgress / 2] = "The middle"
        seekBar.marker = map

        seekBar.sample?.let {
            it.forEachIndexed { index, i ->
                Log.d("vinhwave", "$index : $i")
            }
        }

        seekBar.sample?.let {
            waveView.data = it
        }


        // Wave View
        waveView.listener = object : WaveProgressListener {
            override fun onProgressChange(start: Int, end: Int) {
                Log.d("vinhlis", "$start - $end")
            }

        }
    }
}