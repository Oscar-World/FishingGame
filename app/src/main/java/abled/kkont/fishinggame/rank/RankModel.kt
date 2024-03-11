package abled.kkont.fishinggame.rank

import android.content.SharedPreferences
import abled.kkont.fishinggame.data.Fish
import abled.kkont.fishinggame.data.Rank
import abled.kkont.fishinggame.network.ApiClient
import abled.kkont.fishinggame.network.ApiInterface
import android.util.Log
import com.google.gson.Gson
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Call

class RankModel(val preferences: SharedPreferences) {

    lateinit var selectType: String

    lateinit var totalList: ArrayList<Rank>
    lateinit var resultArray: ArrayList<Rank>
    val apiClient: ApiClient = ApiClient()
    val apiInterface: ApiInterface = apiClient.getApiClient()!!.create(ApiInterface::class.java)
    var insertRodRank: Boolean = true
    private var buttonClickStatus : Boolean = false
    var registeredKKON: Int = 0
    lateinit var fish:Fish
    lateinit var inventoryHiddenFishList:ArrayList<Fish>


    /*
    랭킹 리사이클러뷰 세팅
    */
    fun setTotalRankList(totalList: ArrayList<Rank>) {

        this.totalList = totalList

    } // setTotalRankList()


    /*
    각 탭을 눌렀을때 리사이클러뷰 갱신하는 메서드
    */
    fun setCategory(name: String): ArrayList<Rank> {

        val resultArrayList = ArrayList<Rank>()

        when(name){

            "낚시대" -> selectType = "낚시대"
            "기부왕" -> selectType = "기부왕"
            "호수" -> selectType = "호수"
            "강" -> selectType = "강"
            "바닷가" -> selectType = "바닷가"
            "태평양" -> selectType = "태평양"

        }

        for (i: Int in 0 until totalList.size) {

            if (totalList[i].type == selectType) {

                val rank: Rank =
                    Rank(totalList[i].nickname, totalList[i].content, totalList[i].type)
                resultArrayList.add(rank)

            }

        }

        resultArrayList.sortByDescending { Integer.parseInt(it.content) }

        resultArray = resultArrayList

        val resultShortArrayList = ArrayList<Rank>()

        if (resultArrayList.size > 30) {

            for (i: Int in 0 until 30) {

                resultShortArrayList.add(resultArrayList[i])

            }

        } else {

            for (i: Int in 0 until resultArrayList.size) {

                resultShortArrayList.add(resultArrayList[i])

            }

        }

        return resultShortArrayList

    } // setCategory()


    /*
    현재 내 랭크 보여주는 메서드
    */
    fun myRank(): String {

        val nickname = preferences.getString("nickname", "")
        var myRank = -1
        var text: String

        for (i: Int in 0 until resultArray.size) {

            if (resultArray[i].nickname == nickname) {

                myRank = i

            }

        }

        text = "현재 나의 랭킹 : ${myRank + 1}위"

        // 나의 순위가 없는경우
        if (myRank == -1) {

            text = "현재 나의 랭킹 : 순위 없음"

        }

        return text

    } // myRank


    /*
    서버에 랭크리스트 요청하는 메서드
    */
    fun requestRank(): Call<ArrayList<Rank>> {

        return apiInterface.readRankArrayList("mode")

    } // requestRank


    /*
    낚시대 랭킹 업데이트 하는 메서드
    */
    fun updateRodRank(rankList: ArrayList<Rank>): Call<ArrayList<Rank>> {

        val nickname = preferences.getString("nickname", "")

        var call: Call<ArrayList<Rank>> = apiInterface.readRankArrayList("mode")

        val rod = preferences.getInt("rod", 0)
        var updateRodRank = false

        // 액티비티에서 낚시대 랭킹 등록 한번만 가능하게함.
        insertRodRank = false

        for (i: Int in 0 until totalList.size) {

            // 기존에 낚시대 랭킹을 등록한 경우
            if (nickname == totalList[i].nickname && totalList[i].type == "낚시대") {

                updateRodRank = true

            }

        }

        // 낚시대 랭크 등록한게 있고 update 해준다.
        if (updateRodRank) {

            call = apiInterface.updateRankArrayList(
                "update",
                nickname.toString(),
                "낚시대",
                rod.toString()
            )

            // 낚시대 랭크 등록한게 없고 insert 해준다.
        } else {

            call = apiInterface.updateRankArrayList(
                "insert",
                nickname.toString(),
                "낚시대",
                rod.toString()
            )

        }

        return call

    } // updateRank()


