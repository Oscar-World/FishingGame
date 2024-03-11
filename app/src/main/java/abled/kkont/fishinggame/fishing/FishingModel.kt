package abled.kkont.fishinggame.fishing

import abled.kkont.fishinggame.R
import abled.kkont.fishinggame.data.Fish
import android.content.SharedPreferences
import com.google.gson.Gson
import org.json.JSONArray
import org.json.JSONObject
import java.util.Random

class FishingModel(val preferences: SharedPreferences, val field: String) {

    val editor = preferences.edit()

    var fish = Fish("", 0, 0, 0)
    val fishList = ArrayList<Fish>()
    val collectionList = ArrayList<Fish>()

    private var startTime: Long = 0
    private var stopTime: Long = 0
    private var cycle: Int = 0

    private var biteStatus: Boolean = false
    private var animationRepeatStatus: Boolean = false
    private var buttonClickStatus : Boolean = false
    private var collectionNum = 0
    private var fishingCount = 0
    private var boostCount = 0


    /*
    인벤토리 가득 차면 낚시 불가.
    낚시할 수 있는지 반환.
     */
    fun isInventoryFull() : Boolean {

        return fishList.size >= 40

    } // isInventoryFull()


    /*
    인벤토리에 물고기가 없는지 체크하여 반환.
     */
    fun isInventoryEmpty() : Boolean {

        return fishList.size == 0

    } // isInventoryEmpty()


    /*
    낚시 시작 시 입질 기다리는 시간 반환.
    2초 ~ 5초의 시간 랜덤 지정.
     */
    fun getFishingTime(): Int {

        val random = Random()
        return (random.nextInt(4) + 2)
//        return (random.nextInt(2))
    } // getFishingTime()


    /*
    낚시 시작.
    입질이 올 때까지 false.
     */
    fun startFishing() {

        biteStatus = false

    } // startFishing()


    /*
    입질이 왔을 때.
     */
    fun fishBite() {

        biteStatus = true

    } // stopFishing()


    /*
    입질 여부 반환.
     */
    fun isBiting(): Boolean {

        return biteStatus

    } // isFishing()


    /*
    물고기 잡았다 or 놓쳤다 판별.
    물고기를 잡을 확률은 낚싯대의 강화 수치에 비례함.
     */
    fun isCatch(): Boolean {

        val success = 60 + getFishingRodUpgrade()

        val random = Random()
        val randomValue = random.nextInt(100) + 1

        return randomValue <= success

    } // isCatch()


    /*
    피쉬캐치 게임 시작 시간 세팅.
     */
    fun setFishCatchStart() {

        startTime = System.currentTimeMillis()
        cycle = 0

    } // setStartTime()


    /*
    피쉬캐치 게임 정지 시간 세팅.
     */
    fun setFishCatchStop() {

        stopTime = System.currentTimeMillis()

    } // setStopTime()


    /*
    피쉬캐치 게임 반복 횟수 추가.
     */
    fun addCycle() {

        cycle++

    } // setCycle()


    /*
    피쉬캐치 게임 애니메이션 반복.
     */
    fun startAnimationRepeat() {

        animationRepeatStatus = true

    } // startRepeat()


    /*
    피쉬캐치 게임 애니메이션 멈춤.
     */
    fun stopAnimationRepeat() {

        animationRepeatStatus = false

    } // stopAnimationRepeat()


    /*
    피쉬캐치 게임 반복 여부 반환.
     */
    fun isAnimationRepeat(): Boolean {

        return animationRepeatStatus

    } // isAnimationRepeat()


    /*
    피쉬캐치게임 중 물고기 위치 확인하는 시간 반환.
     */
    fun getFishCatchTime(): Long {

        return stopTime - startTime - (cycle * getFishCatchGameTime())

    } // getFishCatchTime()


    /*
    피쉬캐치게임 애니메이션 속도 반환.
    */
    fun getFishCatchGameTime() : Long {

        var time : Long = 0

        when (getFishingRodUpgrade()) {

            in 0..5 -> time = 1000
            in 6..10 -> time = 1250
            in 11..15 -> time = 1500
            in 16..20 -> time = 1750
            in 21..25 -> time = 2000
            in 26..30 -> time = 0

        }

        return time

    } // getFishCatchGameTime()


