package abled.kkont.fishinggame.home

import abled.kkont.fishinggame.R
import android.content.SharedPreferences
import abled.kkont.fishinggame.data.Fish
import abled.kkont.fishinggame.data.UpgradeProbability
import org.json.JSONArray
import org.json.JSONObject
import kotlin.random.Random

class HomeModel(val preferences : SharedPreferences) {

    private var buttonClickStatus : Boolean = false
    val itemList : ArrayList<Fish> = ArrayList()


    /*
    쉐어드에서 인벤토리 가져오기
    */
    fun getInventoryList() : ArrayList<Fish> {

        val inventory = preferences.getString("inventory", "")

        if (inventory != "") {

            val jsonArray = JSONArray(inventory)

            for (i in 0 until jsonArray.length()) {

                val jsonObject : JSONObject = jsonArray.get(i) as JSONObject

                itemList.add(
                    Fish(
                        jsonObject.get("name").toString(),
                        jsonObject.get("length").toString().toInt(),
                        jsonObject.get("price").toString().toInt(),
                        jsonObject.get("icon").toString().toInt()
                    )
                )

            }

        }

        return itemList

    } // getInventoryList


    /*
    쉐어드에서 도감 가져오기
    */
    fun getCollectionList() : ArrayList<Fish> {

        val collection = preferences.getString("collection", "")
        val collectionFishList = ArrayList<Fish>()

        if (collection != "") {

            val jsonArray = JSONArray(collection)

            for (i in 0 until jsonArray.length()) {

                val jsonObject : JSONObject = jsonArray.get(i) as JSONObject

                collectionFishList.add(
                    Fish(
                        jsonObject.get("name").toString(),
                        jsonObject.get("length").toString().toInt(),
                        jsonObject.get("price").toString().toInt(),
                        jsonObject.get("icon").toString().toInt()
                    )
                )

            }

        }

        return collectionFishList

    } // getCollectionList


    /*
   강화 조건 만족하는지 확인
    */
    fun isUpgrade() : Boolean {

        val rodUpgrade = preferences.getInt("rod", 0)
        val userKKON = preferences.getInt("KKON", 0)

        var isUpgrade : Boolean = false

        when(rodUpgrade){

            in 0..9 -> { if (userKKON >= (500 * (rodUpgrade + 1))) { isUpgrade = true } }
            in 10..19 -> { if (userKKON >= 1000 * rodUpgrade) { isUpgrade = true } }
            in 20..24 -> { if (userKKON >= 2000 * rodUpgrade) { isUpgrade = true } }
            25 -> { if (userKKON >= 100000) { isUpgrade = true } }
            26 -> { if (userKKON >= 150000) { isUpgrade = true } }
            27 -> { if (userKKON >= 200000) { isUpgrade = true } }
            28 -> { if (userKKON >= 250000) { isUpgrade = true } }
            29 -> { if (userKKON >= 500000) { isUpgrade = true } }

        }

        return isUpgrade

    } // isUpgrade()


    /*
    낚싯대 강화 수치에 따른 적용 효과 - 낚싯대 배경색 반환.
    */
    fun getFishingRodImage() : Int {

        var fishingRodBackground = 0

        when (preferences.getInt("rod", 0)) {

            0 -> fishingRodBackground = R.drawable.dialog_background_normal
            in 1..9 -> fishingRodBackground = R.drawable.dialog_background_magic
            in 10..19 -> fishingRodBackground = R.drawable.dialog_background_rare
            in 20..29 -> fishingRodBackground = R.drawable.dialog_background_unique
            30 -> fishingRodBackground = R.drawable.dialog_background_legendary

        }

        return fishingRodBackground

    } // getFishingRodImage()


    /*
    낚시대 색깔 바꿔주는 메서드
     */
    fun upgradeRodColor() : String {
        var rodUpgrade = preferences.getInt("rod", 0)
        var color = ""

        when (rodUpgrade){

            0 -> color = "normal"
            in 1..9 -> color = "magic"
            in 10..19 -> color = "rare"
            in 20..29 -> color  ="unique"
            30 -> color  ="legendary"

        }

        return color

    } // upgradeRodColor()


