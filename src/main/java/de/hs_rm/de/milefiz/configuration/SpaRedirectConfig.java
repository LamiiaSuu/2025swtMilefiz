package de.hs_rm.de.milefiz.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class SpaRedirectConfig implements WebMvcConfigurer {

    /**
     * SPA-Seite (von frontend generierte index.html) wird bei unbekannten
     * Seiten geforwardet, sodass man clientseitig mehr Kontrolle hat. z.B. für
     * join-Endpunkt
     */
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/{path:^(?!api|actuator|ws|assets|static|favicon\\.ico).*$}")
                .setViewName("forward:/index.html");
        registry.addViewController("/**")
                .setViewName("forward:/index.html");
    }
}