    /*
    피쉬캐치게임 성공 최저 속도 반환.
    */
    fun getFishCatchGameMinTime() : Long {

        var time : Long = 0

        when (getFishingRodUpgrade()) {

            in 0..5 -> time = 450
            in 6..10 -> time = 575
            in 11..15 -> time = 700
            in 16..20 -> time = 825
            in 21..25 -> time = 900
            in 26..30 -> time = 0

        }

        return time

    } // getFishCatchGameMinTime()


    /*
    피쉬캐치게임 성공 최대 속도 반환.
    */
    fun getFishCatchGameMaxTime() : Long {

        var time : Long = 0

        when (getFishingRodUpgrade()) {

            in 0..5 -> time = 600
            in 6..10 -> time = 725
            in 11..15 -> time = 850
            in 16..20 -> time = 1150
            in 21..25 -> time = 1200
            in 26..30 -> time = 0

        }

        return time

    } // getFishCatchGameMaxTime()


    /*
    잡은 물고기 랜덤 지정.
    필드에 따라 등장 물고기 다름.
    */
    fun catchFishResult() {

        var fish = Fish("", 0, 0, 0)

        when (field) {

            "호수" -> fish = lakeFishResult()
            "강" -> fish = riverFishResult()
            "바닷가" -> fish = beachFishResult()
            "태평양" -> fish = seaFishResult()

        }

        this.fish = fish

    } // catchFishResult()


    /*
    물고기 길이 랜덤 지정.
    잡은 물고기 종류에 따른 길이 반환.
    */
    fun getFishLength(fishName: String): Int {

        val random = Random()
        var length = 0

        when (fishName) {

            "피라미" -> {
                length = random.nextInt(9) + 2
            }

            "금붕어" -> {
                length = random.nextInt(6) + 5
            }

            "붕어" -> {
                length = random.nextInt(41) + 10
            }

            "잉어" -> {
                length = random.nextInt(86) + 15
            }

            "향어" -> {
                length = random.nextInt(51) + 20
            }

            "메기" -> {
                length = random.nextInt(36) + 15
            }

            "비단잉어" -> {
                length = random.nextInt(41) + 30
            }

            "미꾸라지" -> {
                length = random.nextInt(6) + 5
            }

            "배스" -> {
                length = random.nextInt(21) + 15
            }

            "연어" -> {
                length = random.nextInt(31) + 20
            }

            "장어" -> {
                length = random.nextInt(41) + 20
            }

            "쏘가리" -> {
                length = random.nextInt(51) + 20
            }

            "가물치" -> {
                length = random.nextInt(71) + 50
            }

            "볼락" -> {
                length = random.nextInt(11) + 10
            }

            "우럭" -> {
                length = random.nextInt(21) + 15
            }

            "광어" -> {
                length = random.nextInt(46) + 25
            }

            "참돔" -> {
                length = random.nextInt(36) + 25
            }

            "감성돔" -> {
                length = random.nextInt(31) + 30
            }

            "돌돔" -> {
                length = random.nextInt(36) + 20
            }

            "다금바리" -> {
                length = random.nextInt(56) + 45
            }

            "해파리" -> {
                length = random.nextInt(41) + 10
            }

            "오징어" -> {
                length = random.nextInt(21) + 10
            }

            "참치" -> {
                length = random.nextInt(51) + 50
            }

            "만새기" -> {
                length = random.nextInt(71) + 50
            }

            "청새치" -> {
                length = random.nextInt(81) + 70
            }

            "돌고래" -> {
                length = random.nextInt(101) + 100
            }

            "상어" -> {
                length = random.nextInt(151) + 100
            }

            "고래" -> {
                length = random.nextInt(1701) + 300
            }

        }

        return length

    } // getFishLength()


    /*
    물고기 가격 반환.
    물고기 길이 * 기본 금액 * 0.5 (밸런스 언제든 조절 가능)
    */
    fun getFishPrice(length: Int, basePrice: Int): Int {

        return (length * 0.2 * basePrice).toInt()

    } // getFishPrice()


