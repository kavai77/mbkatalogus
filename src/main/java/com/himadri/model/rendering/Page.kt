package com.himadri.model.rendering

class Page(
    val headLine: String,
    val category: String,
    val pageNumber: Int,
    val orientation: Orientation,
    val boxes: List<Box>
) {
    enum class Orientation {
        LEFT, RIGHT
    }
}
