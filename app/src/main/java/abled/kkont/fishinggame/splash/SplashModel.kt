package abled.kkont.fishinggame.splash

import abled.kkont.fishinggame.data.Fish
import android.content.SharedPreferences
import com.google.gson.Gson

class SplashModel(var preferences: SharedPreferences) {

    /*
    SharedPreference에 Collection 데이터 있는지 확인.
    없으면 빈 JsonArray 추가.
    */
    fun checkCollection() {

        val editor = preferences.edit()
        val collection = preferences.getString("collection", "")

        if (collection == "") {

            val collectionList = ArrayList<Fish>()

            for (i in 0 until 28) {

                collectionList.add(Fish("", 0, 0, 0))

            }

            val jsonArray = Gson().toJson(collectionList)
            editor.putString("collection", jsonArray)
            editor.commit()

        }

    } // checkCollection()

}