package abled.kkont.fishinggame.rank

import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import abled.kkont.fishinggame.R
import abled.kkont.fishinggame.adapter.InventoryAdapter
import abled.kkont.fishinggame.adapter.RankAdapter
import abled.kkont.fishinggame.data.Fish
import abled.kkont.fishinggame.data.Rank
import abled.kkont.fishinggame.databinding.ActivityRankBinding
import abled.kkont.fishinggame.network.NetworkStatus
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.activity.OnBackPressedCallback
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Response
import kotlin.concurrent.thread

class RankActivity : AppCompatActivity(), InventoryAdapter.ItemClickListener {

    lateinit var binding: ActivityRankBinding
    lateinit var rankAdapter: RankAdapter
    lateinit var inventoryAdapter: InventoryAdapter
    lateinit var model: RankModel
    lateinit var preferences: SharedPreferences

    private lateinit var buttonClickAnimation: Animation

    var index: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRankBinding.inflate(layoutInflater)
        setContentView(binding.root)

        overridePendingTransition(R.anim.layout_left_in, R.anim.layout_none)

        setVariable()
        requestTotalList()
        setClickListener()
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
    변수 초기화
     */
    fun setVariable() {

        preferences = getSharedPreferences("fishingGame", MODE_PRIVATE)
        model = RankModel(preferences)
        rankAdapter = RankAdapter()
        inventoryAdapter = InventoryAdapter(this)

        buttonClickAnimation = AnimationUtils.loadAnimation(this, R.anim.button_click_animation)

    } // setVariable()


    /*
    통신으로 랭킹데이터 전부 받아오는 메서드
    */
    private fun requestTotalList() {

        if (NetworkStatus().getNetworkStatus(this)) {

            model.requestRank().enqueue(object : retrofit2.Callback<ArrayList<Rank>> {

                override fun onResponse(
                    call: Call<ArrayList<Rank>>,
                    response: Response<ArrayList<Rank>>
                ) {

                    if (response.isSuccessful && response.body() != null) {

                        model.setTotalRankList(response.body()!!)

                        // 랭킹 첫화면을 낚시대 랭킹 화면으로
                        binding.linerLayoutFishPlace.visibility = View.GONE
                        binding.textViewRodRank.setBackgroundResource(R.drawable.background_rank_tab_choice)
                        binding.textViewKKONRank.setBackgroundResource(R.drawable.background_rank_tap)
                        binding.textViewFishRank.setBackgroundResource(R.drawable.background_rank_tap)
                        binding.textViewLakeRank.setBackgroundResource(R.drawable.background_rank_tap)
                        binding.textViewRiverRank.setBackgroundResource(R.drawable.background_rank_tap)
                        binding.textViewBeachRank.setBackgroundResource(R.drawable.background_rank_tap)
                        binding.textViewSeaRank.setBackgroundResource(R.drawable.background_rank_tap)

                        rankAdapter.rankList = model.setCategory("낚시대")

                        binding.textViewMyRank.text = model.myRank()

                        binding.recyclerViewRank.adapter = rankAdapter

                    } else {

                        binding.recyclerViewRank.visibility = View.GONE
                        binding.textViewNoNetwork.visibility = View.VISIBLE
                        Toast.makeText(this@RankActivity, "네트워크 연결을 확인해주세요", Toast.LENGTH_SHORT)
                            .show()

                    }

                }

                override fun onFailure(call: Call<ArrayList<Rank>>, t: Throwable) {

                    binding.recyclerViewRank.visibility = View.GONE
                    binding.textViewNoNetwork.visibility = View.VISIBLE
                    Toast.makeText(this@RankActivity, "네트워크 연결을 확인해주세요", Toast.LENGTH_SHORT).show()

                }

            })

        } else {

            binding.recyclerViewRank.visibility = View.GONE
            binding.textViewNoNetwork.visibility = View.VISIBLE
            Toast.makeText(this, "네트워크 연결을 확인해주세요", Toast.LENGTH_SHORT).show()

        }

    } // requestTotalList()