    /*
    호수에서 잡히는 물고기 반환.
     */
    fun lakeFishResult(): Fish {

        val random = Random()
        val randomValue = random.nextInt(1000) + 1

        var fish = Fish("", 0, 0, 0)
        var fishName = ""
        var length = 0
        var price = 0

        when (randomValue) {

            in 1..100 -> {
                fishName = "신발"
                fish = Fish(fishName, 0, 10, R.drawable.shoe)
            }

            in 101..200 -> {
                fishName = "타이어"
                fish = Fish(fishName, 0, 15, R.drawable.tire)
            }

            in 201..400 -> {
                fishName = "피라미"
                length = getFishLength(fishName)
                price = getFishPrice(length, 100)
                fish = Fish(fishName, length, price, R.drawable.pirami)
            }

            in 401..600 -> {
                fishName = "금붕어"
                length = getFishLength(fishName)
                price = getFishPrice(length, 200)
                fish = Fish(fishName, length, price, R.drawable.geumbung_uh)
            }

            in 601..800 -> {
                fishName = "붕어"
                length = getFishLength(fishName)
                price = getFishPrice(length, 250)
                fish = Fish(fishName, length, price, R.drawable.bung_uh)
            }

            in 801..880 -> {
                fishName = "잉어"
                length = getFishLength(fishName)
                price = getFishPrice(length, 500)
                fish = Fish(fishName, length, price, R.drawable.ing_uh)
            }

            in 881..950 -> {
                fishName = "향어"
                length = getFishLength(fishName)
                price = getFishPrice(length, 400)
                fish = Fish(fishName, length, price, R.drawable.hyang_uh)
            }

            in 951..1000 -> {
                fishName = "메기"
                length = getFishLength(fishName)
                price = getFishPrice(length, 1000)
                fish = Fish(fishName, length, price, R.drawable.megi)
            }

        }

        return fish

    } // lakeFishResult()


    /*
    강에서 잡히는 물고기 반환.
     */
    fun riverFishResult(): Fish {

        val random = Random()
        val randomValue = random.nextInt(1000) + 1

        var fish = Fish("", 0, 0, 0)
        var fishName = ""
        var length = 0
        var price = 0

        when (randomValue) {

            in 1..120 -> {
                fishName = "캔"
                fish = Fish(fishName, 0, 20, R.drawable.can)
            }

            in 121..240 -> {
                fishName = "페트병"
                fish = Fish(fishName, 0, 10, R.drawable.pet)
            }

            in 241..490 -> {
                fishName = "미꾸라지"
                length = getFishLength(fishName)
                price = getFishPrice(length, 350)
                fish = Fish(fishName, length, price, R.drawable.mikkuraji)
            }

            in 491..740 -> {
                fishName = "배스"
                length = getFishLength(fishName)
                price = getFishPrice(length, 900)
                fish = Fish(fishName, length, price, R.drawable.baeseu)
            }

            in 741..850 -> {
                fishName = "연어"
                length = getFishLength(fishName)
                price = getFishPrice(length, 1200)
                fish = Fish(fishName, length, price, R.drawable.yeon_uh)
            }

            in 851..950 -> {
                fishName = "장어"
                length = getFishLength(fishName)
                price = getFishPrice(length, 1800)
                fish = Fish(fishName, length, price, R.drawable.jang_uh)
            }

            in 951..1000 -> {
                fishName = "쏘가리"
                length = getFishLength(fishName)
                price = getFishPrice(length, 3500)
                fish = Fish(fishName, length, price, R.drawable.ssogari)
            }

        }

        return fish

    } // riverFishResult()


