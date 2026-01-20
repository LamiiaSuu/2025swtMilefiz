package de.hs_rm.de.milefiz.game.controller;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Controller;

import jakarta.servlet.Filter;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;

// https://stackoverflow.com/questions/62012156/spring-requestmapping-for-everything-except-api-or-rest-negate-specific-wo

@Controller
public class FrontendController {

    @Bean
    public FilterRegistrationBean<Filter> nonApiRequestToRootPathForwarderFilterRegistrationbean() {
        FilterRegistrationBean<Filter> registrationBean = new FilterRegistrationBean<>();

        registrationBean.setFilter((request, response, chain) -> {
            HttpServletRequest httpRequest = (HttpServletRequest) request;
            String uri = httpRequest.getRequestURI();

            boolean isApi = uri.startsWith("/api/");
            boolean isWs = uri.startsWith("/ws");
            boolean isRoot = uri.equals("/");
            boolean isStaticResource =
                    uri.lastIndexOf('/') < uri.lastIndexOf('.');

            if (!isApi && !isWs && !isRoot && !isStaticResource) {
                RequestDispatcher dispatcher = request.getRequestDispatcher("/");
                dispatcher.forward(request, response);
                return;
            }

            chain.doFilter(request, response);
        });

        return registrationBean;
    }
}