    /*
    액티비티 클릭 리스너 모음
    */
    fun setClickListener() {

        // 뒤로가기 버튼
        binding.imageViewBack.setOnClickListener {

            if (!model.isButtonClick()) {

                model.setButtonClickTrue()

                startMediaPlayer(R.raw.button_sound)
                binding.imageViewBack.startAnimation(buttonClickAnimation)

                thread {

                    Thread.sleep(150)

                    model.setButtonClickFalse()

                    Handler(Looper.getMainLooper()).post {

                        if (binding.linearLayoutInventory.visibility == View.VISIBLE) {
                            binding.textViewTitle.text = "랭킹"
                            binding.linearLayoutInventory.visibility = View.GONE
                            binding.linearLayoutRankTab.visibility = View.VISIBLE

                        } else {

                            finish()

                        }
                    }

                }

            }

        }


        // KKON 랭킹 등록할 때
        binding.buttonRegisterKKON.setOnClickListener {

            if (!model.isButtonClick()) {

                model.setButtonClickTrue()

                startMediaPlayer(R.raw.button_sound)
                binding.buttonRegisterKKON.startAnimation(buttonClickAnimation)

                thread {

                    Thread.sleep(150)

                    model.setButtonClickFalse()

                    Handler(Looper.getMainLooper()).post {
                        if (NetworkStatus().getNetworkStatus(this)) {

                            // 등록조건 만족하지 않을 때
                            if (!model.checkInputTextIsUpdateKKON(binding.editTextInsertKKON.text.toString())) {

                                Toast.makeText(
                                    this,
                                    model.inputUpdateKKONExceptionMessage(binding.editTextInsertKKON.text.toString()),
                                    Toast.LENGTH_SHORT
                                ).show()

                            } else {

                                model.updateKKONRank(
                                    rankAdapter.rankList,
                                    binding.editTextInsertKKON.text.toString()
                                ).enqueue(object : retrofit2.Callback<ArrayList<Rank>> {

                                    override fun onResponse(
                                        call: Call<ArrayList<Rank>>,
                                        response: Response<ArrayList<Rank>>
                                    ) {

                                        // 통신 성공 후 응답 있을 때
                                        if (response.isSuccessful && response.body() != null) {

                                            binding.linearLayoutInsertKKON.visibility = View.GONE

                                            model.setTotalRankList(response.body()!!)
                                            model.updateUserKKON(Integer.parseInt(binding.editTextInsertKKON.text.toString()))

                                            rankAdapter.rankList = model.setCategory("기부왕")

                                            binding.textViewMyRank.text = model.myRank()

                                            rankAdapter.rankList.sortByDescending {
                                                Integer.parseInt(
                                                    it.content
                                                )
                                            }
                                            rankAdapter.notifyDataSetChanged()

                                        } else {

                                            Toast.makeText(
                                                this@RankActivity,
                                                "네트워크 연결을 확인해주세요",
                                                Toast.LENGTH_SHORT
                                            ).show()

                                        }

                                    }

                                    override fun onFailure(
                                        call: Call<ArrayList<Rank>>,
                                        t: Throwable
                                    ) {

                                        Toast.makeText(
                                            this@RankActivity,
                                            "네트워크 연결을 확인해주세요",
                                            Toast.LENGTH_SHORT
                                        ).show()

                                    }

                                })

                            }

                        } else {
                            Toast.makeText(
                                this@RankActivity,
                                "네트워크 연결을 확인해주세요",
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                    }

                }

            }


        }

        // insertKKON 닫을 때
        binding.imageViewCancelInsertKKON.setOnClickListener {

            if (!model.isButtonClick()) {

                model.setButtonClickTrue()

                startMediaPlayer(R.raw.button_sound)
                binding.imageViewCancelInsertKKON.startAnimation(buttonClickAnimation)

                thread {

                    Thread.sleep(150)

                    model.setButtonClickFalse()

                    Handler(Looper.getMainLooper()).post {

                        binding.textViewRodRank.isEnabled = true
                        binding.textViewFishRank.isEnabled = true
                        binding.imageViewBack.isEnabled = true

                        binding.linearLayoutInsertKKON.visibility = View.GONE
                        binding.buttonInsertRank.isEnabled = true

                    }

                }

            }


        }

        // 랭킹 등록 버튼 눌렀을 때
        binding.buttonInsertRank.setOnClickListener {

            if (!model.isButtonClick()) {

                model.setButtonClickTrue()

                startMediaPlayer(R.raw.button_sound)
                binding.buttonInsertRank.startAnimation(buttonClickAnimation)

                thread {

                    Thread.sleep(150)

                    model.setButtonClickFalse()

                    Handler(Looper.getMainLooper()).post {

                        if (NetworkStatus().getNetworkStatus(this)) {

                            when (model.insertRodRank) {

                                true -> {

                                    if (model.selectType == "낚시대") {

                                        binding.buttonInsertRank.isEnabled = false
                                        binding.buttonInsertRank.text = "등록 완료"

                                        model.updateRodRank(rankAdapter.rankList)
                                            .enqueue(object : retrofit2.Callback<ArrayList<Rank>> {
                                                override fun onResponse(
                                                    call: Call<ArrayList<Rank>>,
                                                    response: Response<ArrayList<Rank>>
                                                ) {

                                                    if (response.isSuccessful && response.body() != null) {

                                                        model.setTotalRankList(response.body()!!)
                                                        rankAdapter.rankList =
                                                            model.setCategory("낚시대")

                                                        binding.textViewMyRank.text = model.myRank()

                                                        rankAdapter.notifyDataSetChanged()

                                                    } else {

                                                    }

                                                }

                                                override fun onFailure(
                                                    call: Call<ArrayList<Rank>>,
                                                    t: Throwable
                                                ) {

                                                    Toast.makeText(this@RankActivity, "네트워크 연결을 확인해주세요", Toast.LENGTH_SHORT).show()

                                                }

                                            })
                                    }

                                }

                                false -> {}

                            }

                            when (model.selectType) {

                                // 기부왕에서 랭킹등록 눌렀을 때
                                "기부왕" -> {

                                    binding.textViewUserKKON.text = "${model.getKKON()}"
                                    binding.linearLayoutInsertKKON.visibility = View.VISIBLE
                                    binding.textViewMyRank.text = model.myRank()

                                }

                                // 물고기종류에서 랭킹등록 눌렀을 때
                                "호수", "강", "바닷가", "태평양" -> {

                                    inventoryAdapter.inventoryItemList = model.setInventory()

                                    if (inventoryAdapter.inventoryItemList.size == 0) {

                                        binding.recyclerViewInventory.visibility = View.GONE
                                        binding.imageViewEmpty.visibility = View.VISIBLE

                                    } else {

                                        binding.recyclerViewInventory.visibility = View.VISIBLE
                                        binding.imageViewEmpty.visibility = View.GONE

                                    }

                                    binding.recyclerViewInventory.adapter = inventoryAdapter

                                    binding.textViewTitle.text = "인벤토리"
                                    binding.linearLayoutInventory.visibility = View.VISIBLE
                                    binding.linearLayoutRankTab.visibility = View.GONE

                                    binding.textViewMyRank.text = model.myRank()

                                }

                            }

                        } else {

                            Toast.makeText(this@RankActivity, "네트워크 연결을 확인해주세요", Toast.LENGTH_SHORT).show()

                        }

                    }

                }

            }

        }

        // 낚시대 랭킹탭 눌렀을 때
        binding.textViewRodRank.setOnClickListener {

            if (!model.isButtonClick()) {

                model.setButtonClickTrue()

                startMediaPlayer(R.raw.button_sound)
                binding.textViewRodRank.startAnimation(buttonClickAnimation)

                thread {

                    Thread.sleep(150)

                    model.setButtonClickFalse()

                    Handler(Looper.getMainLooper()).post {

                        if (!model.insertRodRank) {

                            binding.buttonInsertRank.isEnabled = false
                            binding.buttonInsertRank.text = "등록 완료"

                        }

                        binding.linerLayoutFishPlace.visibility = View.GONE
                        binding.textViewRodRank.setBackgroundResource(R.drawable.background_rank_tab_choice)
                        binding.textViewKKONRank.setBackgroundResource(R.drawable.background_rank_tap)
                        binding.textViewFishRank.setBackgroundResource(R.drawable.background_rank_tap)
                        binding.textViewLakeRank.setBackgroundResource(R.drawable.background_rank_tap)
                        binding.textViewRiverRank.setBackgroundResource(R.drawable.background_rank_tap)
                        binding.textViewBeachRank.setBackgroundResource(R.drawable.background_rank_tap)
                        binding.textViewSeaRank.setBackgroundResource(R.drawable.background_rank_tap)

                        if (binding.buttonInsertRank.visibility == View.VISIBLE) {

                        }
                        rankAdapter.rankList = model.setCategory("낚시대")

                        binding.textViewMyRank.text = model.myRank()

                        rankAdapter.notifyDataSetChanged()

                    }

                }

            }


        }

        // 기부왕 랭킹 눌렀을 때
        binding.textViewKKONRank.setOnClickListener {

            if (!model.isButtonClick()) {

                model.setButtonClickTrue()

                startMediaPlayer(R.raw.button_sound)
                binding.textViewKKONRank.startAnimation(buttonClickAnimation)

                thread {

                    Thread.sleep(150)

                    model.setButtonClickFalse()

                    Handler(Looper.getMainLooper()).post {

                        binding.buttonInsertRank.isEnabled = true
                        binding.buttonInsertRank.text = "랭킹 등록"
                        binding.linerLayoutFishPlace.visibility = View.GONE

                        binding.textViewRodRank.setBackgroundResource(R.drawable.background_rank_tap)
                        binding.textViewKKONRank.setBackgroundResource(R.drawable.background_rank_tab_choice)
                        binding.textViewFishRank.setBackgroundResource(R.drawable.background_rank_tap)
                        binding.textViewLakeRank.setBackgroundResource(R.drawable.background_rank_tap)
                        binding.textViewRiverRank.setBackgroundResource(R.drawable.background_rank_tap)
                        binding.textViewBeachRank.setBackgroundResource(R.drawable.background_rank_tap)
                        binding.textViewSeaRank.setBackgroundResource(R.drawable.background_rank_tap)

                        rankAdapter.rankList = model.setCategory("기부왕")


                        binding.textViewMyRank.text = model.myRank()

                        rankAdapter.notifyDataSetChanged()

                    }

                }

            }


        }

        // 물고기 랭킹 눌렀을 때
        binding.textViewFishRank.setOnClickListener {

            if (!model.isButtonClick()) {

                model.setButtonClickTrue()

                startMediaPlayer(R.raw.button_sound)
                binding.textViewFishRank.startAnimation(buttonClickAnimation)

                thread {

                    Thread.sleep(150)

                    model.setButtonClickFalse()

                    Handler(Looper.getMainLooper()).post {

                        binding.buttonInsertRank.isEnabled = true
                        binding.buttonInsertRank.text = "랭킹 등록"
                        binding.linerLayoutFishPlace.visibility = View.VISIBLE

                        binding.textViewRodRank.setBackgroundResource(R.drawable.background_rank_tap)
                        binding.textViewKKONRank.setBackgroundResource(R.drawable.background_rank_tap)
                        binding.textViewFishRank.setBackgroundResource(R.drawable.background_rank_tab_choice)
                        binding.textViewLakeRank.setBackgroundResource(R.drawable.background_rank_tap)
                        binding.textViewRiverRank.setBackgroundResource(R.drawable.background_rank_tap)
                        binding.textViewBeachRank.setBackgroundResource(R.drawable.background_rank_tap)
                        binding.textViewSeaRank.setBackgroundResource(R.drawable.background_rank_tap)
                        binding.textViewLakeRank.setBackgroundResource(R.drawable.background_rank_tab_choice)

                        rankAdapter.rankList = model.setCategory("호수")

                        binding.textViewMyRank.text = model.myRank()

                        rankAdapter.notifyDataSetChanged()

                    }

                }

            }


        }

        // 호수 눌렀을 때
        binding.textViewLakeRank.setOnClickListener {

            if (!model.isButtonClick()) {

                model.setButtonClickTrue()

                startMediaPlayer(R.raw.button_sound)
                binding.textViewLakeRank.startAnimation(buttonClickAnimation)

                thread {

                    Thread.sleep(150)

                    model.setButtonClickFalse()

                    Handler(Looper.getMainLooper()).post {

                        binding.textViewRodRank.setBackgroundResource(R.drawable.background_rank_tap)
                        binding.textViewKKONRank.setBackgroundResource(R.drawable.background_rank_tap)
                        binding.textViewFishRank.setBackgroundResource(R.drawable.background_rank_tab_choice)
                        binding.textViewLakeRank.setBackgroundResource(R.drawable.background_rank_tab_choice)
                        binding.textViewRiverRank.setBackgroundResource(R.drawable.background_rank_tap)
                        binding.textViewBeachRank.setBackgroundResource(R.drawable.background_rank_tap)
                        binding.textViewSeaRank.setBackgroundResource(R.drawable.background_rank_tap)

                        rankAdapter.rankList = model.setCategory("호수")

                        binding.textViewMyRank.text = model.myRank()

                        rankAdapter.notifyDataSetChanged()

                    }

                }

            }

        }

        // 강 눌렀을 때
        binding.textViewRiverRank.setOnClickListener {

            if (!model.isButtonClick()) {

                model.setButtonClickTrue()

                startMediaPlayer(R.raw.button_sound)
                binding.textViewRiverRank.startAnimation(buttonClickAnimation)

                thread {

                    Thread.sleep(150)

                    model.setButtonClickFalse()

                    Handler(Looper.getMainLooper()).post {

                        binding.textViewRodRank.setBackgroundResource(R.drawable.background_rank_tap)
                        binding.textViewKKONRank.setBackgroundResource(R.drawable.background_rank_tap)
                        binding.textViewLakeRank.setBackgroundResource(R.drawable.background_rank_tap)
                        binding.textViewRiverRank.setBackgroundResource(R.drawable.background_rank_tab_choice)
                        binding.textViewBeachRank.setBackgroundResource(R.drawable.background_rank_tap)
                        binding.textViewSeaRank.setBackgroundResource(R.drawable.background_rank_tap)

                        rankAdapter.rankList = model.setCategory("강")

                        binding.textViewMyRank.text = model.myRank()

                        rankAdapter.notifyDataSetChanged()

                    }

                }

            }

        }

        // 바닷가 눌렀을 때
        binding.textViewBeachRank.setOnClickListener {

            if (!model.isButtonClick()) {

                model.setButtonClickTrue()

                startMediaPlayer(R.raw.button_sound)
                binding.textViewBeachRank.startAnimation(buttonClickAnimation)

                thread {

                    Thread.sleep(150)

                    model.setButtonClickFalse()

                    Handler(Looper.getMainLooper()).post {

                        binding.textViewRodRank.setBackgroundResource(R.drawable.background_rank_tap)
                        binding.textViewKKONRank.setBackgroundResource(R.drawable.background_rank_tap)
                        binding.textViewLakeRank.setBackgroundResource(R.drawable.background_rank_tap)
                        binding.textViewRiverRank.setBackgroundResource(R.drawable.background_rank_tap)
                        binding.textViewBeachRank.setBackgroundResource(R.drawable.background_rank_tab_choice)
                        binding.textViewSeaRank.setBackgroundResource(R.drawable.background_rank_tap)

                        rankAdapter.rankList = model.setCategory("바닷가")

                        binding.textViewMyRank.text = model.myRank()

                        rankAdapter.notifyDataSetChanged()

                    }

                }

            }

        }

        // 태평양 눌렀을 때
        binding.textViewSeaRank.setOnClickListener {

            if (!model.isButtonClick()) {

                model.setButtonClickTrue()

                startMediaPlayer(R.raw.button_sound)
                binding.textViewSeaRank.startAnimation(buttonClickAnimation)

                thread {

                    Thread.sleep(150)

                    model.setButtonClickFalse()

                    Handler(Looper.getMainLooper()).post {

                        binding.textViewRodRank.setBackgroundResource(R.drawable.background_rank_tap)
                        binding.textViewKKONRank.setBackgroundResource(R.drawable.background_rank_tap)
                        binding.textViewLakeRank.setBackgroundResource(R.drawable.background_rank_tap)
                        binding.textViewRiverRank.setBackgroundResource(R.drawable.background_rank_tap)
                        binding.textViewBeachRank.setBackgroundResource(R.drawable.background_rank_tap)
                        binding.textViewSeaRank.setBackgroundResource(R.drawable.background_rank_tab_choice)

                        rankAdapter.rankList = model.setCategory("태평양")

                        binding.textViewMyRank.text = model.myRank()

                        rankAdapter.notifyDataSetChanged()

                    }

                }

            }

        }

        binding.buttonUpdateFishRank.setOnClickListener { // 물고기 등록 눌렀을 때

            if (!model.isButtonClick()) {

                model.setButtonClickTrue()

                startMediaPlayer(R.raw.button_sound)
                binding.buttonUpdateFishRank.startAnimation(buttonClickAnimation)

                thread {

                    Thread.sleep(150)

                    model.setButtonClickFalse()

                    Handler(Looper.getMainLooper()).post {

                        if (NetworkStatus().getNetworkStatus(this)){
                            // 랭킹에 등록 되어있는지 먼저 체크 해서 랭킹에 없으면 바로 등록.

                            // 등록되어있는 기록이 있다.
                            if (model.checkIsExistRank(rankAdapter.rankList)) {

                                // 기존 등록했던 물고기보다 새로고른 물고기가 작은경우
                                if (!model.checkIsInsertFish(
                                        inventoryAdapter.inventoryItemList[index],
                                        rankAdapter.rankList
                                    )
                                ) {

                                    Toast.makeText(this, "기존에 등록했던 물고기보다 작습니다", Toast.LENGTH_SHORT)
                                        .show()

                                    // 기존 등록했던 물고기보다 새로고른 물고기가 큰경우
                                } else {

                                    val type =

                                        // 여기가 문제임
                                        model.checkType(model.fish.name)

                                    model.updateFishRank(
                                        "update",
                                        type,
                                        // 여기가 문제임
                                        model.fish.length.toString()

                                    ).enqueue(object : retrofit2.Callback<ArrayList<Rank>> {
                                        override fun onResponse(
                                            call: Call<ArrayList<Rank>>,
                                            response: Response<ArrayList<Rank>>
                                        ) {

                                            if (response.isSuccessful && response.body() != null) {

                                                model.fishUpdateSaveInventory(model.fish)

                                                binding.linearLayoutFishDetail.visibility = View.GONE
                                                binding.linearLayoutInventory.visibility = View.GONE
                                                binding.imageViewBack.isEnabled = true
                                                binding.textViewTitle.text = "랭킹"

                                                binding.linearLayoutRankTab.visibility = View.VISIBLE
                                                model.setTotalRankList(response.body()!!)
                                                rankAdapter.rankList =
                                                    model.setCategory(model.selectType)

                                                binding.textViewMyRank.text = model.myRank()

                                                rankAdapter.notifyDataSetChanged()

                                            } else {

                                                Toast.makeText(this@RankActivity, "네트워크 연결을 확인해주세요", Toast.LENGTH_SHORT).show()

                                            }

                                        }

                                        override fun onFailure(
                                            call: Call<ArrayList<Rank>>,
                                            t: Throwable
                                        ) {

                                            Toast.makeText(this@RankActivity, "네트워크 연결을 확인해주세요", Toast.LENGTH_SHORT).show()

                                        }

                                    })

                                }

                                // 등록되어 있는 기록이 없다. -> 랭킹에 등록 해준다. + 통신 성공하면 쉐어드에서 빼준다.
                            } else {

                                val type =
                                    model.checkType(model.fish.name)

                                model.updateFishRank(
                                    "insert",
                                    type,
                                    model.fish.length.toString()
                                ).enqueue(object : retrofit2.Callback<ArrayList<Rank>> {
                                    override fun onResponse(
                                        call: Call<ArrayList<Rank>>,
                                        response: Response<ArrayList<Rank>>
                                    ) {

                                        if (response.isSuccessful && response.body() != null) {

                                            model.fishUpdateSaveInventory(model.fish)

                                            binding.linearLayoutFishDetail.visibility = View.GONE
                                            binding.linearLayoutInventory.visibility = View.GONE
                                            binding.textViewTitle.text = "랭킹"

                                            binding.linearLayoutRankTab.visibility = View.VISIBLE
                                            model.setTotalRankList(response.body()!!)
                                            rankAdapter.rankList = model.setCategory(model.selectType)

                                            binding.textViewMyRank.text = model.myRank()

                                            rankAdapter.notifyDataSetChanged()

                                        } else {

                                            Toast.makeText(this@RankActivity, "네트워크 연결을 확인해주세요", Toast.LENGTH_SHORT).show()

                                        }

                                    }

                                    override fun onFailure(call: Call<ArrayList<Rank>>, t: Throwable) {

                                        Toast.makeText(this@RankActivity, "네트워크 연결을 확인해주세요", Toast.LENGTH_SHORT).show()

                                    }

                                })

                            }

                        } else {

                            Toast.makeText(this@RankActivity, "네트워크 연결을 확인해주세요", Toast.LENGTH_SHORT).show()

                        }

                    }

                }

            }

        }

        // 아이템 뷰에서 물고기 등록 취소 눌렀을 때
        binding.imageViewCancelInsertFish.setOnClickListener {

            if (!model.isButtonClick()) {

                model.setButtonClickTrue()

                startMediaPlayer(R.raw.button_sound)
                binding.imageViewCancelInsertFish.startAnimation(buttonClickAnimation)

                thread {

                    Thread.sleep(150)

                    model.setButtonClickFalse()

                    Handler(Looper.getMainLooper()).post {

                        binding.linearLayoutFishDetail.visibility = View.GONE
                        binding.imageViewBack.isEnabled = true

                    }

                }

            }

        }

    } // setClickListener ()


    /*
    아이템 클릭 리스너
    */
    override fun onClick(view: View, position: Int) {

        model.setSelectFish(model.inventoryHiddenFishList[position])

        if (!model.isButtonClick()) {

            index = position

            model.setButtonClickTrue()

            startMediaPlayer(R.raw.button_sound)
            view.startAnimation(buttonClickAnimation)

            thread {

                Thread.sleep(150)

                model.setButtonClickFalse()

                Handler(Looper.getMainLooper()).post {

                    binding.linearLayoutFishDetail.visibility = View.VISIBLE
                    binding.textViewFishDetailFishName.text = model.fish.name
                    binding.textViewFishDetailFishLength.text = "${model.fish.length}cm"
                    binding.textViewFishDetailFishPrice.text = "${model.fish.price}꼰"
                    binding.imageViewFishDetail.setImageResource(model.fish.icon)
                    binding.imageViewBack.isEnabled = false

                }

            }

        }

    } // onClick


    /*
    뒤로가기 눌렀을 때
    */
    fun setBackPressed() {

        val backPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {

                if (binding.linearLayoutFishDetail.visibility == View.VISIBLE) {

                    binding.imageViewCancelInsertFish.performClick()

                } else if (binding.linearLayoutInventory.visibility == View.VISIBLE) {

                    binding.imageViewBack.performClick()

                } else if (binding.linearLayoutInsertKKON.visibility == View.VISIBLE) {

                    binding.imageViewCancelInsertKKON.performClick()

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
    fun startMediaPlayer(music: Int) {

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