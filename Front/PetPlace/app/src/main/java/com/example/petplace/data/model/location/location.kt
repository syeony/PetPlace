// data/model/location/DongAuthResponse.kt
import com.google.gson.annotations.SerializedName

data class DongAuthResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: DongAuthResult?
)

data class DongAuthResult(
    val regionId: Long,
    val regionName: String
)
