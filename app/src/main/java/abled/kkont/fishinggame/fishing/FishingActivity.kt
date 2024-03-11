package abled.kkont.fishinggame.fishing

import abled.kkont.fishinggame.adapter.InventoryAdapter
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import abled.kkont.fishinggame.R
import abled.kkont.fishinggame.databinding.ActivityFishingBinding
import android.content.Context
import android.media.MediaPlayer
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.activity.OnBackPressedCallback
import kotlin.concurrent.thread

class FishingActivity : AppCompatActivity(), InventoryAdapter.ItemClickListener {

    private lateinit var binding : ActivityFishingBinding
    private lateinit var model : FishingModel

    private lateinit var fishCatchGameAnimation : Animation
    private lateinit var fishingThread: Thread
    private lateinit var fishCatchGameThread: Thread
    private lateinit var inventoryAdapter : InventoryAdapter

    private lateinit var buttonClickAnimation : Animation
    private lateinit var ggeeAnimation : Animation
    private lateinit var ggeeThrowAnimation : Animation
    private lateinit var ggeeDownAnimation : Animation
    private lateinit var layoutFadeInAnimation : Animation
    private lateinit var layoutFadeOutAnimation: Animation
    private lateinit var textUpAnimation: Animation
    private lateinit var vibrator: Vibrator


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityFishingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        overridePendingTransition(R.anim.layout_left_in, R.anim.layout_none)

