package abled.kkont.fishinggame.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities

class NetworkStatus {

    /*
    네트워크 연결 상태를 논리 값으로 반환.
    와이파이, 모바일 데이터 연결 중일 경우 true.
     */
    fun getNetworkStatus(context : Context) : Boolean {

        var networkStatus = false

        val connectivityManager : ConnectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network : Network? = connectivityManager.activeNetwork
        val actNetwork : NetworkCapabilities? = connectivityManager.getNetworkCapabilities(network)

        if (actNetwork == null) {
            networkStatus = false
        } else if (actNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) || actNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
            networkStatus = true
        }

        return networkStatus

    } // getNetworkStatus()

}