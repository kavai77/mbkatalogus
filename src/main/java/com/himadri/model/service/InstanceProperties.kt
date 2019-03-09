package com.himadri.model.service

import com.himadri.dto.Quality

class InstanceProperties {
    var lastCatalogueName: String? = null
    var lastQuality: Quality? = null
    var lastWholeSaleFormat: Boolean? = null
    var isLastAutoLineBreakAfterMinQty: Boolean = false
    var isLastWideHeaderImage: Boolean = false
    var isLastWideFooterImage: Boolean = false
    var productGroupsWithoutChapter: List<String>? = null
}
