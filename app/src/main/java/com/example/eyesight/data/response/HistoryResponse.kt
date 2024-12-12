package com.example.eyesight.data.response

import com.google.gson.annotations.SerializedName

data class HistoryResponse(

	@field:SerializedName("HistoryResponse")
	val historyResponse: List<HistoryResponseItem?>? = null
)

data class HistoryResponseItem(

	@field:SerializedName("date")
	val date: String? = null,

	@field:SerializedName("result_item")
	val resultItem: String? = null,

	@field:SerializedName("feasibility_test")
	val feasibilityTest: String? = null,

	@field:SerializedName("desc_feasibility")
	val descFeasibility: String? = null,

	@field:SerializedName("confidence")
	val confidence: String? = null,

	@field:SerializedName("id")
	val id: String? = null
)