    /*
    기부왕 랭크 update
    */
    fun updateKKONRank(rankList: ArrayList<Rank>, inputKKON: String): Call<ArrayList<Rank>> {

        val nickname = preferences.getString("nickname", "")
        var checkRegister = false
        var KKON: Int = 0

        for (i: Int in 0 until rankList.size) {

            if (nickname == rankList[i].nickname) {

                checkRegister = true
                KKON = Integer.parseInt(rankList[i].content)

            }

        }

        // INSERT
        var call: Call<ArrayList<Rank>> = apiInterface.updateRankArrayList(
            "insert",
            nickname.toString(),
            "기부왕",
            (KKON + Integer.parseInt(inputKKON)).toString()
        )

        // UPDATE
        if (checkRegister) {

            call = apiInterface.updateRankArrayList(
                "update",
                nickname.toString(),
                "기부왕",
                (KKON + Integer.parseInt(inputKKON)).toString()
            )

        }

        return call

    } // updateKKONRank()


    /*
    잡은 히든물고기 랭크 등록 및 갱신
    */
    fun updateFishRank(mode: String, type: String, content: String): Call<ArrayList<Rank>> {

        var call: Call<ArrayList<Rank>> = apiInterface.readRankArrayList("mode")

        val nickname = preferences.getString("nickname", "")

        // 인서트인 경우
        if (mode == "insert") {

            call = apiInterface.updateRankArrayList("insert", nickname.toString(), type, content)

            // 업데이트인 경우
        } else {

            call = apiInterface.updateRankArrayList("update", nickname.toString(), type, content)

        }

        return call

    } // updateFishRank()


    /*
    유저 KKON 갱신
    */
    fun updateUserKKON(inputKKON: Int) {

        val editor = preferences.edit()
        editor.putInt("KKON", preferences.getInt("KKON", 0) - inputKKON)
        editor.apply()

    } // updateUserKKON()


    /*
    이미 랭킹에 등록한적이 있는지 없는지 확인하는 메서드
    */
    fun checkIsExistRank(rankList: ArrayList<Rank>): Boolean {

        var isExist = false
        val nickname = preferences.getString("nickname", "")

        for (i: Int in 0 until rankList.size) {

            if (rankList[i].nickname == nickname) {
                isExist = true
            }

        }

        return isExist

    } // checkIsExistRank


    /*
    등록한 물고기의 길이가 등록하려는 물고기의 길이보다 작은지 판별하는 메서드
    */
    fun checkIsInsertFish(fish: Fish, rankList: ArrayList<Rank>): Boolean {

        val nickname = preferences.getString("nickname", "")
        var isRegist = false

        when (selectType) {

            "호수", "강", "바닷가", "태평양" -> {

                for (i: Int in 0 until rankList.size) {

                    if (rankList[i].nickname == nickname) {

                        if (fish.length > Integer.parseInt(rankList[i].content)) {

                            isRegist = true

                        }

                    }

                }

            }

        }

        return isRegist

    } // checkIsInsertFish


    /*
    랭킹에서 물고기 등록할때 등록할 수 있는 히든 물고기만 띄워주는 메서드
    */
    fun setInventory(): ArrayList<Fish> {

        inventoryHiddenFishList = ArrayList<Fish>()
        val inventory = preferences.getString("inventory", "")
        var hiddenFishName = ""

        // 장소에 따른 히든 물고기 지정
        when (selectType) {

            "호수" -> hiddenFishName = "비단잉어"
            "강" -> hiddenFishName = "가물치"
            "바닷가" -> hiddenFishName = "다금바리"
            "태평양" -> hiddenFishName = "고래"

        }

        // 각 물고기 탭에서 랭킹등록 눌렀을 때 지역별 히든물고기만 보여주기.
        if (inventory != "") {

            val jsonArray = JSONArray(inventory)

            for (i in 0 until jsonArray.length()) {

                val jsonObject: JSONObject = jsonArray.get(i) as JSONObject

                if (jsonObject.get("name").toString() == hiddenFishName) {

                    inventoryHiddenFishList.add(
                        Fish(
                            jsonObject.get("name").toString(),
                            jsonObject.get("length").toString().toInt(),
                            jsonObject.get("price").toString().toInt(),
                            jsonObject.get("icon").toString().toInt()
                        )
                    )

                }

            }

        }

        return inventoryHiddenFishList

    } // setInventory()


