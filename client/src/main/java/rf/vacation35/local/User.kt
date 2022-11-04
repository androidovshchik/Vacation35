package rf.vacation35.local

import kotlinx.serialization.Serializable

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
