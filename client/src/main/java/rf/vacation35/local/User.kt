package rf.vacation35.local

import androidx.annotation.Keep
import kotlinx.serialization.Serializable

@Keep
@Serializable
class User(
    var id: Int,
    var name: String,
    var login: String,
    var password: String,
    var accessPrice: Boolean = false,
    var accessBooking: Boolean = false,
    var admin: Boolean = false,
)
