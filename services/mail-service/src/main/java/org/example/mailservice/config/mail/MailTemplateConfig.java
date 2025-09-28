package org.example.mailservice.config.mail;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import org.thymeleaf.templateresolver.ITemplateResolver;

import java.nio.charset.StandardCharsets;

@Configuration
public class MailTemplateConfig {

    @Bean("emailTemplateResolver")
    public ITemplateResolver emailTemplateResolver(
            @Value("${app.mail.template-cache:false}") boolean cacheable) {
        ClassLoaderTemplateResolver r = new ClassLoaderTemplateResolver();
        r.setPrefix("mail/");
        r.setSuffix(".html");
        r.setTemplateMode(TemplateMode.HTML);
        r.setCharacterEncoding(StandardCharsets.UTF_8.name());
        r.setCacheable(cacheable);
        r.setCheckExistence(true);
        r.setOrder(1);
        return r;
    }

    @Bean("emailTemplateEngine")
    public SpringTemplateEngine emailTemplateEngine(
            @Qualifier("emailTemplateResolver") ITemplateResolver resolver,
            MessageSource messageSource
    ) {
        SpringTemplateEngine engine = new SpringTemplateEngine();
        engine.setTemplateResolver(resolver);
        engine.setTemplateEngineMessageSource(messageSource);
        return engine;
    }
}
