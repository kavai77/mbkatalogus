package com.himadri.renderer.imageloader

import org.apache.pdfbox.pdmodel.PDDocument

data class ResourceImageKey (
    val resource: String,
    val pdDocument: PDDocument
)