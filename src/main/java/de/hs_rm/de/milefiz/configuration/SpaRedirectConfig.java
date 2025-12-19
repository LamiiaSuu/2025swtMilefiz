package de.hs_rm.de.milefiz.configuration;

import java.io.IOException;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;

@Configuration
public class SpaRedirectConfig implements WebMvcConfigurer {

    /**
     * SPA-Seite (von frontend generierte index.html) wird bei unbekannten
     * Seiten geforwardet, sodass man clientseitig mehr Kontrolle hat. z.B. für
     * join-Endpunkt
     */
    // @Override
    // public void addViewControllers(ViewControllerRegistry registry) {
    //     registry.addViewController("/{path:^(?!api|actuator|ws|assets|static|favicon\\.ico).*$}")
    //             .setViewName("forward:/index.html");
    //     registry.addViewController("/**")
    //             .setViewName("forward:/index.html");
    // }
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/")
                .resourceChain(true)
                .addResolver(new PathResourceResolver() {
                    @Override
                    protected Resource getResource(String resourcePath, Resource location) throws IOException {
                        // Exclude backend endpoints
                        if (resourcePath.startsWith("api/")
                                || resourcePath.startsWith("ws")
                                || resourcePath.startsWith("sockjs")) {
                            return null;
                        }
                        Resource requestedResource = location.createRelative(resourcePath);
                        return (requestedResource.exists() && requestedResource.isReadable())
                                ? requestedResource
                                : new ClassPathResource("/static/index.html");
                    }
                });
    }
}
