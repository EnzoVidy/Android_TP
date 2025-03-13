package com.example.diceroller

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity


class MainActivity : AppCompatActivity() {

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        val rollButton: Button = findViewById(R.id.button)
        rollButton.setOnClickListener {
            rollDice()
        }

        rollDice()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun rollDice() {
        val dice = Dice(6)

        val diceRoll = dice.roll()
        val diceImage: ImageView = findViewById(R.id.imageView1)
        val drawableResource = getDiceDrawable(diceRoll)
        diceImage.setImageResource(drawableResource)
        diceImage.contentDescription = diceRoll.toString()
        animateDiceRoll(diceImage)

        val diceRoll2 = dice.roll()
        val diceImage2: ImageView = findViewById(R.id.imageView2)
        val drawableResource2 = getDiceDrawable(diceRoll2)
        diceImage2.setImageResource(drawableResource2)
        diceImage2.contentDescription = diceRoll2.toString()
        animateDiceRoll(diceImage2)

        triggerVibration()

        val addition = diceRoll + diceRoll2
        val resultTextView: TextView = findViewById(R.id.textView)
        resultTextView.text = addition.toString()
    }

    private fun getDiceDrawable(diceRoll: Int): Int {
        return when (diceRoll) {
            1 -> R.drawable.dice_1
            2 -> R.drawable.dice_2
            3 -> R.drawable.dice_3
            4 -> R.drawable.dice_4
            5 -> R.drawable.dice_5
            else -> R.drawable.dice_6
        }
    }

    private fun animateDiceRoll(diceImage: ImageView) {
        val rotation = ObjectAnimator.ofFloat(diceImage, "rotation", 0f, 360f).apply {
            duration = 500
        }

        val scaleXUp = ObjectAnimator.ofFloat(diceImage, "scaleX", 1f, 1.1f).apply {
            duration = 150
        }
        val scaleYUp = ObjectAnimator.ofFloat(diceImage, "scaleY", 1f, 1.1f).apply {
            duration = 150
        }
        val scaleXDown = ObjectAnimator.ofFloat(diceImage, "scaleX", 1.1f, 1f).apply {
            duration = 100
        }
        val scaleYDown = ObjectAnimator.ofFloat(diceImage, "scaleY", 1.1f, 1f).apply {
            duration = 100
        }

        val animatorSet = AnimatorSet()
        animatorSet.playSequentially(rotation, scaleXUp, scaleYUp, scaleXDown, scaleYDown)
        animatorSet.start()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun triggerVibration() {
        val vibrator = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            val vibratorManager = getSystemService(VibratorManager::class.java)
            vibratorManager?.defaultVibrator
        } else {
            getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }

        vibrator?.let {
            val vibrationEffect = VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE)
            it.vibrate(vibrationEffect)
        }
    }
}

class Dice(val numSides: Int) {

    fun roll(): Int {
        return (1..numSides).random()
    }
}
