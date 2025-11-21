package com.utility;

import java.io.File;
import java.util.List;
import java.util.Properties;

import org.apache.logging.log4j.Logger;

import io.github.cdimascio.dotenv.Dotenv;
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

	// =====================================================
	// MULTIPLE RECIPIENTS FROM CONFIG FILE
	// =====================================================
	private static final Dotenv dotenv = Dotenv.configure().filename("Credentials.env").load();
	private static final String RECIPIENTS = String.join(",", dotenv.get("recipient1"),
			dotenv.get("recipient2"));

	private static final String FROM_EMAIL = dotenv.get("email");
	private static final String APP_PASSWORD = dotenv.get("password");

	public static void sendFailureReportWithScreenshots(List<String> failedScenarios, List<String> failedScreenshots) {

		log.info("Preparing to send error report email...");

		try {

			// =============================
			// 1. EMAIL SERVER PROPERTIES
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
			// 2. CREATE EMAIL MESSAGE
			// =============================
			MimeMessage message = new MimeMessage(session);
			message.setFrom(new InternetAddress(FROM_EMAIL));

			// Set multiple recipients
			message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(RECIPIENTS));

			// Subject
			message.setSubject(" NECF SITE DOWN — NEED IMMEDIATE ATTENTION!!!");

			// =============================
			// 3. EMAIL BODY
			// =============================
			MimeBodyPart messageBodyPart = new MimeBodyPart();

			StringBuilder htmlContent = new StringBuilder();

			htmlContent.append("<h2 style='color:red;'> NECF Website Health Check Failed</h2>");
			htmlContent.append("<p>The automation detected that the NECF site is <b>not reachable</b>.</p>");

			htmlContent.append("<h3> Failed Scenarios:</h3><ul>");
			for (String scenario : failedScenarios) {
				htmlContent.append("<li>").append(scenario).append("</li>");
			}
			htmlContent.append("</ul>");

			htmlContent.append("<p>Please check the issue immediately.</p>");
			htmlContent.append("<p>Regards,<br>Automation Monitoring System</p>");

			// Set HTML
			messageBodyPart.setContent(htmlContent.toString(), "text/html");

			Multipart multipart = new MimeMultipart();
			multipart.addBodyPart(messageBodyPart);

			// =============================
			// 4. ATTACH SCREENSHOTS
			// =============================
			for (String screenshotPath : failedScreenshots) {
				File file = new File(screenshotPath);

				if (file.exists()) {
					MimeBodyPart attachPart = new MimeBodyPart();
					FileDataSource source = new FileDataSource(file);
					attachPart.setDataHandler(new DataHandler(source));
					attachPart.setFileName(file.getName());
					multipart.addBodyPart(attachPart);
					log.info("Screenshot attached: " + file.getAbsolutePath());
				} else {
					log.warn("Screenshot NOT FOUND, skipped: " + screenshotPath);
				}
			}

			// =============================
			// 5. ATTACH HTML REPORT
			// =============================
			File htmlReport = new File("target/ExtentReports/SparkReport.html");
			if (htmlReport.exists()) {
				MimeBodyPart htmlPart = new MimeBodyPart();
				htmlPart.attachFile(htmlReport);
				multipart.addBodyPart(htmlPart);
				log.info("HTML report attached.");
			} else {
				log.warn("HTML report NOT FOUND.");
			}

			// =============================
			// 6. ATTACH PDF REPORT
			// =============================
			File pdfReport = new File("target/ExtentReports/ExtentReport.pdf");
			if (pdfReport.exists()) {
				MimeBodyPart pdfPart = new MimeBodyPart();
				pdfPart.attachFile(pdfReport);
				multipart.addBodyPart(pdfPart);
				log.info("PDF report attached.");
			} else {
				log.warn("PDF report NOT FOUND.");
			}

			// =============================
			// 7. FINAL EMAIL ASSEMBLE
			// =============================
			message.setContent(multipart);

			// =============================
			// 8. SEND EMAIL
			// =============================
			Transport.send(message);

			log.info("📧 Email sent successfully to: " + RECIPIENTS);

		} catch (Exception e) {
			log.error("❌ Error sending email: " + e.getMessage());
		}
	}
}
