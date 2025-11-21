package com.utility;

import java.io.File;
import java.util.List;
import java.util.Properties;

import org.apache.logging.log4j.Logger;

import jakarta.activation.DataHandler;
import jakarta.activation.FileDataSource;
import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.Multipart;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;

public class EmailReportSender {

    private static final Logger log = LoggerHelper.getLogger(EmailReportSender.class);

    /**
     * Dynamically fetch recipients from environment variables.
     * Supports up to 5 recipients (RECIPIENT1 to RECIPIENT5)
     * Ignores empty or undefined values.
     */
    private static String getRecipients() {
        StringBuilder recipients = new StringBuilder();
        String[] keys = { "RECIPIENT1", "RECIPIENT2", "RECIPIENT3", "RECIPIENT4", "RECIPIENT5" };

        for (String key : keys) {
            String email = System.getenv(key);
            if (email != null && !email.trim().isEmpty()) {
                if (recipients.length() > 0) recipients.append(",");
                recipients.append(email.trim());
            }
        }
        return recipients.toString();
    }

    private static final String FROM_EMAIL = System.getenv("EMAIL");
    private static final String APP_PASSWORD = System.getenv("PASSWORD");

    public static void sendFailureReportWithScreenshots(List<String> failedScenarios, List<String> failedScreenshots) {

        // Fetch recipients dynamically at runtime
        String recipients = getRecipients();

        if (recipients.isEmpty()) {
            log.warn("⚠️ No recipients provided. Email sending skipped.");
            return; // Exit if no recipients defined
        }

        log.info("Preparing to send error report email...");
        log.info("From: " + FROM_EMAIL + ", To: " + recipients);

        try {
            // =============================
            // 1. Email server properties
            // =============================
            Properties props = new Properties();
            props.put("mail.smtp.host", "smtp.gmail.com");
            props.put("mail.smtp.port", "587");
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");

            Session session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(FROM_EMAIL, APP_PASSWORD);
                }
            });

            // =============================
            // 2. Create email message
            // =============================
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(FROM_EMAIL));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipients));
            message.setSubject("NECF SITE DOWN — NEED IMMEDIATE ATTENTION!!!");

            // =============================
            // 3. Email HTML body
            // =============================
            MimeBodyPart messageBodyPart = new MimeBodyPart();
            StringBuilder htmlContent = new StringBuilder();
            htmlContent.append("<h2 style='color:red;'>NECF Website Health Check Failed</h2>");
            htmlContent.append("<p>The automation detected that the NECF site is <b>not reachable</b>.</p>");
            htmlContent.append("<h3>Failed Scenarios:</h3><ul>");
            for (String scenario : failedScenarios) {
                htmlContent.append("<li>").append(scenario).append("</li>");
            }
            htmlContent.append("</ul>");
            htmlContent.append("<p>Please check the issue immediately.</p>");
            htmlContent.append("<p>Regards,<br>Automation Monitoring System</p>");
            messageBodyPart.setContent(htmlContent.toString(), "text/html");

            Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(messageBodyPart);

            // =============================
            // 4. Attach screenshots
            // =============================
            for (String screenshotPath : failedScreenshots) {
                File file = new File(screenshotPath);
                if (file.exists()) {
                    MimeBodyPart attachPart = new MimeBodyPart();
                    attachPart.setDataHandler(new DataHandler(new FileDataSource(file)));
                    attachPart.setFileName(file.getName());
                    multipart.addBodyPart(attachPart);
                    log.info("Screenshot attached: " + file.getAbsolutePath());
                } else {
                    log.warn("Screenshot NOT FOUND, skipped: " + screenshotPath);
                }
            }

            // =============================
            // 5. Attach HTML report
            // =============================
            File htmlReport = new File("target/ExtentReports/SparkReport.html");
            if (htmlReport.exists()) {
                MimeBodyPart htmlPart = new MimeBodyPart();
                htmlPart.attachFile(htmlReport);
                multipart.addBodyPart(htmlPart);
                log.info("HTML report attached: " + htmlReport.getAbsolutePath());
            } else {
                log.warn("HTML report NOT FOUND.");
            }

            // =============================
            // 6. Attach PDF report
            // =============================
            File pdfReport = new File("target/ExtentReports/ExtentReport.pdf");
            if (pdfReport.exists()) {
                MimeBodyPart pdfPart = new MimeBodyPart();
                pdfPart.attachFile(pdfReport);
                multipart.addBodyPart(pdfPart);
                log.info("PDF report attached: " + pdfReport.getAbsolutePath());
            } else {
                log.warn("PDF report NOT FOUND.");
            }

            // =============================
            // 7. Assemble and send email
            // =============================
            message.setContent(multipart);
            Transport.send(message);

            log.info("📧 Email sent successfully to: " + recipients);

        } catch (Exception e) {
            log.error("❌ Error sending email: ", e);
        }

    }
}
