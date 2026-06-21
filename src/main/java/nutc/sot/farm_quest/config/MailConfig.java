package nutc.sot.farm_quest.config;

import java.util.Properties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

@Configuration
@ConditionalOnProperty(prefix = "farm-quest.auth", name = "mail-mode", havingValue = "smtp")
public class MailConfig {

    @Bean
    public JavaMailSender javaMailSender(AuthProperties authProperties) {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(authProperties.getMailHost());
        if (authProperties.getMailPort() != null) {
            mailSender.setPort(authProperties.getMailPort());
        }
        mailSender.setUsername(authProperties.getMailUsername());
        mailSender.setPassword(authProperties.getMailPassword());

        Properties properties = mailSender.getJavaMailProperties();
        properties.put("mail.transport.protocol", "smtp");
        properties.put("mail.smtp.auth", authProperties.isMailAuth());
        properties.put("mail.smtp.starttls.enable", authProperties.isMailStarttlsEnabled());
        properties.put("mail.smtp.ssl.enable", authProperties.isMailSslEnabled());
        properties.put("mail.smtp.connectiontimeout", 5000);
        properties.put("mail.smtp.timeout", 5000);
        properties.put("mail.smtp.writetimeout", 5000);
        return mailSender;
    }
}
