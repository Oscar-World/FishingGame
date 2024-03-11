package abled.kkont.fishinggame.main

import abled.kkont.fishinggame.R
import abled.kkont.fishinggame.adapter.CollectionAdapter
import abled.kkont.fishinggame.adapter.InventoryAdapter
import abled.kkont.fishinggame.databinding.ActivityMainBinding
import abled.kkont.fishinggame.fishing.FishingActivity
import abled.kkont.fishinggame.home.HomeActivity
import abled.kkont.fishinggame.network.NetworkStatus
import abled.kkont.fishinggame.rank.RankActivity
import abled.kkont.fishinggame.shop.ShopActivity
import android.content.Intent
import android.content.SharedPreferences
import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import retrofit2.Call
import retrofit2.Response
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity(), InventoryAdapter.ItemClickListener{

    private lateinit var binding: ActivityMainBinding
    private lateinit var model : MainModel

    lateinit var inventoryAdapter: InventoryAdapter
    lateinit var collectionAdapter: CollectionAdapter

    private lateinit var buttonClickAnimation: Animation
    private lateinit var textAnimation : Animation
    private lateinit var leftInAnimation: Animation
    private lateinit var leftOutAnimation: Animation
    private lateinit var rightInAnimation: Animation
    private lateinit var rightOutAnimation: Animation
    private lateinit var layoutFadeInAnimation : Animation
    private lateinit var layoutFadeOutAnimation: Animation

    private var backPressedTime : Long = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        overridePendingTransition(R.anim.main_fade_in, R.anim.layout_none)

        setVariable()

    } // onCreate()


    override fun onStart() {
        super.onStart()

        setVariable()
        setView()
        setListener()
        setBackPressed()
        checkPreference()

    } // onStart()


    /*
    초기 변수 세팅.
     */
    fun setVariable() {

        val preferences = getSharedPreferences("fishingGame", MODE_PRIVATE)
        model = MainModel(preferences)

        buttonClickAnimation = AnimationUtils.loadAnimation(this, R.anim.button_click_animation)
        textAnimation = AnimationUtils.loadAnimation(this, R.anim.lock_text_animation)
        leftInAnimation = AnimationUtils.loadAnimation(this, R.anim.layout_left_in)
        leftOutAnimation = AnimationUtils.loadAnimation(this, R.anim.layout_left_out)
        rightInAnimation = AnimationUtils.loadAnimation(this, R.anim.layout_right_in)
        rightOutAnimation = AnimationUtils.loadAnimation(this, R.anim.layout_right_out)
        layoutFadeInAnimation = AnimationUtils.loadAnimation(this, R.anim.layout_fade_in)
        layoutFadeOutAnimation = AnimationUtils.loadAnimation(this, R.anim.layout_fade_out)

        binding.textViewRodCount.text = "+${model.getFishingRodUpgrade()}"
        binding.textViewBagCount.text = "${model.getInventorySize()} / 40"
        binding.textViewUserKKON.text = "${model.kkon}꼰"

        inventoryAdapter = InventoryAdapter(this)
        inventoryAdapter.inventoryItemList = model.getInventoryList()

        collectionAdapter = CollectionAdapter()
        collectionAdapter.collectionItemList = model.getCollectionList()

        binding.recyclerViewInventory.adapter = inventoryAdapter
        binding.recyclerViewCollection.adapter = collectionAdapter

        binding.textViewMaxCollectionCount.text = collectionAdapter.collectionItemList.size.toString()
        binding.textViewNowCollectionCount.text = model.collectionCount(collectionAdapter.collectionItemList).toString()

        if (model.getInventorySize() == 0) {

            binding.recyclerViewInventory.visibility = View.GONE
            binding.imageViewEmpty.visibility = View.VISIBLE

        } else {

            binding.recyclerViewInventory.visibility = View.VISIBLE
            binding.imageViewEmpty.visibility = View.GONE

        }

    } // setVariable()


    /*
    뷰 세팅.
     */
    fun setView() {

        when (model.getFishingRodUpgrade()) {

            in 0..9 -> {
                binding.imageViewRiver.visibility = View.GONE
                binding.imageViewBeach.visibility = View.GONE
                binding.imageViewSea.visibility = View.GONE
                binding.imageViewRiverLock.visibility = View.VISIBLE
                binding.imageViewBeachLock.visibility = View.VISIBLE
                binding.imageViewSeaLock.visibility = View.VISIBLE
            }
            in 10..19 -> {
                binding.imageViewBeach.visibility = View.GONE
                binding.imageViewSea.visibility = View.GONE
                binding.imageViewRiver.visibility = View.VISIBLE
                binding.imageViewBeachLock.visibility = View.VISIBLE
                binding.imageViewSeaLock.visibility = View.VISIBLE
                binding.imageViewRiverLock.visibility = View.GONE
            }
            in 20..24 -> {
                binding.imageViewRiver.visibility = View.VISIBLE
                binding.imageViewRiverLock.visibility = View.GONE
                binding.imageViewBeach.visibility = View.VISIBLE
                binding.imageViewBeachLock.visibility = View.GONE
                binding.imageViewSea.visibility = View.GONE
                binding.imageViewSeaLock.visibility = View.VISIBLE
            }
            in 25..30 -> {
                binding.imageViewSea.visibility = View.VISIBLE
                binding.imageViewBeach.visibility = View.VISIBLE
                binding.imageViewRiver.visibility = View.VISIBLE
                binding.imageViewSeaLock.visibility = View.GONE
                binding.imageViewBeachLock.visibility = View.GONE
                binding.imageViewRiverLock.visibility = View.GONE
            }

        }

    } // setView()


    /*
    클릭 리스너 세팅.
     */
    fun setListener() {

        // 랭킹 관리소 이미지 클릭
        binding.imageViewRanking.setOnClickListener {

            if (NetworkStatus().getNetworkStatus(this)) {

                if (!model.isButtonClick()) {

                    model.setButtonClickTrue()

                    startMediaPlayer(R.raw.button_sound)
                    binding.imageViewRanking.startAnimation(buttonClickAnimation)

                    thread {

                        Thread.sleep(150)

                        Handler(Looper.getMainLooper()).post {

                            binding.imageViewRanking.clearAnimation()

                        }

                        model.setButtonClickFalse()

                        val intent = Intent(this, RankActivity::class.java)
                        startActivity(intent)

                    }

                }

            } else {

                Toast.makeText(this, "네트워크 연결을 확인해주세요.", Toast.LENGTH_SHORT).show()

            }

        }

        // 홈 이미지 클릭
        binding.imageViewHome.setOnClickListener {

            if (!model.isButtonClick()) {

                model.setButtonClickTrue()

                startMediaPlayer(R.raw.button_sound)
                binding.imageViewHome.startAnimation(buttonClickAnimation)

                thread {

                    Thread.sleep(150)

                    Handler(Looper.getMainLooper()).post {

                        binding.imageViewHome.clearAnimation()

                    }

                    model.setButtonClickFalse()

                    val intent = Intent(this,HomeActivity::class.java)
                    startActivity(intent)

                }

            }

        }

        // 상점 이미지 클릭
        binding.imageViewShop.setOnClickListener {

            if (!model.isButtonClick()) {

                model.setButtonClickTrue()

                startMediaPlayer(R.raw.button_sound)
                binding.imageViewShop.startAnimation(buttonClickAnimation)

                thread {

                    Thread.sleep(150)

                    Handler(Looper.getMainLooper()).post {

                        binding.imageViewShop.clearAnimation()

                    }

                    model.setButtonClickFalse()

                    val intent = Intent(this, ShopActivity::class.java)
                    startActivity(intent)

                }

            }

        }

        // 낚시터 이미지 클릭
        binding.imageViewFishing.setOnClickListener { //imageViewFishing 클릭시

            if (!model.isButtonClick()) {

                model.setButtonClickTrue()

                startMediaPlayer(R.raw.button_sound)
                binding.imageViewFishing.startAnimation(buttonClickAnimation)

                thread {

                    Thread.sleep(150)

                    Handler(Looper.getMainLooper()).post {

                        binding.imageViewFishing.clearAnimation()

                        binding.frameLayoutMain.visibility = View.GONE
                        binding.frameLayoutFishingField.visibility = View.VISIBLE
                        binding.frameLayoutMain.startAnimation(leftOutAnimation)
                        binding.frameLayoutFishingField.startAnimation(leftInAnimation)

                    }

                    Thread.sleep(300)

                    Handler(Looper.getMainLooper()).post {

                        binding.frameLayoutMain.clearAnimation()
                        binding.frameLayoutFishingField.clearAnimation()

                    }

                    model.setButtonClickFalse()

                }

            }

        }

        // 호수 이미지 클릭
        binding.imageViewLake.setOnClickListener {

            if (!model.isButtonClick()) {

                model.setButtonClickTrue()

                startMediaPlayer(R.raw.button_sound)
                binding.imageViewLake.startAnimation(buttonClickAnimation)

                thread {

                    Thread.sleep(150)

                    Handler(Looper.getMainLooper()).post {

                        binding.imageViewLake.clearAnimation()

                    }

                    model.setButtonClickFalse()

                    val intent = Intent(this,FishingActivity::class.java)
                    intent.putExtra("field","호수")
                    startActivity(intent)

                }

            }

        }

        // 강 이미지 클릭
        binding.imageViewRiver.setOnClickListener {

            if (!model.isButtonClick()) {

                model.setButtonClickTrue()

                startMediaPlayer(R.raw.button_sound)
                binding.imageViewRiver.startAnimation(buttonClickAnimation)

                thread {

                    Thread.sleep(150)

                    Handler(Looper.getMainLooper()).post {

                        binding.imageViewRiver.clearAnimation()

                    }

                    model.setButtonClickFalse()

                    val intent = Intent(this,FishingActivity::class.java)
                    intent.putExtra("field","강")
                    startActivity(intent)

                }

            }

        }

        // 바닷가 이미지 클릭
        binding.imageViewBeach.setOnClickListener {

            if (!model.isButtonClick()) {

                model.setButtonClickTrue()

                startMediaPlayer(R.raw.button_sound)
                binding.imageViewBeach.startAnimation(buttonClickAnimation)

                thread {

                    Thread.sleep(150)

                    Handler(Looper.getMainLooper()).post {

                        binding.imageViewBeach.clearAnimation()

                    }

                    model.setButtonClickFalse()

                    val intent = Intent(this,FishingActivity::class.java)
                    intent.putExtra("field","바닷가")
                    startActivity(intent)

                }

            }

        }

        // 태평양 이미지 클릭
        binding.imageViewSea.setOnClickListener {

            if (!model.isButtonClick()) {

                model.setButtonClickTrue()

                startMediaPlayer(R.raw.button_sound)
                binding.imageViewSea.startAnimation(buttonClickAnimation)

                thread {

                    Thread.sleep(150)

                    Handler(Looper.getMainLooper()).post {

                        binding.imageViewSea.clearAnimation()

                    }

                    model.setButtonClickFalse()

                    val intent = Intent(this,FishingActivity::class.java)
                    intent.putExtra("field","태평양")
                    startActivity(intent)

                }

            }

        }

        // 낚시필드 뒤로가기 이미지 클릭
        binding.imageViewBack.setOnClickListener{ //imageViewBack 클릭시

            if (!model.isButtonClick()) {

                model.setButtonClickTrue()

                startMediaPlayer(R.raw.button_sound)
                binding.imageViewBack.startAnimation(buttonClickAnimation)

                thread {

                    Thread.sleep(150)

                    Handler(Looper.getMainLooper()).post {

                        binding.imageViewBack.clearAnimation()
                        binding.frameLayoutMain.visibility = View.VISIBLE
                        binding.frameLayoutFishingField.visibility = View.GONE
                        binding.frameLayoutMain.startAnimation(rightInAnimation)
                        binding.frameLayoutFishingField.startAnimation(rightOutAnimation)

                    }

                    Thread.sleep(300)

                    Handler(Looper.getMainLooper()).post {

                        binding.frameLayoutMain.clearAnimation()
                        binding.frameLayoutFishingField.clearAnimation()

                    }

                    model.setButtonClickFalse()

                }

            }

        }

        // 인벤토리 뒤로가기 이미지 클릭
        binding.imageViewInventoryBack.setOnClickListener{ //imageViewBack 클릭시

            if (!model.isButtonClick()) {

                model.setButtonClickTrue()

                startMediaPlayer(R.raw.button_sound)
                binding.imageViewInventoryBack.startAnimation(buttonClickAnimation)

                thread {

                    Thread.sleep(150)

                    Handler(Looper.getMainLooper()).post {

                        binding.imageViewBack.clearAnimation()
                        binding.frameLayoutMain.visibility = View.VISIBLE
                        binding.linearLayoutInventory.visibility = View.GONE
                        binding.frameLayoutMain.startAnimation(rightInAnimation)
                        binding.linearLayoutInventory.startAnimation(rightOutAnimation)

                    }

                    Thread.sleep(300)

                    Handler(Looper.getMainLooper()).post {

                        binding.frameLayoutMain.clearAnimation()
                        binding.linearLayoutInventory.clearAnimation()
                    }

                    model.setButtonClickFalse()

                }

            }

        }

        // 도감 뒤로가기 이미지 클릭
        binding.imageViewCollectionBack.setOnClickListener{ //imageViewBack 클릭시

            if (!model.isButtonClick()) {

                model.setButtonClickTrue()

                startMediaPlayer(R.raw.button_sound)
                binding.imageViewCollectionBack.startAnimation(buttonClickAnimation)

                thread {

                    Thread.sleep(150)

                    Handler(Looper.getMainLooper()).post {

                        binding.imageViewBack.clearAnimation()
                        binding.frameLayoutMain.visibility = View.VISIBLE
                        binding.linearLayoutCollection.visibility = View.GONE
                        binding.frameLayoutMain.startAnimation(rightInAnimation)
                        binding.linearLayoutCollection.startAnimation(rightOutAnimation)

                    }

                    Thread.sleep(300)

                    Handler(Looper.getMainLooper()).post {

                        binding.frameLayoutMain.clearAnimation()
                        binding.linearLayoutCollection.clearAnimation()
                    }

                    model.setButtonClickFalse()

                }

            }

        }

        // 잠겨있는 강 이미지 클릭
        binding.imageViewRiverLock.setOnClickListener {

            if (!model.isButtonClick()) {

                model.setButtonClickTrue()

                startMediaPlayer(R.raw.button_sound)
                binding.imageViewRiverLock.startAnimation(buttonClickAnimation)
                binding.textViewRiverLockText.visibility = View.VISIBLE
                binding.textViewRiverLockText.startAnimation(textAnimation)

                thread {
                    Thread.sleep(1000)

                    Handler(Looper.getMainLooper()).post {

                        binding.imageViewRiverLock.clearAnimation()
                        binding.textViewRiverLockText.clearAnimation()
                        binding.textViewRiverLockText.visibility = View.GONE

                    }

                    model.setButtonClickFalse()

                }

            }

        }

        // 잠겨있는 바닷가 이미지 클릭
        binding.imageViewBeachLock.setOnClickListener {

            if (!model.isButtonClick()) {

                model.setButtonClickTrue()

                startMediaPlayer(R.raw.button_sound)
                binding.imageViewBeachLock.startAnimation(buttonClickAnimation)
                binding.textViewBeachLockText.visibility = View.VISIBLE
                binding.textViewBeachLockText.startAnimation(textAnimation)

                thread {
                    Thread.sleep(1000)

                    Handler(Looper.getMainLooper()).post {

                        binding.imageViewBeachLock.clearAnimation()
                        binding.textViewBeachLockText.clearAnimation()
                        binding.textViewBeachLockText.visibility = View.GONE

                    }

                    model.setButtonClickFalse()

                }

            }

        }

        // 잠겨있는 태평양 이미지 클릭
        binding.imageViewSeaLock.setOnClickListener {

            if (!model.isButtonClick()) {

                model.setButtonClickTrue()

                startMediaPlayer(R.raw.button_sound)
                binding.imageViewSeaLock.startAnimation(buttonClickAnimation)
                binding.textViewSeaLockText.visibility = View.VISIBLE
                binding.textViewSeaLockText.startAnimation(textAnimation)

                thread {
                    Thread.sleep(1000)

                    Handler(Looper.getMainLooper()).post {

                        binding.imageViewSeaLock.clearAnimation()
                        binding.textViewSeaLockText.clearAnimation()
                        binding.textViewSeaLockText.visibility = View.GONE

                    }

                    model.setButtonClickFalse()

                }

            }

        }


        // 닉네임 생성 서버 통신
        binding.buttonRegister.setOnClickListener {

            if (NetworkStatus().getNetworkStatus(this)) {

                if(model.isCheckNickname(binding.editTextNickname.text.toString())){

                    model.duplicateInputNickname(binding.editTextNickname.text.toString()).enqueue(object : retrofit2.Callback<String>{
                        override fun onResponse(call: Call<String>, response: Response<String>) {

                            if (response.isSuccessful && response.body() != null) {

                                if(response.body() == "nicknameExist"){ // 유저 아이디가 존재하는 경우

                                    Toast.makeText(this@MainActivity,"중복된 닉네임 입니다",Toast.LENGTH_SHORT).show()

                                } else {

                                    model.registerNickname(binding.editTextNickname.text.toString())
                                    binding.frameLayoutRegisterNickname.visibility = View.GONE

                                }

                            } else {

                                Toast.makeText(this@MainActivity, "네트워크 연결을 확인해주세요", Toast.LENGTH_SHORT).show()

                            }
                        }
                        override fun onFailure(call: Call<String>, t: Throwable) {

                            Toast.makeText(this@MainActivity, "네트워크 연결을 확인해주세요", Toast.LENGTH_SHORT).show()

                        }
                    })

                }else{
                    Toast.makeText(this@MainActivity, "닉네임을 입력해주세요.", Toast.LENGTH_SHORT).show()
                }

            } else {
                Toast.makeText(this@MainActivity, "네트워크 연결을 확인해주세요", Toast.LENGTH_SHORT).show()
            }

        }


        // 도감탭 버튼 클릭 이벤트.
        binding.imageViewCollection.setOnClickListener {

            if (!model.isButtonClick()) {

                model.setButtonClickTrue()

                startMediaPlayer(R.raw.button_sound)
                binding.imageViewCollection.startAnimation(buttonClickAnimation)

                thread {

                    Thread.sleep(150)

                    model.setButtonClickFalse()

                    Handler(Looper.getMainLooper()).post {
                        changeLayout("도감")
                    }

                }

            }

        }


        // 인벤토리탭 버튼 클릭 이벤트.
        binding.imageViewInventory.setOnClickListener {

            if (!model.isButtonClick()) {

                model.setButtonClickTrue()

                startMediaPlayer(R.raw.button_sound)
                binding.imageViewInventory.startAnimation(buttonClickAnimation)

                thread {

                    Thread.sleep(150)

                    model.setButtonClickFalse()

                    Handler(Looper.getMainLooper()).post {
                        changeLayout("인벤토리")
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
    기기 뒤로가기 버튼 클릭 리스너.
     */
    fun setBackPressed() {

        val backPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {

                if (binding.frameLayoutFishingField.visibility == View.VISIBLE) {

                    binding.imageViewBack.performClick()

                } else if(binding.linearLayoutCollection.visibility == View.VISIBLE) {

                    binding.imageViewCollectionBack.performClick()

                } else if(binding.linearLayoutFishDetail.visibility == View.VISIBLE) {

                    binding.linearLayoutFishDetail.visibility = View.GONE

                } else if(binding.linearLayoutInventory.visibility == View.VISIBLE) {

                    binding.imageViewInventoryBack.performClick()

                } else {

                    if (System.currentTimeMillis() - backPressedTime >= 2000) {

                        backPressedTime = System.currentTimeMillis()
                        Toast.makeText(this@MainActivity, "한 번 더 누르면 종료됩니다.", Toast.LENGTH_SHORT).show()

                    } else {
                        finish()
                    }

                }

            }

        }

        this.onBackPressedDispatcher.addCallback(this, backPressedCallback)

    } // setBackPressed()


    /*
    SharedPreference 닉네임 여부 확인.
     */
    fun checkPreference() {

        if (!model.isRegister()) {

            binding.frameLayoutRegisterNickname.visibility = View.VISIBLE

        }

    } // checkPreference()


    /*
    음악 재생
     */
    fun startMediaPlayer(music : Int) {

        val mediaPlayer = MediaPlayer.create(this, music)
        mediaPlayer.isLooping = false
        mediaPlayer.setVolume(0.2f, 0.2f)

        mediaPlayer.setOnCompletionListener {
            mediaPlayer.release()
        }

        mediaPlayer.start()

    } // startMediaPlayer()

    /*
    인벤토리 아이템 클릭 이벤트.
     */
    override fun onClick(view: View, position: Int) {

        startMediaPlayer(R.raw.button_sound)
        binding.linearLayoutFishDetail.startAnimation(layoutFadeInAnimation)
        binding.textViewFishDetailFishLength.text = "${inventoryAdapter.inventoryItemList[position].length}cm"
        binding.textViewFishDetailFishName.text = inventoryAdapter.inventoryItemList[position].name
        binding.textViewFishDetailFishPrice.text = inventoryAdapter.inventoryItemList[position].price.toString()
        binding.imageViewFishDetail.setImageResource(inventoryAdapter.inventoryItemList[position].icon)
        binding.linearLayoutFishDetail.visibility = View.VISIBLE

    } // onClick()



    /*
    레이아웃 변환.
    */
    fun changeLayout(action:String) {

        if(action.equals("도감")){

            binding.frameLayoutMain.visibility = View.GONE
            binding.linearLayoutCollection.visibility = View.VISIBLE

        } else if(action.equals("인벤토리")){

            binding.frameLayoutMain.visibility = View.GONE
            binding.linearLayoutInventory.visibility = View.VISIBLE

        }

    } // changeLayout()


}