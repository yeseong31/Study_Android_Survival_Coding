import android.content.Context
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import org.jetbrains.anko.cameraManager

class Torch(context: Context) {
    private var cameraId: String? = null
    private val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager   // 이 메서드는 Object 형을 반환하므로 as로 형 변환

    init {
        cameraId = getCameraId()
    }

    fun flashOn() {
        cameraManager.setTorchMode(cameraId!!, true)
    }

    fun flashOff() {
        cameraManager.setTorchMode(cameraId!!, false)
    }

    private fun getCameraId(): String? {
        // 기기가 가지고 있는 모든 카메라에 대한 정보 목록을 제공
        val cameraIds = cameraManager.cameraIdList
        for (id in cameraIds) {
            val info = cameraManager.getCameraCharacteristics(id)
            val flashAvailable = info.get(CameraCharacteristics.FLASH_INFO_AVAILABLE)
            val lensFacing = info.get(CameraCharacteristics.LENS_FACING)
            // 플래시 가능 여부와 카메라의 렌즈 방향 확인
            if (flashAvailable != null
                && flashAvailable
                && lensFacing != null
                && lensFacing == CameraCharacteristics.LENS_FACING_BACK
            ) {
                return id
            }
        }

        // 카메라가 없다면 ID가 null일 수 있기 때문에 반환값을 String?으로 설정
        return null
    }
}