package abled.kkont.fishinggame.home

import abled.kkont.fishinggame.R
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import abled.kkont.fishinggame.databinding.ActivityHomeBinding
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import kotlin.concurrent.thread

class HomeActivity : AppCompatActivity() {

    lateinit var model:HomeModel
    lateinit var binding: ActivityHomeBinding
    lateinit var preferences: SharedPreferences

    private lateinit var buttonClickAnimation : Animation
    private lateinit var layoutFadeInAnimation : Animation
    private lateinit var layoutFadeOutAnimation: Animation
    private lateinit var upgradeAnimation: Animation
    private lateinit var upgradeResultAnimation: Animation

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        overridePendingTransition(R.anim.layout_left_in, R.anim.layout_none)

        setVariable()
        setOnBackPressed()
        setClickListener()
        setUpgradeView()

    } // onCreate()


    override fun onPause() {
        super.onPause()

        overridePendingTransition(R.anim.layout_none , R.anim.layout_right_out)

    } // onPause()


    fun setVariable() {

        preferences = getSharedPreferences("fishingGame", MODE_PRIVATE)
        model = HomeModel(preferences)
        model.getInventoryList()

        layoutFadeInAnimation = AnimationUtils.loadAnimation(this, R.anim.layout_fade_in)
        layoutFadeOutAnimation = AnimationUtils.loadAnimation(this, R.anim.layout_fade_out)
        buttonClickAnimation = AnimationUtils.loadAnimation(this, R.anim.button_click_animation)
        upgradeAnimation = AnimationUtils.loadAnimation(this, R.anim.upgrade_animation)
        upgradeResultAnimation = AnimationUtils.loadAnimation(this, R.anim.upgrade_result_animation)

    } // setVariable()


    fun setUpgradeView() {

        binding.textViewUpgrade.text = model.checkUpgradeMax()

        binding.textViewUserKKON.text = "${model.getKKON()}꼰"

        binding.textViewRodCount.text = "+${model.getRod()}"

        binding.textViewBagCount.text = "${model.itemList.size} / 40"

        binding.textViewUpgradeKKON.text = "강화 비용 : " + model.needKKON()

        binding.textViewSuccessProbability.text = "성공 확률 : " + model.successProbability().toString() + "%"

        binding.textViewDownProbability.text = "하락 확률 : ${model.getProbability().down}%"

        binding.textViewKeepProbability.text = "유지 확률 : ${model.getProbability().keep}%"

        binding.imageViewRod.setBackgroundResource(model.getFishingRodImage())
        binding.buttonUpgrade.setBackgroundResource(model.getFishingRodImage())

    } // setUpgradeView()


    /*
    클릭 리스너 세팅.
     */
    fun setClickListener() {

        // 뒤로가기 버튼 클릭 이벤트.
        binding.imageViewBack.setOnClickListener {

            if (!model.isButtonClick()) {

                model.setButtonClickTrue()

                startMediaPlayer(R.raw.button_sound)
                binding.imageViewBack.startAnimation(buttonClickAnimation)

                thread {

                    Thread.sleep(150)

                    model.setButtonClickFalse()

                    Handler(Looper.getMainLooper()).post {
                        backToBefore()
                    }

                }

            }

        }


        // 강화탭에서 강화 버튼 클릭 이벤트.
        binding.buttonUpgrade.setOnClickListener {

            if (!model.isButtonClick()) {

                model.setButtonClickTrue()
                binding.buttonUpgrade.startAnimation(buttonClickAnimation)

                if(model.isUpgrade()) {

                    thread {
                        Thread.sleep(150)

                        startMediaPlayer(R.raw.upgrade_sound)

                        Handler(Looper.getMainLooper()).post {
                            binding.imageViewHammer.visibility = View.VISIBLE
                            binding.imageViewHammer.startAnimation(upgradeAnimation)
                        }

                        Thread.sleep(2000)
                        model.setButtonClickFalse()

                        Handler(Looper.getMainLooper()).post {
                            binding.imageViewHammer.visibility = View.GONE
                            binding.imageViewHammer.clearAnimation()

                            if (model.upgradeFishingRod()) {

                                binding.imageViewSuccess.visibility = View.VISIBLE
                                binding.imageViewSuccess.startAnimation(upgradeResultAnimation)
                                startMediaPlayer(R.raw.success_sound)

                            } else {

                                binding.imageViewFail.visibility = View.VISIBLE
                                binding.imageViewFail.startAnimation(upgradeResultAnimation)
                                startMediaPlayer(R.raw.fail_sound)

                            }

                        }

                        Thread.sleep(500)

                        Handler(Looper.getMainLooper()).post {
                            binding.imageViewFail.visibility = View.GONE
                            binding.imageViewSuccess.visibility = View.GONE
                            binding.imageViewFail.clearAnimation()
                            binding.imageViewSuccess.clearAnimation()
                            //업그레이드 한후 view 다시 세팅해주기
                            setUpgradeView()
                        }

                    }

                } else {

                    startMediaPlayer(R.raw.button_sound)
                    model.setButtonClickFalse()

                    if(preferences.getInt("rod",0) == 30){

                        Toast.makeText(this,"최대 강화 수치에 도달하셨습니다.",Toast.LENGTH_SHORT).show()

                    } else {

                        Toast.makeText(this,"필요한 KKON이 부족합니다",Toast.LENGTH_SHORT).show()

                    }

                }

            }

        }


        // 물고기 상세 정보 확인 버튼 클릭 이벤트.
        binding.buttonFishDetailCheck.setOnClickListener {

            startMediaPlayer(R.raw.button_sound)
            binding.linearLayoutFishDetail.startAnimation(layoutFadeOutAnimation)
            binding.linearLayoutFishDetail.visibility = View.GONE

        }

    } // setListener()


    /*
    뒤로가기 버튼 클릭 이벤트.
     */
    fun setOnBackPressed() {

        onBackPressedDispatcher.addCallback(this, object: OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {

                if(binding.linearLayoutUpgrade.visibility == View.VISIBLE){ // 화면에 홈 레이아웃 보일 떄

                    binding.imageViewBack.performClick()

                }

            }
        })

    } // setOnBackPressed()


    /*
    뒤로가기 버튼 클릭 시 현재 레이아웃 판별.
     */
    fun backToBefore() {

        if(binding.linearLayoutUpgrade.visibility == View.VISIBLE){ // 화면에 홈 레이아웃 보일 떄

            finish()

        }

    } // backToBefore()


    /*
    음악 재생
     */
    fun startMediaPlayer(music : Int) {

        val mediaPlayer = MediaPlayer.create(this, music)
        mediaPlayer.isLooping = false

        if (music == R.raw.button_sound) {
            mediaPlayer.setVolume(0.2f, 0.2f)
        } else {
            mediaPlayer.setVolume(0.05f, 0.05f)
        }

        mediaPlayer.setOnCompletionListener {
            mediaPlayer.release()
        }

        mediaPlayer.start()

    } // startMediaPlayer()


}