    /*
    물고기에 따른 필드 정의해주는 메서드
    */
    fun checkType(name: String): String {

        var type: String = ""

        when (name) {

            "비단잉어" -> type = "호수"
            "가물치" -> type = "강"
            "다금바리" -> type = "바닷가"
            "고래" -> type = "태평양"

        }

        return type

    } // checkType()


    /*
    물고기 등록 후 인벤토리 갱신 해주는 메서드
    */
    fun fishUpdateSaveInventory(fish: Fish) {

        val fishList = ArrayList<Fish>()
        val editor = preferences.edit()

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

        fishList.remove(fish)

        val jsonArray = Gson().toJson(fishList)

        editor.putString("inventory", jsonArray)
        editor.apply()

    } // fishUpdateSaveInventory


    /*
    유저의 현재 KKON 쉐어드에서 불러오기
    */
    fun getKKON(): Int {

        return preferences.getInt("KKON", 0)

    } // getKKON


    /*
    기부왕 랭킹 등록할때 KKON 입력에 관한 예외처리
    */
    fun checkInputTextIsUpdateKKON(text: String): Boolean {

        var isCheck = true

        val KKON = preferences.getInt("KKON", 0)
        val nickname = preferences.getString("nickname","")

        // 내가 등록한 KKON의 값을 가져오기 위한 arrayList 순회
        for(i:Int in 0 until resultArray.size) {

            if (resultArray[i].nickname == nickname) {

                registeredKKON = Integer.parseInt(resultArray[i].content)

            }

        }

        if (text.trim() == "") {

            isCheck = false

        } else if (text.contains(" ")) {

            isCheck = false

        } else if (text.toLong() > 2147483646) {

            isCheck = false

        } else if (KKON < Integer.parseInt(text)) {

            isCheck = false

        } else if (Integer.parseInt(text) < 1) {

            isCheck = false

        } else if (registeredKKON+Integer.parseInt(text) > 999999999) {

            isCheck = false

        }

        return isCheck

    } // checkInputTextIsUpdateKKON


    /*
    KKON 등록 예외처리 후 토스트메시지에 들어갈 텍스트
    */
    fun inputUpdateKKONExceptionMessage(text: String): String {

        var message = ""

        val KKON = preferences.getInt("KKON", 0)
        val nickname = preferences.getString("nickname","")

        // 내가 등록한 KKON의 값을 가져오기 위한 arrayList 순회
        for(i:Int in 0 until resultArray.size) {

            if (resultArray[i].nickname == nickname) {

                registeredKKON = Integer.parseInt(resultArray[i].content)

            }

        }

        if (text.trim() == "") {

            message = "등록할 KKON을 입력해 주세요."

        } else if (text.contains(" ")) {

            message = "공백이 있으면 안됩니다."

        } else if (text[0] == '0') {

            message = "0 으로 시작할 수 없습니다."

        } else if (text.toLong() > 2147483646 && KKON < text.toLong()) {

            message = "입력가능한 수치를 초과했습니다."

        } else if (KKON < Integer.parseInt(text)) {

            message = "보유한 KKON이 부족합니다."

        } else if (Integer.parseInt(text) < 1) {

            message = "0 이상의 숫자를 입력해 주세요"

        } else if ((registeredKKON+Integer.parseInt(text)) > 999999999) {

            message = "더이상 KKON을 등록할 수 없습니다."

        }

        return message

    } // inputUpdateKKONExceptionMessage


    /*
    모델에서 갖고있는 피쉬 값 초기화
    */
    fun setSelectFish(fish: Fish) {

        this.fish = fish

    }


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