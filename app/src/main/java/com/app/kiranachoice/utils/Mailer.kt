package com.app.kiranachoice.utils

import android.annotation.SuppressLint
import android.content.Context
import io.reactivex.rxjava3.core.Completable
import java.io.InputStream
import java.util.*
import javax.mail.*
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeBodyPart
import javax.mail.internet.MimeMessage
import javax.mail.internet.MimeMultipart

object Mailer {
    @SuppressLint("CheckResult")
    fun sendMail(
        context: Context,
        recipient: String,
        userName: String,
        orderId: String,
        orderPlacedDate: String,
        amountWithoutShippingCharge : String,
        shippingCharge : String,
        amountWithShippingCharge : String
    ): Completable {

        val multipart = MimeMultipart()

        return Completable.create { emitter ->

            //configure SMTP server
            val props: Properties = Properties().also {
                it["mail.smtp.host"] = "smtp.gmail.com"
                it["mail.smtp.socketFactory.port"] = "465"
                it["mail.smtp.socketFactory.class"] = "javax.net.ssl.SSLSocketFactory"
                it["mail.smtp.auth"] = "true"
                it["mail.smtp.port"] = "465"
            }

            //Creating a session
            val session = Session.getDefaultInstance(props, object : Authenticator() {
                override fun getPasswordAuthentication(): PasswordAuthentication {
                    return PasswordAuthentication(EMAIL, PASSWORD)
                }
            })

            try {

                MimeMessage(session).let { mime ->
                    mime.setFrom(InternetAddress(EMAIL))
                    //Adding receiver
                    mime.addRecipient(Message.RecipientType.TO, InternetAddress(recipient))
                    //Adding subject
                    mime.subject = "ORDER CONFIRMED"

                    val messageBodyPart = MimeBodyPart()
                    val inputStream: InputStream =
                        context.assets.open("email order confirmation.html")
                    val size = inputStream.available()
                    val buffer = ByteArray(size)
                    inputStream.read(buffer)
                    inputStream.close()
                    var str = String(buffer)
                    str = str.replace(USERNAME, userName.substringBefore(" "))
                    str = str.replace(ORDER_ID, orderId)
                    str = str.replace(ORDER_DATE, orderPlacedDate)
                    str = str.replace(AMOUNT_WITHOUT_SHIPPING_CHARGE, amountWithoutShippingCharge)
                    str = str.replace(SHIPPING_CHARGE, shippingCharge)
                    str = str.replace(AMOUNT_WITH_SHIPPING_CHARGE, amountWithShippingCharge)
//                    str = str.replace(
//                        COUPON,
//                        if (couponApplied.isNullOrEmpty()) "No Coupon Applied" else couponApplied
//                    )
                    messageBodyPart.setContent(str, "text/html; charset=utf-8")
                    multipart.addBodyPart(messageBodyPart)

                    //Adding message
                    mime.setContent(multipart)
                    //send mail
                    Transport.send(mime)
                }

            } catch (e: MessagingException) {
                emitter.onError(e)
            }

            //ending subscription
            emitter.onComplete()
        }

    }

}