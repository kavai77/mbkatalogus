package com.himadri.model.rendering

import java.awt.Color
import java.awt.image.BufferedImage

data class Box(
    val image: String? = null,
    val brandImage: String? = null,
    val title: String? = null,
    val category: String? = null,
    val productGroup: String? = null,
    val productColor: Color? = null,
    val width: Int,
    val height: Int,
    val isNewProduct: Boolean? = null,
    val onNewPage: Boolean? = null,
    val articles: List<Article>? = null,
    val bufferedImage: BufferedImage? = null,
    val boxType: Type
) {
    var row: Int = 0
    var column: Int = 0

    companion object {
        @JvmStatic
        fun createArticleBox(
            image: String?,
            brandImage: String?,
            title: String,
            category: String,
            productGroup: String,
            productColor: Color,
            width: Int,
            height: Int,
            isNewProduct: Boolean,
            onNewPage: Boolean,
            articles: List<Article>
        ) = Box(
            image = image,
            brandImage = brandImage,
            title = title,
            category = category,
            productGroup = productGroup,
            productColor = productColor,
            width = width,
            height = height,
            isNewProduct = isNewProduct,
            onNewPage = onNewPage,
            articles = articles,
            boxType = Type.ARTICLE
        )

        @JvmStatic
        fun createImageBox(
            width: Int,
            height: Int,
            bufferedImage: BufferedImage
        ) = Box(
            width = width,
            height = height,
            bufferedImage = bufferedImage,
            boxType = Type.IMAGE
        )
    }

    enum class Type {
        ARTICLE, IMAGE
    }

    fun setDimensions(row: Int, column: Int) {
        this.row = row
        this.column = column
    }

    data class Article(
        val number: String,
        val price: String,
        val description: String,
        val indexName: String?,
        val isEmptyItemText: Boolean,
        val packaging: String,
        val cikkAzon: String
    )

}
