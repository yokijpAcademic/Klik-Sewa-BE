package com.gity.shared.utils

import com.gity.config.AppConfig
import org.simplejavamail.api.email.Email
import org.simplejavamail.api.mailer.Mailer
import org.simplejavamail.api.mailer.config.TransportStrategy
import org.simplejavamail.email.EmailBuilder
import org.simplejavamail.mailer.MailerBuilder
import org.slf4j.LoggerFactory

class EmailUtil(
    private val appConfig: AppConfig
) {
    private val logger = LoggerFactory.getLogger(EmailUtil::class.java)

    private val mailer: Mailer? = initializeMailer()

    private fun initializeMailer(): Mailer? {
        return try {
            if (appConfig.email.apiKey.isBlank()) {
                logger.warn("Email API key not configured. Email functionality will be disabled.")
                return null
            }

            MailerBuilder
                .withSMTPServer(
                    "smtp-relay.brevo.com",
                    587,
                    appConfig.email.apiKey,
                    "" // Brevo tidak memerlukan username, hanya API key sebagai password
                )
                .withTransportStrategy(TransportStrategy.SMTP_TLS)
                .withSessionTimeout(10 * 1000)
                .buildMailer()
        } catch (e: Exception) {
            logger.error("Failed to initialize email mailer: ${e.message}", e)
            null
        }
    }

    /**
     * Kirim email verifikasi setelah registrasi
     */
    suspend fun sendVerificationEmail(toEmail: String, verificationToken: String) {
        if (mailer == null) {
            logger.warn("Email mailer not initialized. Skipping verification email to $toEmail")
            return
        }

        try {
            val verificationUrl = "${appConfig.app.frontendUrl}/verify-email?token=$verificationToken"

            val email = buildEmail(
                to = toEmail,
                subject = "Verifikasi Email Anda - Klik Sewa",
                htmlBody = buildVerificationEmailHtml(verificationUrl),
                plainTextBody = buildVerificationEmailPlainText(verificationUrl)
            )

            mailer.sendMail(email)
            logger.info("Verification email sent successfully to $toEmail")
        } catch (e: Exception) {
            logger.error("Failed to send verification email to $toEmail: ${e.message}", e)
            throw RuntimeException("Failed to send verification email", e)
        }
    }

    /**
     * Kirim email reset password
     */
    suspend fun sendPasswordResetEmail(toEmail: String, resetToken: String) {
        if (mailer == null) {
            logger.warn("Email mailer not initialized. Skipping password reset email to $toEmail")
            return
        }

        try {
            val resetUrl = "${appConfig.app.frontendUrl}/reset-password?token=$resetToken"

            val email = buildEmail(
                to = toEmail,
                subject = "Reset Password Anda - Klik Sewa",
                htmlBody = buildPasswordResetEmailHtml(resetUrl),
                plainTextBody = buildPasswordResetEmailPlainText(resetUrl)
            )

            mailer.sendMail(email)
            logger.info("Password reset email sent successfully to $toEmail")
        } catch (e: Exception) {
            logger.error("Failed to send password reset email to $toEmail: ${e.message}", e)
            throw RuntimeException("Failed to send password reset email", e)
        }
    }

    /**
     * Kirim email notifikasi listing disetujui
     */
    suspend fun sendListingApprovedEmail(toEmail: String, userName: String, listingTitle: String) {
        if (mailer == null) {
            logger.warn("Email mailer not initialized. Skipping listing approval email to $toEmail")
            return
        }

        try {
            val email = buildEmail(
                to = toEmail,
                subject = "Listing Anda Telah Disetujui - Klik Sewa",
                htmlBody = buildListingApprovedEmailHtml(userName, listingTitle),
                plainTextBody = buildListingApprovedEmailPlainText(userName, listingTitle)
            )

            mailer.sendMail(email)
            logger.info("Listing approval email sent successfully to $toEmail")
        } catch (e: Exception) {
            logger.error("Failed to send listing approval email to $toEmail: ${e.message}", e)
        }
    }

    /**
     * Kirim email notifikasi listing ditolak
     */
    suspend fun sendListingRejectedEmail(
        toEmail: String,
        userName: String,
        listingTitle: String,
        reason: String
    ) {
        if (mailer == null) {
            logger.warn("Email mailer not initialized. Skipping listing rejection email to $toEmail")
            return
        }

        try {
            val email = buildEmail(
                to = toEmail,
                subject = "Listing Anda Ditolak - Klik Sewa",
                htmlBody = buildListingRejectedEmailHtml(userName, listingTitle, reason),
                plainTextBody = buildListingRejectedEmailPlainText(userName, listingTitle, reason)
            )

            mailer.sendMail(email)
            logger.info("Listing rejection email sent successfully to $toEmail")
        } catch (e: Exception) {
            logger.error("Failed to send listing rejection email to $toEmail: ${e.message}", e)
        }
    }

    /**
     * Helper function untuk build email
     */
    private fun buildEmail(
        to: String,
        subject: String,
        htmlBody: String,
        plainTextBody: String
    ): Email {
        return EmailBuilder.startingBlank()
            .from(appConfig.email.senderName, appConfig.email.senderEmail)
            .to(to)
            .withSubject(subject)
            .withHTMLText(htmlBody)
            .withPlainText(plainTextBody)
            .buildEmail()
    }

    // ============= EMAIL TEMPLATES =============

    private fun buildVerificationEmailHtml(verificationUrl: String): String {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background-color: #4F46E5; color: white; padding: 20px; text-align: center; }
                    .content { background-color: #f9fafb; padding: 30px; }
                    .button { 
                        display: inline-block; 
                        padding: 12px 30px; 
                        background-color: #4F46E5; 
                        color: white; 
                        text-decoration: none; 
                        border-radius: 5px; 
                        margin: 20px 0;
                    }
                    .footer { text-align: center; padding: 20px; color: #6b7280; font-size: 12px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>Verifikasi Email Anda</h1>
                    </div>
                    <div class="content">
                        <h2>Selamat datang di Klik Sewa! 🎉</h2>
                        <p>Terima kasih telah mendaftar. Untuk melanjutkan, silakan verifikasi email Anda dengan mengklik tombol di bawah ini:</p>
                        <div style="text-align: center;">
                            <a href="$verificationUrl" class="button">Verifikasi Email</a>
                        </div>
                        <p>Atau copy dan paste link berikut ke browser Anda:</p>
                        <p style="word-break: break-all; color: #4F46E5;">$verificationUrl</p>
                        <p><strong>Link ini akan kadaluarsa dalam 24 jam.</strong></p>
                        <p>Jika Anda tidak mendaftar di Klik Sewa, abaikan email ini.</p>
                    </div>
                    <div class="footer">
                        <p>&copy; ${java.time.Year.now().value} Klik Sewa. All rights reserved.</p>
                    </div>
                </div>
            </body>
            </html>
        """.trimIndent()
    }

    private fun buildVerificationEmailPlainText(verificationUrl: String): String {
        return """
            Selamat datang di Klik Sewa!
            
            Terima kasih telah mendaftar. Untuk melanjutkan, silakan verifikasi email Anda dengan mengklik link berikut:
            
            $verificationUrl
            
            Link ini akan kadaluarsa dalam 24 jam.
            
            Jika Anda tidak mendaftar di Klik Sewa, abaikan email ini.
            
            © ${java.time.Year.now().value} Klik Sewa. All rights reserved.
        """.trimIndent()
    }

    private fun buildPasswordResetEmailHtml(resetUrl: String): String {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background-color: #DC2626; color: white; padding: 20px; text-align: center; }
                    .content { background-color: #f9fafb; padding: 30px; }
                    .button { 
                        display: inline-block; 
                        padding: 12px 30px; 
                        background-color: #DC2626; 
                        color: white; 
                        text-decoration: none; 
                        border-radius: 5px; 
                        margin: 20px 0;
                    }
                    .footer { text-align: center; padding: 20px; color: #6b7280; font-size: 12px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>Reset Password</h1>
                    </div>
                    <div class="content">
                        <h2>Permintaan Reset Password</h2>
                        <p>Kami menerima permintaan untuk reset password akun Anda. Klik tombol di bawah ini untuk membuat password baru:</p>
                        <div style="text-align: center;">
                            <a href="$resetUrl" class="button">Reset Password</a>
                        </div>
                        <p>Atau copy dan paste link berikut ke browser Anda:</p>
                        <p style="word-break: break-all; color: #DC2626;">$resetUrl</p>
                        <p><strong>Link ini akan kadaluarsa dalam 1 jam.</strong></p>
                        <p>Jika Anda tidak meminta reset password, abaikan email ini. Password Anda tetap aman.</p>
                    </div>
                    <div class="footer">
                        <p>&copy; ${java.time.Year.now().value} Klik Sewa. All rights reserved.</p>
                    </div>
                </div>
            </body>
            </html>
        """.trimIndent()
    }

    private fun buildPasswordResetEmailPlainText(resetUrl: String): String {
        return """
            Permintaan Reset Password
            
            Kami menerima permintaan untuk reset password akun Anda. Klik link berikut untuk membuat password baru:
            
            $resetUrl
            
            Link ini akan kadaluarsa dalam 1 jam.
            
            Jika Anda tidak meminta reset password, abaikan email ini. Password Anda tetap aman.
            
            © ${java.time.Year.now().value} Klik Sewa. All rights reserved.
        """.trimIndent()
    }

    private fun buildListingApprovedEmailHtml(userName: String, listingTitle: String): String {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background-color: #10B981; color: white; padding: 20px; text-align: center; }
                    .content { background-color: #f9fafb; padding: 30px; }
                    .footer { text-align: center; padding: 20px; color: #6b7280; font-size: 12px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>✅ Listing Disetujui</h1>
                    </div>
                    <div class="content">
                        <h2>Halo $userName,</h2>
                        <p>Kabar baik! Listing Anda telah disetujui oleh admin:</p>
                        <p><strong>$listingTitle</strong></p>
                        <p>Listing Anda sekarang dapat dilihat oleh pengguna lain di platform Klik Sewa.</p>
                        <p>Terima kasih telah berkontribusi di Klik Sewa! 🎉</p>
                    </div>
                    <div class="footer">
                        <p>&copy; ${java.time.Year.now().value} Klik Sewa. All rights reserved.</p>
                    </div>
                </div>
            </body>
            </html>
        """.trimIndent()
    }

    private fun buildListingApprovedEmailPlainText(userName: String, listingTitle: String): String {
        return """
            Listing Disetujui
            
            Halo $userName,
            
            Kabar baik! Listing Anda telah disetujui oleh admin:
            $listingTitle
            
            Listing Anda sekarang dapat dilihat oleh pengguna lain di platform Klik Sewa.
            
            Terima kasih telah berkontribusi di Klik Sewa!
            
            © ${java.time.Year.now().value} Klik Sewa. All rights reserved.
        """.trimIndent()
    }

    private fun buildListingRejectedEmailHtml(
        userName: String,
        listingTitle: String,
        reason: String
    ): String {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background-color: #DC2626; color: white; padding: 20px; text-align: center; }
                    .content { background-color: #f9fafb; padding: 30px; }
                    .reason-box { 
                        background-color: #FEE2E2; 
                        border-left: 4px solid #DC2626; 
                        padding: 15px; 
                        margin: 20px 0;
                    }
                    .footer { text-align: center; padding: 20px; color: #6b7280; font-size: 12px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>❌ Listing Ditolak</h1>
                    </div>
                    <div class="content">
                        <h2>Halo $userName,</h2>
                        <p>Mohon maaf, listing Anda tidak dapat disetujui:</p>
                        <p><strong>$listingTitle</strong></p>
                        <div class="reason-box">
                            <strong>Alasan penolakan:</strong><br>
                            $reason
                        </div>
                        <p>Anda dapat memperbaiki listing sesuai dengan alasan di atas dan submit ulang untuk review.</p>
                        <p>Jika ada pertanyaan, silakan hubungi tim support kami.</p>
                    </div>
                    <div class="footer">
                        <p>&copy; ${java.time.Year.now().value} Klik Sewa. All rights reserved.</p>
                    </div>
                </div>
            </body>
            </html>
        """.trimIndent()
    }

    private fun buildListingRejectedEmailPlainText(
        userName: String,
        listingTitle: String,
        reason: String
    ): String {
        return """
            Listing Ditolak
            
            Halo $userName,
            
            Mohon maaf, listing Anda tidak dapat disetujui:
            $listingTitle
            
            Alasan penolakan:
            $reason
            
            Anda dapat memperbaiki listing sesuai dengan alasan di atas dan submit ulang untuk review.
            
            Jika ada pertanyaan, silakan hubungi tim support kami.
            
            © ${java.time.Year.now().value} Klik Sewa. All rights reserved.
        """.trimIndent()
    }
}