package abled.kkont.fishinggame.splash

import abled.kkont.fishinggame.R
import abled.kkont.fishinggame.databinding.ActivitySplashBinding
import abled.kkont.fishinggame.main.MainActivity
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.view.View
import android.view.animation.AnimationUtils
import kotlin.concurrent.thread

class SplashActivity : AppCompatActivity() {

    lateinit var binding: ActivitySplashBinding
    private lateinit var model: SplashModel
    private lateinit var preference:SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        preference = applicationContext.getSharedPreferences("fishingGame", Context.MODE_PRIVATE)
        model = SplashModel(preference)
        binding =  ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        model.checkCollection()
        startSplash()

    } // onCreate()


    /*
    액티비티 종료 시 애니메이션 추가.
     */
    override fun onPause() {
        super.onPause()

        overridePendingTransition(R.anim.layout_none, R.anim.layout_left_out)

    } // onPause()


    /*
    스플래시 시작.
    애니메이션 효과 후 액티비티 전환.
     */
    fun startSplash() {

        val animation = AnimationUtils.loadAnimation(this, R.anim.splash_animation)

        binding.linearLayoutSplash.startAnimation(animation)

        thread {

            Thread.sleep(3000)

            binding.imageViewAppLogo.visibility = View.INVISIBLE
            binding.imageViewTeamLogo.visibility = View.INVISIBLE
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()

        }

    } // startSplash()

}