    /*
    바닷가에서 잡히는 물고기 반환.
     */
    fun beachFishResult(): Fish {

        val random = Random()
        val randomValue = random.nextInt(1000) + 1

        var fish = Fish("", 0, 0, 0)
        var fishName = ""
        var length = 0
        var price = 0

        when (randomValue) {

            in 1..100 -> {
                fishName = "미역"
                fish = Fish(fishName, 0, 50, R.drawable.miyeok)
            }

            in 101..200 -> {
                fishName = "불가사리"
                fish = Fish(fishName, 0, 20, R.drawable.bulgasari)
            }

            in 201..450 -> {
                fishName = "볼락"
                length = getFishLength(fishName)
                price = getFishPrice(length, 800)
                fish = Fish(fishName, length, price, R.drawable.bollak)
            }

            in 451..650 -> {
                fishName = "우럭"
                length = getFishLength(fishName)
                price = getFishPrice(length, 1000)
                fish = Fish(fishName, length, price, R.drawable.ureok)
            }

            in 651..850 -> {
                fishName = "광어"
                length = getFishLength(fishName)
                price = getFishPrice(length, 1500)
                fish = Fish(fishName, length, price, R.drawable.gwang_uh)
            }

            in 851..900 -> {
                fishName = "참돔"
                length = getFishLength(fishName)
                price = getFishPrice(length, 2500)
                fish = Fish(fishName, length, price, R.drawable.chamdom)
            }

            in 901..950 -> {
                fishName = "감성돔"
                length = getFishLength(fishName)
                price = getFishPrice(length, 4000)
                fish = Fish(fishName, length, price, R.drawable.gamseongdom)
            }

            in 951..1000 -> {
                fishName = "돌돔"
                length = getFishLength(fishName)
                price = getFishPrice(length, 8000)
                fish = Fish(fishName, length, price, R.drawable.doldom)
            }

        }

        return fish

    } // beachFishResult()


    /*
    태평양에서 잡히는 물고기 반환.
     */
    fun seaFishResult(): Fish {

        val random = Random()
        val randomValue = random.nextInt(1000) + 1

        var fish = Fish("", 0, 0, 0)
        var fishName = ""
        var length = 0
        var price = 0

        when (randomValue) {

            in 1..100 -> {
                fishName = "빨대"
                fish = Fish(fishName, 0, 10, R.drawable.straw)
            }

            in 101..200 -> {
                fishName = "낡은 그물"
                fish = Fish(fishName, 0, 100, R.drawable.net)
            }

            in 201..450 -> {
                fishName = "해파리"
                length = getFishLength(fishName)
                price = getFishPrice(length, 1500)
                fish = Fish(fishName, length, price, R.drawable.haepari)
            }

            in 451..650 -> {
                fishName = "오징어"
                length = getFishLength(fishName)
                price = getFishPrice(length, 2500)
                fish = Fish(fishName, length, price, R.drawable.ojing_uh)
            }

            in 651..800 -> {
                fishName = "참치"
                length = getFishLength(fishName)
                price = getFishPrice(length, 5000)
                fish = Fish(fishName, length, price, R.drawable.chamchi)
            }

            in 801..880 -> {
                fishName = "만새기"
                length = getFishLength(fishName)
                price = getFishPrice(length, 8000)
                fish = Fish(fishName, length, price, R.drawable.mansaegi)
            }

            in 881..930 -> {
                fishName = "청새치"
                length = getFishLength(fishName)
                price = getFishPrice(length, 10000)
                fish = Fish(fishName, length, price, R.drawable.cheongsaechi)
            }

            in 931..970 -> {
                fishName = "돌고래"
                length = getFishLength(fishName)
                price = getFishPrice(length, 18000)
                fish = Fish(fishName, length, price, R.drawable.dolgorae)
            }

            in 971..1000 -> {
                fishName = "상어"
                length = getFishLength(fishName)
                price = getFishPrice(length, 28500)
                fish = Fish(fishName, length, price, R.drawable.sang_uh)
            }

        }

        return fish

    } // seaFishResult()


    /*
    SharedPreference에 있는 Inventory 로드.
    JSONArray -> ArrayList 변환.
    */
    fun loadInventory() {

        val inventory = preferences.getString("inventory", "")

        if (inventory != "") {

            val jsonArray = JSONArray(inventory)

            for (i in 0 until jsonArray.length()) {

                val jsonObject: JSONObject = jsonArray.get(i) as JSONObject

                fishList.add(
                    Fish(
                        jsonObject.get("name").toString(),
                        jsonObject.get("length").toString().toInt(),
                        jsonObject.get("price").toString().toInt(),
                        jsonObject.get("icon").toString().toInt()
                    )
                )

            }

        }

    } // loadInventory()


