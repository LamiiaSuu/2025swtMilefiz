package de.hs_rm.de.milefiz.game.controller;

import java.io.IOException;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;

// https://stackoverflow.com/questions/62012156/spring-requestmapping-for-everything-except-api-or-rest-negate-specific-wo

@Controller
public class FrontendController {

    @Bean
    public FilterRegistrationBean nonApiRequestToRootPathForwarderFilterRegistrationbean() {
        FilterRegistrationBean<Filter> filterFilterRegistrationBean = new FilterRegistrationBean<>();
        filterFilterRegistrationBean.setFilter(new Filter() {
            @Override
            public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
                HttpServletRequest request1 = (HttpServletRequest) request;
                if (!request1.getRequestURI().startsWith("/api/")
                    && !request1.getRequestURI().startsWith("/ws")
                    && !request1.getRequestURI().equals("/")
                    && !request1.getRequestURI().matches("/(\\w+/)*.+\\.\\w+$")) {
                    System.out.println(request1.getRequestURI());
                    RequestDispatcher requestDispatcher = request.getRequestDispatcher("/");
                    requestDispatcher.forward(request, response);
                    return;
                }

                chain.doFilter(request, response);
            }
        });
        return filterFilterRegistrationBean;
    }
}
