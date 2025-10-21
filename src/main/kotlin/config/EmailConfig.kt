package com.gity.config

import com.gity.config.AppConfig
import org.simplejavamail.api.mailer.Mailer
import org.simplejavamail.api.mailer.config.TransportStrategy
import org.simplejavamail.email.EmailBuilder
import org.simplejavamail.mailer.MailerBuilder
import org.slf4j.LoggerFactory

class EmailUtil(
    private val appConfig: AppConfig
) {
    private val logger = LoggerFactory.getLogger(EmailUtil::class.java)

    // Inisialisasi mailer (bisa di-cache atau dibuat per pengiriman, tergantung kebutuhan)
    // Untuk sementara, buat instance baru setiap kali, tapi di production, mungkin gunakan pooling.
    private fun createMailer(): Mailer {
        return MailerBuilder.withSMTPServer("smtp-relay.brevo.com", 587, "your_smtp_user@domain.com", appConfig.email.apiKey) // Ganti SMTP user jika perlu Brevo
            .withTransportStrategy(TransportStrategy.SMTP_TLS) // Gunakan TLS
            .withSessionTimeout(10 * 1000) // 10 detik timeout
            .buildMailer()
    }

    fun sendEmail(to: String, subject: String, htmlBody: String) {
        val email = EmailBuilder.startingBlank()
            .from(appConfig.email.senderName, appConfig.email.senderEmail)
            .to(to)
            .withSubject(subject)
            .withHTMLText(htmlBody)
            .buildEmail()

        try {
            val mailer = createMailer()
            mailer.sendMail(email)
            logger.info("Email sent successfully to $to")
        } catch (e: Exception) {
            logger.error("Failed to send email to $to. Error: ${e.message}", e)
            // Tambahkan retry logic jika diperlukan
        }
    }

    // Contoh fungsi untuk verifikasi email
    fun sendVerificationEmail(to: String, token: String) {
        val verificationLink = "http://your-app.com/api/v1/auth/verify-email?token=$token" // Ganti dengan URL frontend verifikasi
        val subject = "Verifikasi Email Klik Sewa"
        val htmlBody = """
            <html>
            <body>
                <h2>Selamat Datang di Klik Sewa!</h2>
                <p>Klik tombol di bawah untuk memverifikasi email Anda:</p>
                <a href="$verificationLink" style="display: inline-block; padding: 10px 20px; color: white; background-color: #007bff; text-decoration: none; border-radius: 5px;">Verifikasi Email</a>
                <p>Link ini akan kadaluarsa dalam 24 jam.</p>
            </body>
            </html>
        """.trimIndent()

        sendEmail(to, subject, htmlBody)
    }

    // Tambahkan fungsi lain seperti sendResetPasswordEmail, sendListingApprovedEmail, dll.
}