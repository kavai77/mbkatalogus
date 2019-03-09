package com.himadri.dto

import java.io.InputStream

class UserRequest(
    val requestId: String,
    val csvInputStream: InputStream,
    val catalogueTitle: String,
    val quality: Quality,
    val isWholeSaleFormat: Boolean,
    val isAutoLineBreakAfterMinQty: Boolean,
    val headerImageStream: InputStream?,
    val isWideHeaderImage: Boolean,
    val footerImageStream: InputStream?,
    val isWideFooterImage: Boolean
)