    /*
    SharedPreference에 Inventory 저장.
    ArrayList -> JsonArray로 변환하여 저장.
    */
    fun saveInventory(fish : Fish) {

        fishList.add(fish)

        val jsonArray = Gson().toJson(fishList)

        editor.putString("inventory", jsonArray)
        editor.commit()

        if (isNeedUpdateCollection(fish)) {

            updateCollection(fish)

        }

    } // saveData()


    /*
    SharedPreference에 있는 Collection 로드.
    JSONArray -> ArrayList 변환.
    */
    fun loadCollection() {

        val collection = preferences.getString("collection", "")

        if (collection != "") {

            val jsonArray = JSONArray(collection)

            for (i in 0 until jsonArray.length()) {

                val jsonObject: JSONObject = jsonArray.get(i) as JSONObject

                collectionList.add(
                    Fish(
                        jsonObject.get("name").toString(),
                        jsonObject.get("length").toString().toInt(),
                        jsonObject.get("price").toString().toInt(),
                        jsonObject.get("icon").toString().toInt()
                    )
                )

            }

        }

    } // loadInventory()


    /*
    업데이트 해야하는 물고기인지 판별.
     */
    fun isNeedUpdateCollection(fish : Fish) : Boolean {

        when (fish.name) {

            "피라미" -> collectionNum = 0
            "금붕어" -> collectionNum = 1
            "붕어" -> collectionNum = 2
            "잉어" -> collectionNum = 3
            "향어" -> collectionNum = 4
            "메기" -> collectionNum = 5
            "비단잉어" -> collectionNum = 6
            "미꾸라지" -> collectionNum = 7
            "배스" -> collectionNum = 8
            "연어" -> collectionNum = 9
            "장어" -> collectionNum = 10
            "쏘가리" -> collectionNum = 11
            "가물치" -> collectionNum = 12
            "볼락" -> collectionNum = 13
            "우럭" -> collectionNum = 14
            "광어" -> collectionNum = 15
            "참돔" -> collectionNum = 16
            "감성돔" -> collectionNum = 17
            "돌돔" -> collectionNum = 18
            "다금바리" -> collectionNum = 19
            "해파리" -> collectionNum = 20
            "오징어" -> collectionNum = 21
            "참치" -> collectionNum = 22
            "만새기" -> collectionNum = 23
            "청새치" -> collectionNum = 24
            "돌고래" -> collectionNum = 25
            "상어" -> collectionNum = 26
            "고래" -> collectionNum = 27

        }

        return collectionList[collectionNum].length < fish.length

    } // isNeedUpdateCollection()


    /*
    SharedPreference에 Collection 업데이트.
    ArrayList -> JsonArray로 변환하여 저장.
    */
    fun updateCollection(fish : Fish) {

        collectionList[collectionNum] = Fish(fish.name, fish.length, fish.price, fish.icon)

        val jsonArray = Gson().toJson(collectionList)

        editor.putString("collection", jsonArray)
        editor.commit()

    } // saveData()


    /*
    낚싯대 강화 수치 반환.
    */
    fun getFishingRodUpgrade() : Int {

        return preferences.getInt("rod", 0)

    } // getFishingRodUpgrade()


    /*
    낚싯대 강화 수치에 따른 적용 효과 - 낚시 성공 확률 반환.
    */
    fun getFishingPercent() : Int {

        return 60 + getFishingRodUpgrade()

    } // getFishingPercent()


    /*
    낚싯대 강화 수치에 따른 적용 효과 - 미니 게임 속도 반환.
    */
    fun getFishingGameSpeed() : String {

        var gameSpeed = ""

        when (getFishingRodUpgrade()) {

            in 0..5 -> gameSpeed = "★★★★★"
            in 6..10 -> gameSpeed = "★★★★☆"
            in 11..15 -> gameSpeed = "★★★☆☆"
            in 16..20 -> gameSpeed = "★★☆☆☆"
            in 21..25 -> gameSpeed = "★☆☆☆☆"
            in 26..30 -> gameSpeed = ""

        }

        return gameSpeed

    } // getFishingGameSpeed()