        setVariable()
        setInventory()
        setField()
        setListener()
        setBackPressed()

    } // onCreate()


    /*
    액티비티 종료 시 애니메이션 추가.
     */
    override fun onPause() {
        super.onPause()

        overridePendingTransition(R.anim.layout_none, R.anim.layout_right_out)

    } // onPause()


    /*
    변수 초기화 작업
     */
    private fun setVariable() {

        val preferences = getSharedPreferences("fishingGame", MODE_PRIVATE)

        val intent = intent
        val field = intent.getStringExtra("field").toString()

        model = FishingModel(preferences, field)
        model.loadInventory()
        model.loadCollection()

        inventoryAdapter = InventoryAdapter(this)
        binding.recyclerViewInventory.adapter = inventoryAdapter
        inventoryAdapter.setItem(model.fishList)

        buttonClickAnimation = AnimationUtils.loadAnimation(this, R.anim.button_click_animation)
        ggeeAnimation = AnimationUtils.loadAnimation(this, R.anim.ggee_animation)
        ggeeThrowAnimation = AnimationUtils.loadAnimation(this, R.anim.ggee_throw_animation)
        ggeeDownAnimation = AnimationUtils.loadAnimation(this, R.anim.ggee_down_animation)
        layoutFadeInAnimation = AnimationUtils.loadAnimation(this, R.anim.layout_fade_in)
        layoutFadeOutAnimation = AnimationUtils.loadAnimation(this, R.anim.layout_fade_out)
        textUpAnimation = AnimationUtils.loadAnimation(this, R.anim.lock_text_animation)

    } // setVariable()


    /*
    사용자가 선택한 필드 + 확률 부스트에 따른 배경 세팅.
     */
    private fun setField() {

        val field = model.getBackground()

        when (field) {

            "호수 1" -> {
                binding.frameLayoutFishing.setBackgroundResource(R.drawable.lake_background)
                binding.imageViewTargetFish.setImageResource(R.drawable.lakefish)
            }
            "호수 2" -> {
                binding.frameLayoutFishing.setBackgroundResource(R.drawable.lake_background2)
            }
            "호수 3" -> {
                binding.frameLayoutFishing.setBackgroundResource(R.drawable.lake_background3)
            }
            "강 1" -> {
                binding.frameLayoutFishing.setBackgroundResource(R.drawable.river_background)
                binding.imageViewTargetFish.setImageResource(R.drawable.riverfish)
            }
            "강 2" -> {
                binding.frameLayoutFishing.setBackgroundResource(R.drawable.river_background2)
            }
            "강 3" -> {
                binding.frameLayoutFishing.setBackgroundResource(R.drawable.river_background3)
            }
            "바닷가 1" -> {
                binding.frameLayoutFishing.setBackgroundResource(R.drawable.beach_background)
                binding.imageViewTargetFish.setImageResource(R.drawable.beachfish)
            }
            "바닷가 2" -> {
                binding.frameLayoutFishing.setBackgroundResource(R.drawable.beach_background2)
            }
            "바닷가 3" -> {
                binding.frameLayoutFishing.setBackgroundResource(R.drawable.beach_background3)
            }
            "태평양 1" -> {
                binding.frameLayoutFishing.setBackgroundResource(R.drawable.sea_background)
                binding.imageViewTargetFish.setImageResource(R.drawable.seafish)
            }
            "태평양 2" -> {
                binding.frameLayoutFishing.setBackgroundResource(R.drawable.sea_background2)
            }
            "태평양 3" -> {
                binding.frameLayoutFishing.setBackgroundResource(R.drawable.sea_background3)
            }

        }

        when (field.substring(field.length-1, field.length)) {

            "1" -> {
                binding.buttonBoost.visibility = View.GONE
            }
            "2" -> {
                binding.buttonBoost.visibility = View.VISIBLE
                binding.buttonBoost.setImageResource(R.drawable.boost)
                binding.imageViewBoostInfo.setImageResource(R.drawable.boost)
                binding.textViewBoost.text = "부스트 적용 중"
                binding.textViewBoostPercent.text = "히든 물고기 등장 확률 상승"
            }
            "3" -> {
                binding.buttonBoost.setImageResource(R.drawable.superboost)
                binding.imageViewBoostInfo.setImageResource(R.drawable.superboost)
                binding.textViewBoost.text = "슈퍼 부스트 적용 중"
                binding.textViewBoostPercent.text = "히든 물고기 등장 확률 대폭 상승"
            }

        }

    } // setField()


    private fun setInventory() {

        if (model.isInventoryEmpty()) {
            binding.recyclerViewInventory.visibility = View.GONE
            binding.linearLayoutEmpty.visibility = View.VISIBLE
        } else {
            binding.recyclerViewInventory.visibility = View.VISIBLE
            binding.linearLayoutEmpty.visibility = View.GONE
        }

    } // setInventory()


    /*
    이벤트 리스너 세팅.
     */
    private fun setListener() {

        // 낚시하기 버튼 클릭 리스너
        binding.buttonFishing.setOnClickListener {

            if (!model.isInventoryFull()) {

                if (!model.isButtonClick()) {
                    model.setButtonClickTrue()

                    startMediaPlayer(R.raw.button_sound)
                    binding.buttonFishing.startAnimation(buttonClickAnimation)

                    thread {
                        Thread.sleep(150)

                        Handler(Looper.getMainLooper()).post {
                            startFishing()
                        }

                    }

                }

            } else {

                binding.textViewFullInventory.visibility = View.VISIBLE
                binding.textViewFullInventory.startAnimation(textUpAnimation)

                thread {

                    Thread.sleep(1500)

                    Handler(Looper.getMainLooper()).post {
                        binding.textViewFullInventory.visibility = View.GONE
                    }

                }

            }

        }


        // 챔질하기 버튼 클릭 리스너
        binding.buttonHooking.setOnClickListener {

            if (model.isBiting()) {

                if (model.isCatch()) {

                    startVibrate()

                    if (model.getFishingRodUpgrade() > 25) {

                        if (model.isHiddenFish()) {

                            model.setHiddenFish()
                            model.resetFishingCount()
                            model.resetBoostCount()

                        } else {

                            model.catchFishResult()
                            model.addFishingCount()
                            model.checkFishingCount()

                        }

                        model.saveInventory(model.fish)

                        startMediaPlayer(R.raw.fish_sound)
                        binding.imageViewFishingRod2.visibility = View.GONE
                        binding.buttonCatching.startAnimation(layoutFadeOutAnimation)
                        binding.linearLayoutFishResult.startAnimation(layoutFadeInAnimation)

                        thread {

                            Thread.sleep(200)
                            model.setButtonClickFalse()

                            Handler(Looper.getMainLooper()).post {

                                binding.buttonCatching.visibility = View.GONE
                                binding.linearLayoutFishResult.visibility = View.VISIBLE

                            }

                        }

                        binding.imageViewFishResult.setImageResource(model.fish.icon)
                        binding.textViewFishResultFishName.text = model.fish.name
                        binding.textViewFishResultFishPrice.text = model.fish.price.toString() + "꼰"

                        if (model.fish.length == 0) {

                            binding.textViewFishResultFishLength.visibility = View.GONE
                            binding.viewFishResultSpace.visibility = View.GONE

                        } else {

                            binding.textViewFishResultFishLength.visibility = View.VISIBLE
                            binding.viewFishResultSpace.visibility = View.VISIBLE
                            binding.textViewFishResultFishLength.text = model.fish.length.toString() + "cm"

                        }

                    } else {

                        startMediaPlayer(R.raw.button_sound)
                        binding.imageViewCatchNoti.visibility = View.GONE
                        binding.buttonCatching.visibility = View.INVISIBLE
                        binding.frameLayoutFishCatchGame.visibility = View.VISIBLE
                        binding.frameLayoutFishCatchGame.startAnimation(layoutFadeInAnimation)

                        thread {

                            Thread.sleep(200)

                            Handler(Looper.getMainLooper()).post {
                                startFishCatch()
                            }

                        }

                    }

                } else {

                    startMediaPlayer(R.raw.button_sound)
                    binding.imageViewFishingRod2.visibility = View.GONE
                    binding.linearLayoutTop.startAnimation(layoutFadeInAnimation)
                    binding.linearLayoutBottom.startAnimation(layoutFadeInAnimation)
                    binding.buttonInventory.startAnimation(layoutFadeInAnimation)
                    binding.buttonFishingRod.startAnimation(layoutFadeInAnimation)
                    binding.imageViewFishingRod1.startAnimation(layoutFadeInAnimation)
                    binding.imageViewCatchNoti.startAnimation(layoutFadeOutAnimation)
                    binding.buttonHooking.startAnimation(layoutFadeOutAnimation)

                    thread {

                        Thread.sleep(200)

                        Handler(Looper.getMainLooper()).post {

                            binding.buttonFishing.visibility = View.VISIBLE
                            binding.linearLayoutBottom.visibility = View.VISIBLE
                            binding.buttonFishingRod.visibility = View.VISIBLE
                            binding.buttonInventory.visibility = View.VISIBLE
                            binding.linearLayoutTop.visibility = View.VISIBLE
                            binding.imageViewFishingRod1.visibility = View.VISIBLE

                        }

                    }

                    model.setButtonClickFalse()

                }

                binding.buttonHooking.visibility = View.GONE
                binding.imageViewCatchNoti.visibility = View.GONE

            } else {

                if (fishingThread.isAlive) {
                    fishingThread.interrupt()
                }

            }

        }


        // 피쉬캐치 중 잡기 버튼 클릭 리스너
        binding.buttonCatching.setOnClickListener {

            if (fishCatchGameThread.isAlive) {
                fishCatchGameThread.interrupt()
            }

        }


        // 물고기 결과창 확인 버튼 클릭 리스너
        binding.buttonFishResultCheck.setOnClickListener {

            if (!model.isButtonClick()) {

                model.setButtonClickTrue()

                startMediaPlayer(R.raw.button_sound)
                binding.buttonFishResultCheck.startAnimation(buttonClickAnimation)

                thread {
                    Thread.sleep(150)

                    Handler(Looper.getMainLooper()).post {
                        binding.linearLayoutFishResult.startAnimation(layoutFadeOutAnimation)
                    }

                    Thread.sleep(200)

                    Handler(Looper.getMainLooper()).post {
                        binding.linearLayoutFishResult.visibility = View.GONE
                        binding.buttonFishing.visibility = View.VISIBLE
                        binding.linearLayoutBottom.visibility = View.VISIBLE
                        binding.linearLayoutTop.visibility = View.VISIBLE
                        binding.buttonFishingRod.visibility = View.VISIBLE
                        binding.buttonInventory.visibility = View.VISIBLE
                        binding.imageViewFishingRod1.visibility = View.VISIBLE

                        setField()

                        binding.linearLayoutBottom.startAnimation(layoutFadeInAnimation)
                        binding.linearLayoutTop.startAnimation(layoutFadeInAnimation)
                        binding.imageViewFishingRod1.startAnimation(layoutFadeInAnimation)
                    }

                    Thread.sleep(200)

                    model.setButtonClickFalse()

                }

            }

        }


        // 대상 어종 확인 버튼 클릭 리스너
        binding.imageViewInfo.setOnClickListener {

            if (!model.isButtonClick()) {

                model.setButtonClickTrue()

                startMediaPlayer(R.raw.button_sound)
                binding.imageViewInfo.startAnimation(buttonClickAnimation)

                thread {

                    Thread.sleep(150)

                    Handler(Looper.getMainLooper()).post {

                        binding.frameLayoutTargetFish.startAnimation(layoutFadeInAnimation)
                        binding.linearLayoutTop.startAnimation(layoutFadeOutAnimation)
                        binding.linearLayoutBottom.startAnimation(layoutFadeOutAnimation)
                        binding.imageViewFishingRod1.startAnimation(layoutFadeOutAnimation)
                        binding.buttonInventory.startAnimation(layoutFadeOutAnimation)
                        binding.buttonFishingRod.startAnimation(layoutFadeOutAnimation)

                        binding.frameLayoutTargetFish.visibility = View.VISIBLE
                        binding.linearLayoutBottom.visibility = View.GONE
                        binding.linearLayoutTop.visibility = View.GONE
                        binding.imageViewFishingRod1.visibility = View.GONE
                        binding.buttonInventory.visibility = View.GONE
                        binding.buttonFishingRod.visibility = View.GONE
                    }

                    Thread.sleep(200)

                    model.setButtonClickFalse()

                }

            }

        }


        // 대상 어종 확인 레이아웃 닫기 버튼 클릭 리스너
        binding.imageViewTargetFishLayoutCancel.setOnClickListener {

            if (!model.isButtonClick()) {

                model.setButtonClickTrue()

                startMediaPlayer(R.raw.button_sound)
                binding.imageViewTargetFishLayoutCancel.startAnimation(buttonClickAnimation)

                thread {
                    Thread.sleep(150)

                    Handler(Looper.getMainLooper()).post {
                        binding.frameLayoutTargetFish.startAnimation(layoutFadeOutAnimation)
                        binding.linearLayoutTop.startAnimation(layoutFadeInAnimation)
                        binding.linearLayoutBottom.startAnimation(layoutFadeInAnimation)
                        binding.imageViewFishingRod1.startAnimation(layoutFadeInAnimation)
                        binding.buttonInventory.startAnimation(layoutFadeInAnimation)
                        binding.buttonFishingRod.startAnimation(layoutFadeInAnimation)

                        binding.frameLayoutTargetFish.visibility = View.GONE
                        binding.linearLayoutBottom.visibility = View.VISIBLE
                        binding.linearLayoutTop.visibility = View.VISIBLE
                        binding.imageViewFishingRod1.visibility = View.VISIBLE
                        binding.buttonFishingRod.visibility = View.VISIBLE
                        binding.buttonInventory.visibility = View.VISIBLE
                    }

                    Thread.sleep(200)

                    model.setButtonClickFalse()

                }

            }

        }


        // 뒤로가기 버튼 클릭 리스너
        binding.imageViewBack.setOnClickListener {

            if (!model.isButtonClick()) {

                model.setButtonClickTrue()

                startMediaPlayer(R.raw.button_sound)
                binding.imageViewBack.startAnimation(buttonClickAnimation)

                thread {
                    Thread.sleep(150)

                    finish()

                }

            }

        }


        // 인벤토리 버튼 클릭 리스너
        binding.buttonInventory.setOnClickListener {

            if (!model.isButtonClick()) {

                model.setButtonClickTrue()

                startMediaPlayer(R.raw.button_sound)
                binding.buttonInventory.startAnimation(buttonClickAnimation)
                setInventory()

                thread {

                    Thread.sleep(150)

                    Handler(Looper.getMainLooper()).post {
                        binding.frameLayoutInventory.startAnimation(layoutFadeInAnimation)
                        binding.linearLayoutTop.startAnimation(layoutFadeOutAnimation)
                        binding.linearLayoutBottom.startAnimation(layoutFadeOutAnimation)
                        binding.imageViewFishingRod1.startAnimation(layoutFadeOutAnimation)
                        binding.buttonInventory.startAnimation(layoutFadeOutAnimation)
                        binding.buttonFishingRod.startAnimation(layoutFadeOutAnimation)

                        binding.frameLayoutInventory.visibility = View.VISIBLE
                        binding.linearLayoutBottom.visibility = View.GONE
                        binding.linearLayoutTop.visibility = View.GONE
                        binding.imageViewFishingRod1.visibility = View.GONE
                        binding.buttonFishingRod.visibility = View.GONE
                        binding.buttonInventory.visibility = View.GONE
                        binding.recyclerViewInventory.scrollToPosition(0)
                    }

                    Thread.sleep(200)

                    model.setButtonClickFalse()

                }

            }

        }


        // 인벤토리 레이아웃 닫기 버튼 클릭 리스너
        binding.imageViewInventoryLayoutCancel.setOnClickListener {

            if (!model.isButtonClick()) {

                model.setButtonClickTrue()

                startMediaPlayer(R.raw.button_sound)
                binding.imageViewInventoryLayoutCancel.startAnimation(buttonClickAnimation)

                thread {

                    Thread.sleep(150)

                    Handler(Looper.getMainLooper()).post {
                        binding.frameLayoutInventory.startAnimation(layoutFadeOutAnimation)
                        binding.linearLayoutTop.startAnimation(layoutFadeInAnimation)
                        binding.linearLayoutBottom.startAnimation(layoutFadeInAnimation)
                        binding.imageViewFishingRod1.startAnimation(layoutFadeInAnimation)
                        binding.buttonInventory.startAnimation(layoutFadeInAnimation)
                        binding.buttonFishingRod.startAnimation(layoutFadeInAnimation)

                        binding.frameLayoutInventory.visibility = View.GONE
                        binding.linearLayoutBottom.visibility = View.VISIBLE
                        binding.linearLayoutTop.visibility = View.VISIBLE
                        binding.imageViewFishingRod1.visibility = View.VISIBLE
                        binding.buttonInventory.visibility = View.VISIBLE
                        binding.buttonFishingRod.visibility = View.VISIBLE
                    }

                    Thread.sleep(200)

                    model.setButtonClickFalse()

                }

            }

        }


        // 물고기 자세히보기 확인 버튼 클릭 리스너
        binding.buttonFishDetailCheck.setOnClickListener {

            if (!model.isButtonClick()) {

                model.setButtonClickTrue()

                startMediaPlayer(R.raw.button_sound)
                binding.buttonFishDetailCheck.startAnimation(buttonClickAnimation)

                thread {

                    Thread.sleep(150)

                    Handler(Looper.getMainLooper()).post {
                        binding.linearLayoutFishDetail.startAnimation(layoutFadeOutAnimation)
                        binding.frameLayoutInventory.startAnimation(layoutFadeInAnimation)

                        binding.linearLayoutFishDetail.visibility = View.GONE
                        binding.frameLayoutInventory.visibility = View.VISIBLE
                    }

                    Thread.sleep(200)

                    model.setButtonClickFalse()

                }

            }

        }


        // 낚싯대 정보 버튼 클릭 리스너
        binding.buttonFishingRod.setOnClickListener {

            if (model.getFishingGameSpeed() == "") {
                binding.textViewFishingRodGameSpeed.text = "미니 게임 스킵"
            } else {
                binding.textViewFishingRodGameSpeed.text = "미니 게임 난이도 : " + model.getFishingGameSpeed()
            }

            binding.imageViewFishingRodInfo.setBackgroundResource(model.getFishingRodImage())
            binding.textViewFishingRodUpgrade.text = "강화 수치 : " + model.getFishingRodUpgrade() + "강"
            binding.textViewFishingRodPercent.text = "낚시 성공 확률 : " + model.getFishingPercent() + "%"

            if (!model.isButtonClick()) {

                model.setButtonClickTrue()

                startMediaPlayer(R.raw.button_sound)
                binding.buttonFishingRod.startAnimation(buttonClickAnimation)

                thread {

                    Thread.sleep(150)

                    Handler(Looper.getMainLooper()).post {
                        binding.frameLayoutFishingRod.startAnimation(layoutFadeInAnimation)
                        binding.linearLayoutTop.startAnimation(layoutFadeOutAnimation)
                        binding.linearLayoutBottom.startAnimation(layoutFadeOutAnimation)
                        binding.imageViewFishingRod1.startAnimation(layoutFadeOutAnimation)
                        binding.buttonInventory.startAnimation(layoutFadeOutAnimation)
                        binding.buttonFishingRod.startAnimation(layoutFadeOutAnimation)

                        binding.frameLayoutFishingRod.visibility = View.VISIBLE
                        binding.linearLayoutBottom.visibility = View.GONE
                        binding.linearLayoutTop.visibility = View.GONE
                        binding.imageViewFishingRod1.visibility = View.GONE
                        binding.buttonInventory.visibility = View.GONE
                        binding.buttonFishingRod.visibility = View.GONE
                    }

                    Thread.sleep(200)

                    model.setButtonClickFalse()

                }

            }

        }


        // 낚싯대 정보 레이아웃 닫기 버튼 클릭 리스너
        binding.imageViewFishingRodLayoutCancel.setOnClickListener {

            if (!model.isButtonClick()) {

                model.setButtonClickTrue()

                startMediaPlayer(R.raw.button_sound)
                binding.imageViewFishingRodLayoutCancel.startAnimation(buttonClickAnimation)

                thread {

                    Thread.sleep(150)

                    Handler(Looper.getMainLooper()).post {
                        binding.frameLayoutFishingRod.startAnimation(layoutFadeOutAnimation)
                        binding.linearLayoutTop.startAnimation(layoutFadeInAnimation)
                        binding.linearLayoutBottom.startAnimation(layoutFadeInAnimation)
                        binding.imageViewFishingRod1.startAnimation(layoutFadeInAnimation)
                        binding.buttonInventory.startAnimation(layoutFadeInAnimation)
                        binding.buttonFishingRod.startAnimation(layoutFadeInAnimation)

                        binding.frameLayoutFishingRod.visibility = View.GONE
                        binding.linearLayoutBottom.visibility = View.VISIBLE
                        binding.linearLayoutTop.visibility = View.VISIBLE
                        binding.imageViewFishingRod1.visibility = View.VISIBLE
                        binding.buttonInventory.visibility = View.VISIBLE
                        binding.buttonFishingRod.visibility = View.VISIBLE
                    }

                    Thread.sleep(200)

                    model.setButtonClickFalse()

                }

            }

        }


        /*
        부스트 버튼 클릭 리스너
         */
        binding.buttonBoost.setOnClickListener {

            if (!model.isButtonClick()) {

                model.setButtonClickTrue()

                startMediaPlayer(R.raw.button_sound)
                binding.buttonBoost.startAnimation(buttonClickAnimation)

                thread {

                    Thread.sleep(150)

                    Handler(Looper.getMainLooper()).post {
                        binding.frameLayoutBoost.startAnimation(layoutFadeInAnimation)
                        binding.linearLayoutTop.startAnimation(layoutFadeOutAnimation)
                        binding.linearLayoutBottom.startAnimation(layoutFadeOutAnimation)
                        binding.imageViewFishingRod1.startAnimation(layoutFadeOutAnimation)
                        binding.buttonInventory.startAnimation(layoutFadeOutAnimation)
                        binding.buttonFishingRod.startAnimation(layoutFadeOutAnimation)

                        binding.frameLayoutBoost.visibility = View.VISIBLE
                        binding.linearLayoutBottom.visibility = View.GONE
                        binding.linearLayoutTop.visibility = View.GONE
                        binding.imageViewFishingRod1.visibility = View.GONE
                        binding.buttonFishingRod.visibility = View.GONE
                        binding.buttonInventory.visibility = View.GONE
                    }

                    Thread.sleep(200)

                    model.setButtonClickFalse()

                }

            }

        }


        /*
        부스트 정보 닫기 버튼 클릭 리스너
         */
        binding.imageViewBoostLayoutCancel.setOnClickListener {

            if (!model.isButtonClick()) {

                model.setButtonClickTrue()

                startMediaPlayer(R.raw.button_sound)
                binding.imageViewBoostLayoutCancel.startAnimation(buttonClickAnimation)

                thread {

                    Thread.sleep(150)

                    Handler(Looper.getMainLooper()).post {
                        binding.frameLayoutBoost.startAnimation(layoutFadeOutAnimation)
                        binding.linearLayoutTop.startAnimation(layoutFadeInAnimation)
                        binding.linearLayoutBottom.startAnimation(layoutFadeInAnimation)
                        binding.imageViewFishingRod1.startAnimation(layoutFadeInAnimation)
                        binding.buttonInventory.startAnimation(layoutFadeInAnimation)
                        binding.buttonFishingRod.startAnimation(layoutFadeInAnimation)

                        binding.frameLayoutBoost.visibility = View.GONE
                        binding.linearLayoutBottom.visibility = View.VISIBLE
                        binding.linearLayoutTop.visibility = View.VISIBLE
                        binding.imageViewFishingRod1.visibility = View.VISIBLE
                        binding.buttonInventory.visibility = View.VISIBLE
                        binding.buttonFishingRod.visibility = View.VISIBLE
                    }

                    Thread.sleep(200)

                    model.setButtonClickFalse()

                }

            }

        }

    } // setListener()


    /*
    인벤토리 아이템 클릭 이벤트
     */
    override fun onClick(view: View, position: Int) {

        startMediaPlayer(R.raw.button_sound)

        binding.imageViewFishDetail.setImageResource(model.fishList[position].icon)
        binding.textViewFishDetailFishName.text = model.fishList[position].name
        binding.textViewFishDetailFishLength.text = model.fishList[position].length.toString() + "cm"
        binding.textViewFishDetailFishPrice.text = model.fishList[position].price.toString() + "꼰"

        if (model.fishList[position].length == 0) {
            binding.textViewFishDetailFishLength.visibility = View.GONE
            binding.viewFishDetailSpace.visibility = View.GONE
        } else {
            binding.textViewFishDetailFishLength.visibility = View.VISIBLE
            binding.viewFishDetailSpace.visibility = View.VISIBLE
        }

        binding.frameLayoutInventory.startAnimation(layoutFadeOutAnimation)
        binding.linearLayoutFishDetail.startAnimation(layoutFadeInAnimation)

        thread {

            Thread.sleep(200)

            Handler(Looper.getMainLooper()).post {

                binding.frameLayoutInventory.visibility = View.GONE
                binding.linearLayoutFishDetail.visibility = View.VISIBLE

            }

        }

    } // onClick()


    /*
    낚시 시작 하기 버튼 클릭 시점.
     */
    private fun startFishing() {

        model.startFishing()

        binding.buttonFishingRod.visibility = View.GONE
        binding.buttonInventory.visibility = View.GONE
        binding.buttonFishing.visibility = View.GONE
        binding.linearLayoutTop.visibility = View.GONE
        binding.imageViewFishingRod1.visibility = View.GONE
        binding.imageViewFishingRod2.visibility = View.VISIBLE

        binding.imageViewGgee.visibility = View.VISIBLE
        binding.imageViewGgee.startAnimation(ggeeThrowAnimation)

        fishingThread = thread {

            try {
                var time = model.getFishingTime()

                Thread.sleep(750)

                Handler(Looper.getMainLooper()).post {
                    binding.buttonHooking.visibility = View.VISIBLE
                }

                while(time != 0) {

                    Handler(Looper.getMainLooper()).post {
                        binding.imageViewGgee.startAnimation(ggeeAnimation)
                    }

                    Thread.sleep(1000)
                    time -= 1

                }

                Handler(Looper.getMainLooper()).post {
                    binding.imageViewGgee.clearAnimation()
                    binding.imageViewGgee.startAnimation(ggeeDownAnimation)
                    binding.imageViewGgee.visibility = View.GONE
                    binding.imageViewCatchNoti.visibility = View.VISIBLE
                }

                model.fishBite()

            } catch (e: InterruptedException) {

                startMediaPlayer(R.raw.button_sound)
                Handler(Looper.getMainLooper()).post {
                    binding.imageViewCatchNoti.visibility = View.GONE
                    binding.imageViewFishingRod2.visibility = View.GONE
                    binding.imageViewGgee.visibility = View.GONE
                    binding.buttonHooking.visibility = View.GONE
                    binding.buttonFishing.visibility = View.VISIBLE
                    binding.linearLayoutBottom.visibility = View.VISIBLE
                    binding.linearLayoutTop.visibility = View.VISIBLE
                    binding.buttonFishingRod.visibility = View.VISIBLE
                    binding.buttonInventory.visibility = View.VISIBLE
                    binding.imageViewFishingRod1.visibility = View.VISIBLE
                    binding.imageViewGgee.clearAnimation()
                    binding.linearLayoutBottom.startAnimation(layoutFadeInAnimation)
                    binding.linearLayoutTop.startAnimation(layoutFadeInAnimation)
                    binding.imageViewFishingRod1.startAnimation(layoutFadeInAnimation)
                    binding.buttonInventory.startAnimation(layoutFadeInAnimation)
                    binding.buttonFishingRod.startAnimation(layoutFadeInAnimation)
                }

                Thread.sleep(200)

                model.setButtonClickFalse()

            }

        }

    } // startFishing()


    /*
    물고기를 잡았다면 피쉬캐치 미니게임 시작.
    미니게임 성공 시 물고기 획득.
     */
    private fun startFishCatch() {

        when (model.getFishingRodUpgrade()) {

            in 0..5 -> fishCatchGameAnimation = AnimationUtils.loadAnimation(this, R.anim.fish_catch_game_animation_1000)
            in 6..10 -> fishCatchGameAnimation = AnimationUtils.loadAnimation(this, R.anim.fish_catch_game_animation_1250)
            in 11..15 -> fishCatchGameAnimation = AnimationUtils.loadAnimation(this, R.anim.fish_catch_game_animation_1500)
            in 16..20 -> fishCatchGameAnimation = AnimationUtils.loadAnimation(this, R.anim.fish_catch_game_animation_1750)
            in 21..25 -> fishCatchGameAnimation = AnimationUtils.loadAnimation(this, R.anim.fish_catch_game_animation_2000)

        }

        model.startAnimationRepeat()
        model.setFishCatchStart()

        binding.buttonCatching.visibility = View.VISIBLE
        binding.imageViewFishingRod2.visibility = View.GONE
        binding.imageViewFishingRod3.visibility = View.VISIBLE

        fishCatchGameThread = thread {

            try {

                while (model.isAnimationRepeat()) {

                    Handler(Looper.getMainLooper()).post {
                        binding.imageViewFishCatchGameFish.startAnimation(fishCatchGameAnimation)
                    }

                    Thread.sleep(model.getFishCatchGameTime())
                    model.addCycle()

                }

            } catch (e: InterruptedException) {

                model.setFishCatchStop()
                model.stopAnimationRepeat()
                model.setButtonClickFalse()

                Handler(Looper.getMainLooper()).post {

                    binding.frameLayoutFishCatchGame.visibility = View.GONE
                    binding.buttonCatching.visibility = View.GONE
                    binding.imageViewFishingRod3.visibility = View.GONE

                    if (model.getFishCatchTime() in model.getFishCatchGameMinTime()..model.getFishCatchGameMaxTime()) {

                        if (model.isHiddenFish()) {

                            model.setHiddenFish()
                            model.resetFishingCount()
                            model.resetBoostCount()

                        } else {

                            model.catchFishResult()
                            model.addFishingCount()
                            model.checkFishingCount()

                        }

                        model.saveInventory(model.fish)

                        binding.linearLayoutFishResult.startAnimation(layoutFadeInAnimation)
                        binding.linearLayoutFishResult.visibility = View.VISIBLE


                        startMediaPlayer(R.raw.fish_sound)
                        binding.imageViewFishResult.setImageResource(model.fish.icon)
                        binding.textViewFishResultFishName.text = model.fish.name
                        binding.textViewFishResultFishPrice.text = model.fish.price.toString() + "꼰"


                        if (model.fish.length == 0) {

                            binding.textViewFishResultFishLength.visibility = View.GONE
                            binding.viewFishResultSpace.visibility = View.GONE

                        } else {

                            binding.textViewFishResultFishLength.visibility = View.VISIBLE
                            binding.viewFishResultSpace.visibility = View.VISIBLE
                            binding.textViewFishResultFishLength.text = model.fish.length.toString() + "cm"

                        }

                    } else {

                        startMediaPlayer(R.raw.button_sound)
                        binding.linearLayoutBottom.startAnimation(layoutFadeInAnimation)
                        binding.linearLayoutTop.startAnimation(layoutFadeInAnimation)
                        binding.imageViewFishingRod1.startAnimation(layoutFadeInAnimation)
                        binding.buttonInventory.startAnimation(layoutFadeInAnimation)
                        binding.buttonFishingRod.startAnimation(layoutFadeInAnimation)

                        thread {

                            Thread.sleep(200)

                            Handler(Looper.getMainLooper()).post {

                                binding.buttonFishing.visibility = View.VISIBLE
                                binding.linearLayoutBottom.visibility = View.VISIBLE
                                binding.linearLayoutTop.visibility = View.VISIBLE
                                binding.buttonFishingRod.visibility = View.VISIBLE
                                binding.buttonInventory.visibility = View.VISIBLE
                                binding.imageViewFishingRod1.visibility = View.VISIBLE

                            }

                        }

                    }
                }
            }
        }

    } // startFishCatch()


    /*
    기기 뒤로가기 버튼 클릭 리스너.
     */
    fun setBackPressed() {

        val backPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {

                if (binding.frameLayoutInventory.visibility == View.VISIBLE) {

                    binding.imageViewInventoryLayoutCancel.performClick()

                } else if (binding.linearLayoutFishDetail.visibility == View.VISIBLE) {

                    binding.buttonFishDetailCheck.performClick()

                } else if (binding.linearLayoutFishResult.visibility == View.VISIBLE) {

                    binding.buttonFishResultCheck.performClick()

                } else if (binding.frameLayoutFishingRod.visibility == View.VISIBLE) {

                    binding.imageViewFishingRodLayoutCancel.performClick()

                } else if (binding.frameLayoutTargetFish.visibility == View.VISIBLE) {

                    binding.imageViewTargetFishLayoutCancel.performClick()

                } else if (binding.frameLayoutBoost.visibility == View.VISIBLE) {

                    binding.imageViewBoostLayoutCancel.performClick()

                } else {

                    binding.imageViewBack.performClick()

                }

            }

        }

        this.onBackPressedDispatcher.addCallback(this, backPressedCallback)

    } // setBackPressed()


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


    /*
    진동 울리기
     */
    fun startVibrate() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {

            val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibrator = vibratorManager.defaultVibrator

            vibrator.vibrate(VibrationEffect.createOneShot(500, 150))

        } else {

            @Suppress("DEPRECATION")
            vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

            @Suppress("DEPRECATION")
            vibrator.vibrate(500)

        }

    } // startVibrate()


}