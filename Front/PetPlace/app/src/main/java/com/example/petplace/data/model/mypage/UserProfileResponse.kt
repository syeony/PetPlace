package com.example.petplace.data.model.mypage

data class UserProfileResponse(
    val userId: Long,
    val nickname: String?,
    val regionName: String?,
    val defaultPetId: Int,
    val userImgSrc: String?,
    val petSmell: Double,
    val defaultBadgeId: Int,
    val level: Int,
    val experience: Int,
    val introduction: String?,
    val petList: List<Pet>?,
    val imgList: List<ImageInfo>
) {
    data class Pet(
        val id: Int,
        val userId: Int,
        val name: String,
        val animal: String,  // enum 가능
        val breed: String,   // enum 가능
        val sex: String,     // enum 가능
        val birthday: String,
        val imgSrc: String?,
        val tnr: Boolean,
        val user: User
    )

    data class User(
        val id: Int,
        val userName: String,
        val password: String,
        val name: String,
        val nickname: String,
        val createdAt: String,
        val deletedAt: String,
        val regionId: Int,
        val defaultPetId: Int,
        val socialId: String,
        val userImgSrc: String,
        val petSmell: Double,
        val defaultBadgeId: Int,
        val ci: String,
        val phoneNumber: String,
        val gender: String,
        val birthday: String,
        val isForeigner: Boolean,
        val level: Int,
        val experience: Int,
        val loginType: String,  // enum 가능
        val socialEmail: String,
        val introduction: Introduction,
        val pets: List<String>
    )

    data class Introduction(
        val id: Int,
        val user: String,
        val content: String
    )

    data class ImageInfo(
        val id: Int,
        val src: String,
        val sort: Int
    )
}