    /*
    낚시대 업그레이드
    */
    fun upgradeFishingRod() : Boolean {

        var rodUpgrade = preferences.getInt("rod", 0)
        var userKKON = preferences.getInt("KKON", 0)
        val edit = preferences.edit()

        var result = false
        val random = Random.nextInt(1, 101)

        when(rodUpgrade){

            in 0..9 -> {

                userKKON -= (500 * (rodUpgrade + 1))

                if (random in 1..65) {

                    rodUpgrade++
                    result = true

                }

            }

            in 10..19 -> {

                userKKON -= (1000 * rodUpgrade)

                if (random in 1..45) {

                    rodUpgrade++
                    result = true

                } else if(random in 46..60){

                    rodUpgrade--

                }

            }

            in 20..24 -> {

                userKKON -= (2000 * rodUpgrade)

                if (random in 1..35) {

                    rodUpgrade++
                    result = true

                } else if(random in 36..67){

                    rodUpgrade--

                }

            }

            25 ->{

                userKKON -= 100000

                // 강화성공 15%
                if(random in 1..15){

                    rodUpgrade ++
                    result = true

                    // 강화 하락 42%
                } else if(random in 16..57) {

                    rodUpgrade --

                }

            }

            26 -> {

                userKKON -= 150000

                // 강화성공 13%
                if(random in 1..13){

                    rodUpgrade ++
                    result = true

                    // 강화 하락 45%
                } else if(random in 13..57) {

                    rodUpgrade --

                }

            }

            27 -> {

                userKKON -= 200000

                // 강화성공
                if(random in 1..11) {

                    rodUpgrade ++
                    result = true

                    // 강화 하락
                } else if(random in 12..60) {

                    rodUpgrade --

                }

            }

            28 -> {

                userKKON -= 250000

                // 강화성공 9%
                if(random in 1..9){

                    rodUpgrade ++
                    result = true

                    // 강화 하락 56%
                } else if(random in 10..65) {

                    rodUpgrade --

                }

            }

            29 -> {

                userKKON -= 500000

                // 강화성공 5%
                if(random in 1..5){

                    rodUpgrade ++
                    result = true

                    // 강화 실패
                }  else {

                    rodUpgrade --

                }

            }

        }

        edit.putInt("KKON",userKKON)
        edit.putInt("rod",rodUpgrade)
        edit.apply()

        return result

    } // upgradeFishingRod()


    /*
    강화 성공률
    */
    fun successProbability() : Int {

        val rodUpgrade = preferences.getInt("rod", 0)
        var probability = 0

        when(rodUpgrade) {

            in 0..9 -> probability = 65
            in 10..19 -> probability = 45
            in 20..24 -> probability = 35
            25 -> probability = 15
            26 -> probability = 13
            27 -> probability = 11
            28 -> probability = 9
            29 -> probability = 5

        }

        return probability

    } // successProbability()


    /*
    필요한 꼰
    */
    fun needKKON() : Int {

        val rodUpgrade = preferences.getInt("rod", 0)
        var needKKON = 0

        when(rodUpgrade) {

            in 0..9 ->  needKKON = 500 * (rodUpgrade + 1)
            in 10..19 ->  needKKON = 1000 * rodUpgrade
            in 20..24 ->  needKKON = 2000 * rodUpgrade
            25 ->  needKKON = 100000
            26 ->  needKKON = 150000
            27 ->  needKKON = 200000
            28 ->  needKKON = 250000
            29 ->  needKKON = 300000

        }

        return needKKON

    } // needKKON()


    /*
    30강이 되어 더이상 강화를 못하는 경우에 대한 메서드
    */
    fun checkUpgradeMax() : String {

        var text : String = ""

        // 강화단계가 30인 경우
        if( preferences.getInt("rod",0) == 30){

            text = "최대 강화 수치에 도달하셨습니다!"

        } else {

            text = "+${preferences.getInt("rod",0)} → +${preferences.getInt("rod",0)+1}"

        }

        return text

    } // checkUpgradeMax()


    /*
    낚시대 강화 수치 리턴
    */
    fun getRod() : Int {

        return preferences.getInt("rod",0)

    } // getRod()


    /*
    꼰 값 리턴
    */
    fun getKKON() : Int {

        return preferences.getInt("KKON",0)

    } // getKKON()


    /*
    강화 확률 , 유지 확률 , 하락 확률 리턴
    */
    fun getProbability() : UpgradeProbability {

        var upgradeProbability : UpgradeProbability? = null

        when(preferences.getInt("rod",0)){

            in 0..9 -> { upgradeProbability = UpgradeProbability(65,35,0) }
            in 10..19 -> { upgradeProbability = UpgradeProbability(45,40,15) }
            in 20..24 -> { upgradeProbability = UpgradeProbability(35,33,32) }
            25 -> { upgradeProbability = UpgradeProbability(15,43,42) }
            26 -> { upgradeProbability = UpgradeProbability(13,42,43) }
            27 -> { upgradeProbability = UpgradeProbability(11,40,45) }
            28 -> { upgradeProbability = UpgradeProbability(9,35,49) }
            29 -> { upgradeProbability = UpgradeProbability(5,0,56) }
            30 -> { upgradeProbability = UpgradeProbability(0,0,0)}

        }

        return upgradeProbability!!

    } // getProbability()


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


}