package com.ledahl.apps.recieppyapi.service

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import java.util.*

interface ImageService {
    fun getCoverImage(): String?
}

@Service
class UnsplashImageService(@Value("\${UNSPLASH_API_KEY}") private val apiKey: String) : ImageService {

    private val logger = LoggerFactory.getLogger(ImageService::class.java)

    private val cachedImageUrls: Queue<String> = LinkedList<String>()

    fun updateImageCache() {
        logger.info("Updating image cache")

        val restTemplate = RestTemplate()
        val headers = HttpHeaders()
        headers.set("Authorization", "Client-ID $apiKey")

        val entity = HttpEntity<String>(headers)
        val resourceUrl = "https://api.unsplash.com/search/photos?query=food&per_page=20"
        val response = restTemplate.exchange(resourceUrl, HttpMethod.GET, entity, String::class.java)

        if (response.statusCode == HttpStatus.OK) {
            cachedImageUrls.clear()
            parseImageUrls(response.body)
        }
    }

    private fun parseImageUrls(jsonString: String?) {
        if (jsonString == null) {
            return
        }

        val jsonElement = JsonParser.parseString(jsonString)
        if (jsonElement.isJsonObject) {
            val jsonRootNode = jsonElement as JsonObject
            if (jsonRootNode.has("results")) {
                val jsonArray = jsonRootNode.getAsJsonArray("results")

                for (jsonNode in jsonArray) {
                    val jsonNodeObject = jsonNode.asJsonObject
                    val jsonUrlsObject = jsonNodeObject.get("urls").asJsonObject
                    val imageUrl = jsonUrlsObject.get("small").asString

                    cachedImageUrls.add(imageUrl)
                }
            }
        }
    }

    override fun getCoverImage(): String? {
        if (cachedImageUrls.isEmpty()) {
            updateImageCache()
        }
        return cachedImageUrls.poll()
    }
}