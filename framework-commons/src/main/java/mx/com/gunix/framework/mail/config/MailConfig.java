package mx.com.gunix.framework.mail.config;

import java.util.Properties;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;

@Configuration
public class MailConfig {
	static final Logger log = Logger.getLogger(MailConfig.class);
	@Bean
	public MailSender mailSender() {
		JavaMailSenderImpl mailSender = new JavaMailSenderImpl();

		mailSender.setHost(System.getenv("MAIL_SERVER"));
		try {
			mailSender.setPort(Integer.parseInt(System.getenv("MAIL_PORT")));
		} catch (NumberFormatException nfe) {
			log.error("Error al inicializar MailSender, si no utilizas MailSender en tu aplicación entonces no hay problema :D");
		}
		mailSender.setUsername(System.getenv("MAIL_USERNAME"));
		mailSender.setPassword(System.getenv("MAIL_PASSWORD"));
		String mailProperties = System.getenv("MAIL_PROPERTIES");
		if (mailProperties != null && !"".equals(mailProperties)) {
			Properties mailProps = new Properties();
			String[] props = mailProperties.split("&");
			for (String prop : props) {
				String[] propVal = prop.split("=");
				mailProps.setProperty(propVal[0], propVal[1]);
			}
			mailSender.setJavaMailProperties(mailProps);
		}
		return mailSender;
	}

	@Bean
	@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	public SimpleMailMessage mailMessageTemplate() {
		SimpleMailMessage smm = new SimpleMailMessage();
		smm.setFrom(System.getenv("MAIL_FROM"));
		return smm;
	}

}
