package com.ledahl.apps.recieppyapi.service

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.mail.MailException
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import org.thymeleaf.context.Context
import org.thymeleaf.spring5.SpringTemplateEngine
import java.util.concurrent.CompletableFuture
import javax.mail.internet.InternetAddress

@Service
class EmailService(@Autowired private val templateEngine: SpringTemplateEngine,
                   @Autowired private val javaMailSender: JavaMailSender) {

    private val logger = LoggerFactory.getLogger(EmailService::class.java)

    @Async
    fun sendInvite(fromName: String, toEmail: String, locationName: String, inviteCode: String): CompletableFuture<Boolean> {

        val ctx = Context()
        ctx.setVariable("acceptInviteUrl", "reciappy://accept-location-invite/$inviteCode")
        ctx.setVariable("fromName", fromName)
        ctx.setVariable("locationName", locationName)
        ctx.setVariable("inviteCode", inviteCode)

        val message = javaMailSender.createMimeMessage()
        val helper = MimeMessageHelper(message, true)
        helper.setFrom(InternetAddress("ledahl@ledahl.com", "Reciappy"))
        helper.setTo(toEmail)
        helper.setSubject("$fromName har invitert deg til kokeboken \"$locationName\"")

        val htmlContent = templateEngine.process("location-invite", ctx)
        helper.setText(htmlContent, true)

        return try {
            javaMailSender.send(message)
            logger.info("Email from $fromName has just been sent to $toEmail")
            CompletableFuture.completedFuture(true)
        } catch (ex: MailException) {
            logger.error("Could not send mail", ex)
            CompletableFuture.completedFuture(false)
        }
    }
}