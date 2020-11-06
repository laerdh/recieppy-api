package com.ledahl.apps.recieppyapi.service

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.mail.MailException
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.stereotype.Service

@Service
class EmailService(@Autowired private val javaMailSender: JavaMailSender) {

    private val logger = LoggerFactory.getLogger(EmailService::class.java)

    fun sendInvite(fromName: String, toEmail: String, locationName: String, inviteCode: String): Boolean {
        val message = javaMailSender.createMimeMessage()
        val helper = MimeMessageHelper(message, true)
        helper.setFrom("noreply@ledahl.com")
        helper.setTo(toEmail)
        helper.setSubject("$fromName har invitert deg til kokeboken \"$locationName\"")

        val body = """
            <p>$fromName har invitert deg til kokeboken "$locationName".</p>
            <h2>Bruk følgende kode</h2>
            <h2>$inviteCode</h2>
            <p>Last ned appen <a href="www.reciappy.com">her</a></p><p>Denne mailen kan ikke besvares osv.</p>
        """.trimIndent()

        helper.setText(body, true)

        return try {
            javaMailSender.send(message)
            true
        } catch (ex: MailException) {
            logger.error("Could not send mail", ex)
            false
        }
    }
}