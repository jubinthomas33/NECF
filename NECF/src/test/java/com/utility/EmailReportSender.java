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

    private static String getReportPath(String fileName) {
        String basePath = "target" + File.separator + "ExtentReports" + File.separator;
        return basePath + fileName;
    }

    private static String getRecipients() {
        StringBuilder recipients = new StringBuilder();
        String[] keys = {"RECIPIENT1","RECIPIENT2","RECIPIENT3","RECIPIENT4","RECIPIENT5"};
        for(String key : keys) {
            String email = System.getenv(key);
            if(email != null && !email.isEmpty()) {
                if(recipients.length() > 0) recipients.append(",");
                recipients.append(email);
            }
        }
        return recipients.toString();
    }

    private static final String FROM_EMAIL = System.getenv("EMAIL");
    private static final String APP_PASSWORD = System.getenv("PASSWORD");

    public static void sendFailureReportWithScreenshots(List<String> failedScenarios, List<String> failedScreenshots) {

        log.info("========== EMAIL DEBUG START ==========");

        boolean canSend = true;
        if(FROM_EMAIL == null || FROM_EMAIL.isEmpty()) { log.warn("⚠️ EMAIL env variable NOT SET."); canSend = false; }
        if(APP_PASSWORD == null || APP_PASSWORD.isEmpty()) { log.warn("⚠️ PASSWORD env variable NOT SET."); canSend = false; }
        String recipients = getRecipients();
        if(recipients.isEmpty()) { log.warn("⚠️ No recipients found."); canSend = false; }

        if(!canSend) { log.info("❌ EMAIL SENDING CANCELLED."); log.info("========== EMAIL DEBUG END =========="); return; }

        try {
            Properties props = new Properties();
            props.put("mail.smtp.host", "smtp.gmail.com");
            props.put("mail.smtp.port", "587");
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");

            Session session = Session.getInstance(props, new Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(FROM_EMAIL, APP_PASSWORD);
                }
            });

            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(FROM_EMAIL));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipients));
            message.setSubject("NECF SITE DOWN — ATTENTION REQUIRED!");
            

            MimeBodyPart htmlPart = new MimeBodyPart();
            StringBuilder htmlContent = new StringBuilder();
            htmlContent.append("<h2 style='color:red;'>NECF Website Health Check Failed</h2>");
            htmlContent.append("<p>Automation detected NECF site downtime.</p>");
            htmlContent.append("<h3>Failed Scenarios:</h3><ul>");
            for(String scenario : failedScenarios) htmlContent.append("<li>").append(scenario).append("</li>");
            htmlContent.append("</ul>");
            htmlPart.setContent(htmlContent.toString(), "text/html");

            Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(htmlPart);

            // Screenshots
            for(String path : failedScreenshots) {
                File file = new File(path);
                if(file.exists()) {
                    MimeBodyPart attach = new MimeBodyPart();
                    attach.setDataHandler(new DataHandler(new FileDataSource(file)));
                    attach.setFileName(file.getName());
                    multipart.addBodyPart(attach);
                    log.info("✔ Screenshot attached: " + file.getAbsolutePath());
                }
            }

            // HTML report attachment - only attach if file exists and is not empty
            File htmlReport = new File(getReportPath("SparkReport.html"));
            if(htmlReport.exists() && htmlReport.length() > 1000) { // Minimum 1KB to ensure it's not empty
                MimeBodyPart htmlAttach = new MimeBodyPart();
                htmlAttach.attachFile(htmlReport);
                multipart.addBodyPart(htmlAttach);
                log.info("✔ HTML report attached (Size: " + htmlReport.length() + " bytes)");
            } else {
                if (!htmlReport.exists()) {
                    log.warn("⚠️ HTML report not found: " + htmlReport.getAbsolutePath());
                } else {
                    log.warn("⚠️ HTML report is empty or too small (Size: " + htmlReport.length() + " bytes). Not attaching to email.");
                }
            }

            message.setContent(multipart);

            Transport.send(message);
            log.info("📧 ✔ Email sent successfully!");

        } catch(Exception e) {
            log.error("❌ EMAIL SEND FAILED: " + e.getMessage(), e);
        }

        log.info("========== EMAIL DEBUG END ==========");
    }
}