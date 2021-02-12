package com.app.kiranachoice.data.network_models

import com.google.gson.annotations.SerializedName

data class CurrentDateTime(

    @field:SerializedName("message")
	val message: String,

    @field:SerializedName("zones")
	val zones: List<ZonesItem>,

    @field:SerializedName("status")
	val status: String
)

data class ZonesItem(

	@field:SerializedName("countryCode")
	val countryCode: String,

	@field:SerializedName("gmtOffset")
	val gmtOffset: Int,

	@field:SerializedName("countryName")
	val countryName: String,

	@field:SerializedName("zoneName")
	val zoneName: String,

	@field:SerializedName("timestamp")
	val timestamp: Int
)