    /*
    낚싯대 강화 수치에 따른 적용 효과 - 낚싯대 이미지 반환.
    */
    fun getFishingRodImage() : Int {

        var fishingRodImage = 0

        when (getFishingRodUpgrade()) {

            0 -> fishingRodImage = R.drawable.dialog_background_normal
            in 1..9 -> fishingRodImage = R.drawable.dialog_background_magic
            in 10..19 -> fishingRodImage = R.drawable.dialog_background_rare
            in 20..29 -> fishingRodImage = R.drawable.dialog_background_unique
            30 -> fishingRodImage = R.drawable.dialog_background_legendary

        }

        return fishingRodImage

    } // getFishingRodImage()


    /*
    버튼 중복 클릭 방지용 Boolean 변수 초기화.
     */
    fun setButtonClickTrue() {

        buttonClickStatus = true

    } // setButtonClickTrue()


    /*
    버튼 중복 클릭 방지용 Boolean 변수 초기화.
     */
    fun setButtonClickFalse() {

        buttonClickStatus = false

    } // setButtonClickFalse()


    /*
    버튼 중복 클릭 방지용 Boolean 변수 반환.
     */
    fun isButtonClick() : Boolean {

        return buttonClickStatus

    } // isButtonClick()


    /*
    낚시 횟수 카운트 증가.
     */
    fun addFishingCount() {

        fishingCount++

    } // addFishingCount()


    /*
    낚시 횟수 카운트 초기화.
     */
    fun resetFishingCount() {

        fishingCount = 0

    } // resetFishingCount()


    /*
    낚시 횟수에 따른 부스트 증가.
     */
    fun checkFishingCount() {

        if (fishingCount == 10 || fishingCount == 30) {
            boostCount++
        }

    } // checkFishingCount()


    /*
    낚시 확률 부스트 초기화.
     */
    fun resetBoostCount() {

        boostCount = 0

    } // resetBoostCount()


    /*
    히든 물고기 등장 여부 반환.
     */
    fun isHiddenFish() : Boolean {

        var isHidden = false

        val random = Random()
        val randomValue = random.nextInt(1000) + 1

        when (boostCount) {

            0 -> isHidden = randomValue <= 5
            1 -> isHidden = randomValue <= 10
            2 -> isHidden = randomValue <= 20

        }

        return isHidden

    } // isHiddenFish()


    /*
    지역 별 히든 물고기 반환.
     */
    fun setHiddenFish() {

        var fishName = ""
        var length = 0
        var price = 0

        when (field) {

            "호수" -> {
                fishName = "비단잉어"
                length = getFishLength(fishName)
                price = getFishPrice(length, 13550)
                fish = Fish(fishName, length, price, R.drawable.bidaning_uh)
            }
            "강" -> {
                fishName = "가물치"
                length = getFishLength(fishName)
                price = getFishPrice(length, 17350)
                fish = Fish(fishName, length, price, R.drawable.gamulchi)
            }
            "바닷가" -> {
                fishName = "다금바리"
                length = getFishLength(fishName)
                price = getFishPrice(length, 35750)
                fish = Fish(fishName, length, price, R.drawable.dageumbari)
            }
            "태평양" -> {
                fishName = "고래"
                length = getFishLength(fishName)
                price = getFishPrice(length, 85250)
                fish = Fish(fishName, length, price, R.drawable.gorae)
            }

        }

    } // setHiddenFish()


    /*
    필드, 부스트에 따른 낚시터 배경 반환.
     */
    fun getBackground() : String {

        var background = ""

        when(field) {

            "호수" -> {
                when(boostCount) {
                    0 -> background = "호수 1"
                    1 -> background = "호수 2"
                    2 -> background = "호수 3"
                }
            }
            "강" -> {
                when(boostCount) {
                    0 -> background = "강 1"
                    1 -> background = "강 2"
                    2 -> background = "강 3"
                }
            }
            "바닷가" -> {
                when(boostCount) {
                    0 -> background = "바닷가 1"
                    1 -> background = "바닷가 2"
                    2 -> background = "바닷가 3"
                }
            }
            "태평양" -> {
                when(boostCount) {
                    0 -> background = "태평양 1"
                    1 -> background = "태평양 2"
                    2 -> background = "태평양 3"
                }
            }

        }

        return background

    } // getBackground()


}