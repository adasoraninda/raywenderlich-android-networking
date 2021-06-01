package com.raywenderlich.android.taskie.model.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class CompleteNoteResponse(
    @SerialName("message")val message: String
)