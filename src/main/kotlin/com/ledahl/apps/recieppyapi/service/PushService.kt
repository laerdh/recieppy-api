package com.ledahl.apps.recieppyapi.service

import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.MulticastMessage
import com.google.firebase.messaging.Notification
import org.springframework.stereotype.Service

@Service
class PushService {
    fun sendPush() {
        /*val token = "eagQbzqh1is:APA91bEPx1i4lFnO8ZF3zs8rS4duVeI65XyqolEzrYMG0oy6CambhhLPGYwiv6uIn07nbnTAB1DE-uZQu8_McYH-xBmWdqPNGmlMlkOuLbzcyTkyhqKwt0V-VTUAkpQ-cXv2xNh_riTt"
        val message = MulticastMessage.builder()
                .setNotification(
                        Notification.builder()
                                .setTitle("title")
                                .setBody("body")
                                .build())
                .addAllTokens(arrayListOf(token))
                .build()

        val response = FirebaseMessaging.getInstance().sendMulticast(message)
        print("")
        */
    }
}