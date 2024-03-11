package abled.kkont.fishinggame.shop

import abled.kkont.fishinggame.data.Fish
import android.content.SharedPreferences
import com.google.gson.Gson
import org.json.JSONArray
import org.json.JSONObject

class ShopModel(private val preferences: SharedPreferences) {

    val fishList = ArrayList<Fish>()
    var kkon = preferences.getInt("KKON", 0)
    var rod = preferences.getInt("rod",0)
    private var buttonClickStatus : Boolean = false
    private lateinit var statusForButtonOk : String
    private var buyItemPosition = 0
    private var sellItemPosition = 0


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
  SharedPreference에 있는 Inventory 전체가격 로드
  JSONArray -> ArrayList 변환.
  */
    fun totalPriceInventory(): Int {

        val inventory = preferences.getString("inventory", "")
        var totalPrice = 0
        if (inventory != "") {

            val jsonArray = JSONArray(inventory)

            for (i in 0 until jsonArray.length()) {

                val jsonObject: JSONObject = jsonArray.get(i) as JSONObject
                totalPrice += jsonObject.get("price").toString().toInt()
            }
        }

        return totalPrice

    } // loadInventory()


    /*
    SharedPreference에 Inventory 저장.
    ArrayList -> JsonArray로 변환하여 저장.
    */
    fun sellInventory(position:Int): Int {

        val editor = preferences.edit()
        kkon += fishList[position].price
        editor.putInt("KKON",kkon)
        fishList.removeAt(position)

        val jsonArray = Gson().toJson(fishList)
        editor.putString("inventory", jsonArray)
        editor.commit()

        return kkon

    } // saveData()


    /*
    전체팔기
     */
    fun totalSellInventory(): Int {

        val editor = preferences.edit()
        kkon += totalPriceInventory()
        editor.putInt("KKON", kkon)

        for (i in fishList.size-1 downTo 0){
            fishList.removeAt(i)
        }
        val jsonArray = Gson().toJson(fishList)
        editor.putString("inventory", jsonArray)

        editor.commit()

        return kkon

    } // totalSellInventory()


    /*
    현재 리스트 전체 팔면 나오는 가격
     */
    fun totalSellPreView(): Int {

        var totalKKON = 0
        for (i in 0 until fishList.size) {
            totalKKON += fishList.get(i).price
        }

        return totalKKON

    } // totalSellPreView()


    /*
    10강 낚싯대 사기
     */
    fun buyRod10(): Int {

        val editor = preferences.edit()
        rod = 10
        kkon -= 10000
        editor.putInt("KKON",kkon)
        editor.putInt("rod",rod)
        editor.commit()

        return kkon

    }


    /*
    15강 낚싯대 사기
     */
    fun buyRod15(): Int{
        val editor = preferences.edit()
        rod = 15
        kkon -= 50000
        editor.putInt("KKON",kkon)
        editor.putInt("rod",rod)
        editor.commit()

        return kkon

    } // buyRod15()


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
    사기 · 팔기 · 전체 판매 중 현재 상태 세팅.
     */
    fun setStatusForButtonOk(status : String) {

        statusForButtonOk = status

    } // setStatusForButtonOk()


    /*
    사기 · 팔기 · 전체 판매 중 현재 상태 반환.
     */
    fun getStatusForButtonOk() : String {

        return statusForButtonOk

    } // getStatusForButtonOk()


    /*
    아이템 사기 포지션 세팅.
     */
    fun setBuyItemPosition(position : Int) {

        buyItemPosition = position

    } // setBuyItemPosition()


    /*
    아이템 사기 포지션 반환.
     */
    fun getBuyItemPosition() : Int {

        return buyItemPosition

    } // getBuyItemPosition()


    /*
    아이템 팔기 포지션 세팅.
     */
    fun setSellItemPosition(position : Int) {

        sellItemPosition = position

    } // setSellItemPosition()


    /*
    아이템 팔기 포지션 반환.
     */
    fun getSellItemPosition() : Int {

        return sellItemPosition

    } // getSellItemPosition()


    /*
    999999999(9억9천....)넘어가면 판매 안되게
    단일 판매 했을 때 가격 미리보기
     */
    fun checkMaxKKONSell(position: Int): Boolean {

        return kkon+fishList[position].price > 999999999
        return false

    } //getSellFishPreView()

    /*
   999999999(9억9천....)넘어가면 판매 안되게
   단일 판매 했을 때 가격 미리보기
    */
    fun checkMaxKKONTotalSell(): Boolean {

        return kkon+totalSellPreView() > 999999999
        return false

    } //getSellFishPreView()

    /*
    돈 999999999 집어넣기
     */
//    fun setMaxKKON() {
//
//        val editor = preferences.edit()
//        editor.putInt("KKON",999999999)
//        editor.commit()
//
//    }
}