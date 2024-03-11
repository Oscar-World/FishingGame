package abled.kkont.fishinggame.shop

import abled.kkont.fishinggame.adapter.InventoryAdapter
import abled.kkont.fishinggame.R
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import abled.kkont.fishinggame.adapter.BuyItemAdapter
import abled.kkont.fishinggame.databinding.ActivityShopBinding
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import kotlin.concurrent.thread

class ShopActivity : AppCompatActivity(), InventoryAdapter.ItemClickListener,
    BuyItemAdapter.ItemClickListener {

    private lateinit var binding: ActivityShopBinding
    private lateinit var model: ShopModel
    private lateinit var inventoryAdapter: InventoryAdapter
    private lateinit var buyItemAdapter: BuyItemAdapter
    private lateinit var buttonClickAnimation: Animation
    private lateinit var layoutFadeInAnimation: Animation
    private lateinit var layoutFadeOutAnimation: Animation


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityShopBinding.inflate(layoutInflater)
        setContentView(binding.root)

        overridePendingTransition(R.anim.layout_left_in, R.anim.layout_none)

        setVariable()
        setListener()
        setOnBackPressed()

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
        model = ShopModel(preferences)
        model.loadInventory()

        inventoryAdapter = InventoryAdapter(this)
        buyItemAdapter = BuyItemAdapter(this, applicationContext)
        binding.recyclerViewSell.adapter = inventoryAdapter
        binding.recyclerViewBuy.adapter = buyItemAdapter
        inventoryAdapter.setItem(model.fishList)

        binding.textViewRodCount.text = "+${model.rod}"
        binding.textViewBagCount.text = "${model.fishList.size} / 40"
        binding.textViewUserKKON.text = "${preferences.getInt("KKON", 0)}꼰"

        layoutFadeInAnimation = AnimationUtils.loadAnimation(this, R.anim.layout_fade_in)
        layoutFadeOutAnimation = AnimationUtils.loadAnimation(this, R.anim.layout_fade_out)
        buttonClickAnimation = AnimationUtils.loadAnimation(this, R.anim.button_click_animation)

    } // setVariable()


    /*
    판매 아이템 클릭 이벤트.
     */
    override fun onClick(view: View, position: Int) {

        if (!model.isButtonClick()) {

            model.setButtonClickTrue()
            model.setStatusForButtonOk("팔기")
            model.setSellItemPosition(position)

            startMediaPlayer(R.raw.button_sound)
            view.startAnimation(buttonClickAnimation)
            binding.textViewCheckName.text = model.fishList[position].name
            binding.imageViewCheck.visibility = View.VISIBLE
            binding.imageViewCheck.setImageResource(model.fishList[position].icon)
            binding.textViewPrice.text = "판매가격 : ${model.fishList[position].price}"
            binding.buttonOk.text = "판매"

            thread {
                Thread.sleep(150)
                model.setButtonClickFalse()

                Handler(Looper.getMainLooper()).post {

                    binding.frameLayoutCheck.startAnimation(layoutFadeInAnimation)
                    binding.frameLayoutCheck.visibility = View.VISIBLE

                }
            }

        }

    } // onClick()


    /*
    구매 아이템 클릭 이벤트.
     */
    override fun onBuyItemClick(view: View, position: Int) {

        if (!model.isButtonClick()) {
            model.setButtonClickTrue()
            model.setStatusForButtonOk("사기")
            model.setBuyItemPosition(position)

            startMediaPlayer(R.raw.button_sound)
            view.startAnimation(buttonClickAnimation)
            binding.imageViewCheck.setImageResource(R.drawable.rod_wood)
            if (position == 0) {
                binding.textViewCheckName.text = "+10 낚싯대"
                binding.textViewPrice.text = "구매가격 : ${100000}"
            } else if (position == 1) {
                binding.textViewCheckName.text = "+15 낚싯대"
                binding.textViewPrice.text = "구매가격 : ${500000}"
            }
            binding.buttonOk.text = "구매"

            thread {
                Thread.sleep(150)
                model.setButtonClickFalse()
                Handler(Looper.getMainLooper()).post {
                    binding.frameLayoutCheck.startAnimation(layoutFadeInAnimation)
                    binding.frameLayoutCheck.visibility = View.VISIBLE
                }
            }

        }

    } // onBuyItemClick()


    /*
    클릭 리스너 세팅.
     */
    fun setListener() {

        // 사기 탭 클릭 이벤트.
        binding.buttonBuy.setOnClickListener {
            startMediaPlayer(R.raw.button_sound)
            binding.recyclerViewBuy.visibility = View.VISIBLE
            binding.recyclerViewSell.visibility = View.GONE
            binding.buttonTotalSell.visibility = View.GONE
        }


        // 팔기 탭 클릭 이벤트.
        binding.buttonSell.setOnClickListener {
            startMediaPlayer(R.raw.button_sound)
            binding.recyclerViewBuy.visibility = View.GONE
            binding.buttonTotalSell.visibility = View.VISIBLE

            if (model.fishList.size == 0) {
                binding.recyclerViewSell.visibility = View.GONE
                binding.linearLayoutEmpty.visibility = View.VISIBLE
            } else {
                binding.recyclerViewSell.visibility = View.VISIBLE
                binding.linearLayoutEmpty.visibility = View.GONE
            }

        }


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

                        finish()

                    }
                }
            }

        }


        // 취소 버튼 클릭 이벤트.
        binding.buttonCancel.setOnClickListener {

            if (!model.isButtonClick()) {

                model.setButtonClickTrue()

                startMediaPlayer(R.raw.button_sound)
                binding.buttonCancel.startAnimation(buttonClickAnimation)
                thread {
                    Thread.sleep(150)
                    model.setButtonClickFalse()

                    Handler(Looper.getMainLooper()).post {
                        binding.frameLayoutCheck.startAnimation(layoutFadeOutAnimation)
                        binding.frameLayoutCheck.visibility = View.GONE
                    }
                }

            }

        }


        // 전체 판매 버튼 클릭 이벤트.
        binding.buttonTotalSell.setOnClickListener {

            if (model.fishList.size != 0) {

                if (!model.isButtonClick()) {

                    model.setButtonClickTrue()
                    model.setStatusForButtonOk("전체 판매")

                    startMediaPlayer(R.raw.button_sound)
                    binding.buttonTotalSell.startAnimation(buttonClickAnimation)

                    thread {
                        Thread.sleep(150)
                        model.setButtonClickFalse()

                        Handler(Looper.getMainLooper()).post {
                            binding.frameLayoutCheck.startAnimation(layoutFadeInAnimation)
                            binding.frameLayoutCheck.visibility = View.VISIBLE
                            binding.textViewCheckName.text = "모든 물고기를 판매하시겠습니까?"
                            binding.imageViewCheck.setImageResource(R.drawable.fish_in_pail)
                            binding.textViewPrice.text = "판매가격 : ${model.totalSellPreView()}"
                            binding.buttonOk.text = "모두 판매"

                        }
                    }

                }

            }

        }


        // 사기 · 팔기 · 전체 판매 다이얼로그 확인 버튼 클릭 이벤트.
        binding.buttonOk.setOnClickListener {

            if (!model.isButtonClick()) {

                model.setButtonClickTrue()

                startMediaPlayer(R.raw.button_sound)
                binding.buttonOk.startAnimation(buttonClickAnimation)

                thread {
                    Thread.sleep(150)
                    model.setButtonClickFalse()

                    when (model.getStatusForButtonOk()) {

                        "사기" -> {
                            Handler(Looper.getMainLooper()).post {
                                binding.frameLayoutCheck.startAnimation(layoutFadeOutAnimation)

                                if (model.getBuyItemPosition() == 0) {
                                    if (model.rod < 10) {
                                        if (model.kkon >= 10000) {
                                            binding.textViewUserKKON.text =
                                                model.buyRod10().toString()
                                        } else {
                                            Toast.makeText(
                                                applicationContext,
                                                "꼰이 부족합니다.",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    } else if (model.rod == 10) {
                                        Toast.makeText(
                                            applicationContext,
                                            "현재 +10 낚시대를 사용중 입니다.",
                                            Toast.LENGTH_SHORT
                                        ).show()

                                    } else {
                                        Toast.makeText(
                                            applicationContext,
                                            "더 높은 강화단계를 갖고 있습니다.",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                } else if (model.getBuyItemPosition() == 1) {
                                    if (model.rod < 15) {
                                        if (model.kkon >= 50000) {
                                            binding.textViewUserKKON.text =
                                                model.buyRod15().toString()
                                        } else {
                                            Toast.makeText(
                                                applicationContext,
                                                "꼰이 부족합니다.",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    } else if (model.rod == 15) {
                                        Toast.makeText(
                                            applicationContext,
                                            "현재 +15 낚시대를 사용중 입니다.",
                                            Toast.LENGTH_SHORT
                                        ).show()

                                    } else {
                                        Toast.makeText(
                                            applicationContext,
                                            "더 높은 강화단계를 갖고 있습니다.",
                                            Toast.LENGTH_SHORT
                                        ).show()

                                    }
                                }
                                binding.frameLayoutCheck.visibility = View.GONE
                                binding.textViewRodCount.text = "+${model.rod}"

                            }
                        }

                        "팔기" -> {

                            Handler(Looper.getMainLooper()).post {

                                if(model.checkMaxKKONSell(model.getSellItemPosition())){

                                    Toast.makeText(this@ShopActivity, "보유할 수 있는 최대금액을 초과 하였습니다.", Toast.LENGTH_SHORT).show()
                                    binding.frameLayoutCheck.visibility = View.GONE
                                    return@post

                                }

                                binding.frameLayoutCheck.startAnimation(layoutFadeOutAnimation)

                                binding.textViewUserKKON.text =
                                    "${model.sellInventory(model.getSellItemPosition())}꼰"

                                inventoryAdapter.setItem(model.fishList)

                                binding.frameLayoutCheck.visibility = View.GONE
                                binding.textViewBagCount.text = "${model.fishList.size} / 40"

                                if (model.fishList.size == 0) {

                                    binding.recyclerViewSell.visibility = View.GONE
                                    binding.linearLayoutEmpty.visibility = View.VISIBLE

                                }


                            }
                        }

                        "전체 판매" -> {

                            Handler(Looper.getMainLooper()).post {

                                if(model.checkMaxKKONTotalSell()){

                                    Toast.makeText(this@ShopActivity, "보유할 수 있는 최대금액을 초과 하였습니다.", Toast.LENGTH_SHORT).show()
                                    binding.frameLayoutCheck.visibility = View.GONE
                                    return@post

                                }

                                binding.frameLayoutCheck.startAnimation(layoutFadeOutAnimation)
                                binding.textViewUserKKON.text =
                                    "${model.totalSellInventory().toString()}꼰"
                                model.loadInventory()
                                inventoryAdapter.setItem(model.fishList)
                                binding.frameLayoutCheck.visibility = View.GONE
                                binding.textViewBagCount.text = "${model.fishList.size} / 40"

                                if (model.fishList.size == 0) {
                                    binding.recyclerViewSell.visibility = View.GONE
                                    binding.linearLayoutEmpty.visibility = View.VISIBLE

                                } else {
                                    binding.recyclerViewSell.visibility = View.VISIBLE
                                    binding.linearLayoutEmpty.visibility = View.GONE

                                }

                            }
                        }

                    }
                }

            }

        }


    } // setListener()


    /*
   뒤로가기 버튼 클릭 이벤트.
    */
    fun setOnBackPressed() {

        onBackPressedDispatcher.addCallback(this, object: OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {

                if(binding.frameLayoutCheck.visibility == View.VISIBLE){ // 화면에 홈 레이아웃 보일 떄

                    binding.buttonCancel.performClick()

                } else {

                    binding.imageViewBack.performClick()

                }

            }
        })

    } // setOnBackPressed()


    /*
    음악 재생
     */
    fun startMediaPlayer(music: Int) {

        val mediaPlayer = MediaPlayer.create(this, music)
        mediaPlayer.isLooping = false
        mediaPlayer.setVolume(0.2f, 0.2f)

        mediaPlayer.setOnCompletionListener {
            mediaPlayer.release()
        }

        mediaPlayer.start()

    } // startMediaPlayer()


}