package com.example.gpsmap

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.example.gpsmap.databinding.ActivityMapsBinding
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.gms.maps.model.TileProvider
import org.jetbrains.anko.alert
import org.jetbrains.anko.noButton
import org.jetbrains.anko.toast
import org.jetbrains.anko.yesButton
import java.util.jar.Manifest

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding

    // 위치 정보를 주기적으로 얻는 데 필요한 객체들을 선언
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: MyLocationCallBack

    private val REQUEST_ACCESS_FINE_LOCATION = 1000

    // PolyLine 옵션
    private val polylineOptions = PolylineOptions().width(5f).color(Color.RED)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 화면이 꺼지지 않게 하기
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        // 세로 모드로 화면 고정
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        setContentView(R.layout.activity_maps)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        // SupportMApFragment를 가져와서 지도가 준비되면 알림을 받음
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        locationInit()
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    // 위치 정보를 얻기 위한 각종 초기화
    private fun locationInit() {
        fusedLocationProviderClient = FusedLocationProviderClient(this)
        locationCallback = MyLocationCallBack()
        locationRequest = LocationRequest()

        // GPS 우선
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        // 업테이트 인터벌
        // 위치 정보가 없을 때에는 없데이트를 하지 않으며, 상황에 따라 주기가 짧아질 수 있음. 정확하지 않음
        // 다른 앱에서 짧은 인터벌로 위치 정보를 요청하면 짧아질 수 있음
        locationRequest.interval = 10000
        // 정확함. 이것보다 짧은 업데이트 주기는 없음
        locationRequest.fastestInterval = 5000
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap    // 지도가 준비되면 GoogleMap 객체를 얻음

        // Add a marker in Sydney and move the camera
        val sydney = LatLng(-34.0, 151.0)
        mMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))
    }

    override fun onResume() {
        super.onResume()

        // 권한 요청
        permissionCheck(cancel = {
            //위치 정보가 필요한 이유 다이얼로그 표시
            showPermissionInfoDialog()
        }, ok = {
            // 현재 위치를 주기적으로 요청(권한이 필요한 부분)
            addLocationListener()
        })
        addLocationListener()
    }

    // 앱이 동작하지 않을 때 위치 정보 요청을 삭제하는 역할
    override fun onPause() {
        super.onPause()
        // 위치 요청 취소
        removeLocationListener()
    }

    // 현재 위치 요청을 삭제
    private fun removeLocationListener() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
    }

    // 위치 정보를 주기적으로 요청하는 코드는 액티비티가 화면에 보일 때만 수행하도록 함
    @SuppressLint("MissingPermission")
    private fun addLocationListener() {
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null)
    }

    // 함수 인자 두 개를 받음(인자, 반환값 없는 함수들)
    private fun permissionCheck(cancel: () -> Unit, ok: () -> Unit) {
        // 위치 권한이 있는지 검사
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // 권한이 허용되지 않음
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.ACCESS_FINE_LOCATION)) {
                // 이전에 권한을 한 번 거부한 것이 있는 경우에 실행할 함수
                cancel()
            } else {
                // 권한 요청
                ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_ACCESS_FINE_LOCATION)
            }
        } else {
            // 권한을 수락했을 때 실행할 함수
            ok()
        }
    }

    // 사용자가 한 번 거부했을 때 권한이 필요한 이유를 설명하는 다이얼로그를 표시하는 메서드
    private fun showPermissionInfoDialog() {
        alert("현재 위치 정보를 얻으려면 위치 권한이 필요합니다", "권한이 필요한 이유") {
            yesButton {
                // 권한 요청
                ActivityCompat.requestPermissions(this@MapsActivity,
                    arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                    REQUEST_ACCESS_FINE_LOCATION)
            }
            noButton {  }
        }.show()
    }

    // 사용자가 권한을 수락하거나 거부했을 때를 처리하는 메서드
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_ACCESS_FINE_LOCATION -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    // 권한 허용됨
                    addLocationListener()
                } else {
                    // 권한 거부
                    toast("권한 거부됨")
                }
                return
            }
        }
    }

    inner class MyLocationCallBack: LocationCallback() {
        override fun onLocationResult(p0: LocationResult?) {
            super.onLocationResult(p0)
            val location = p0?.lastLocation

            // 기기의 GPS 설정이 꺼져 있거나 현재 위치 정보를 얻을 수 없으면 location 객체가 null일 수 있음
            // location 객체가 null이 아니면 해당 위도와 경도 위치로 카메라를 이동함
            location?.run {
                // 14 level로 확대하고, 현재 위치로 카메라 이동
                val latLng = LatLng(latitude, longitude)
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17f))

                // 주기적으로 위치 정보가 갱신되는지 로그로 확인
                Log.d("MapsActivity", "위도: $latitude, 경도: $longitude")

                // PolyLine에 좌표 추가
                polylineOptions.add(latLng)
                // 선 그리기
                mMap.addPolyline(polylineOptions)
            }
        }

    }
}