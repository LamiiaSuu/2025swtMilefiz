package de.hs_rm.de.milefiz.game.controller;

import static org.mockito.Mockito.*;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;

import org.junit.jupiter.api.Test;

class FrontendControllerTest {

    private final FrontendController controller = new FrontendController();

    @Test
    void nonApiRequest_isForwardedToRoot() throws Exception {
        Filter filter = controller
                .nonApiRequestToRootPathForwarderFilterRegistrationbean()
                .getFilter();

        HttpServletRequest request = mock(HttpServletRequest.class);
        ServletResponse response = mock(ServletResponse.class);
        FilterChain chain = mock(FilterChain.class);
        RequestDispatcher dispatcher = mock(RequestDispatcher.class);

        when(request.getRequestURI()).thenReturn("/game/duel/123");
        when(request.getRequestDispatcher("/")).thenReturn(dispatcher);

        filter.doFilter(request, response, chain);

        verify(dispatcher).forward(request, response);
        verify(chain, never()).doFilter(any(), any());
    }

    @Test
    void apiRequest_isNotForwarded() throws Exception {
        Filter filter = controller
                .nonApiRequestToRootPathForwarderFilterRegistrationbean()
                .getFilter();

        HttpServletRequest request = mock(HttpServletRequest.class);
        ServletResponse response = mock(ServletResponse.class);
        FilterChain chain = mock(FilterChain.class);

        when(request.getRequestURI()).thenReturn("/api/game/state");

        filter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
        verifyNoMoreInteractions(chain);
    }

    @Test
    void websocketRequest_isNotForwarded() throws Exception {
        Filter filter = controller
                .nonApiRequestToRootPathForwarderFilterRegistrationbean()
                .getFilter();

        HttpServletRequest request = mock(HttpServletRequest.class);
        ServletResponse response = mock(ServletResponse.class);
        FilterChain chain = mock(FilterChain.class);

        when(request.getRequestURI()).thenReturn("/ws/connect");

        filter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
    }

    @Test
    void rootRequest_isNotForwarded() throws Exception {
        Filter filter = controller
                .nonApiRequestToRootPathForwarderFilterRegistrationbean()
                .getFilter();

        HttpServletRequest request = mock(HttpServletRequest.class);
        ServletResponse response = mock(ServletResponse.class);
        FilterChain chain = mock(FilterChain.class);

        when(request.getRequestURI()).thenReturn("/");

        filter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
    }

    @Test
    void staticResource_isNotForwarded() throws Exception {
        Filter filter = controller
                .nonApiRequestToRootPathForwarderFilterRegistrationbean()
                .getFilter();

        HttpServletRequest request = mock(HttpServletRequest.class);
        ServletResponse response = mock(ServletResponse.class);
        FilterChain chain = mock(FilterChain.class);

        when(request.getRequestURI()).thenReturn("/assets/main.js");

        filter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
    }
}
