package com.app.kiranachoice.data

import com.google.gson.annotations.SerializedName

data class CurrentDateTime(

	@field:SerializedName("unixtime")
	val unixtime: Long? = null,

	@field:SerializedName("utc_offset")
	val utcOffset: String? = null,

	@field:SerializedName("dst")
	val dst: Boolean? = null,

	@field:SerializedName("day_of_year")
	val dayOfYear: Int? = null,

	@field:SerializedName("timezone")
	val timezone: String? = null,

	@field:SerializedName("abbreviation")
	val abbreviation: String? = null,

	@field:SerializedName("dst_offset")
	val dstOffset: Int? = null,

	@field:SerializedName("utc_datetime")
	val utcDatetime: String? = null,

	@field:SerializedName("datetime")
	val datetime: String? = null,

	@field:SerializedName("dst_until")
	val dstUntil: Any? = null,

	@field:SerializedName("client_ip")
	val clientIp: String? = null,

	@field:SerializedName("dst_from")
	val dstFrom: Any? = null,

	@field:SerializedName("week_number")
	val weekNumber: Int? = null,

	@field:SerializedName("day_of_week")
	val dayOfWeek: Int? = null,

	@field:SerializedName("raw_offset")
	val rawOffset: Int? = null
)
