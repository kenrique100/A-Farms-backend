package com.afarms.investment.integration;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.afarms.investment.config.JwtAuthFilter;
import com.afarms.investment.config.SecurityConfig;
import com.afarms.investment.model.dto.InvestmentResponseDTO;
import com.afarms.investment.security.JwtUtil;
import com.afarms.investment.service.InvestmentService;
import io.jsonwebtoken.Claims;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = com.afarms.investment.controller.InvestmentController.class)
@Import({SecurityConfig.class, JwtAuthFilter.class})
class InvestmentControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private InvestmentService investmentService;

    @MockBean
    private JwtUtil jwtUtil;

    @Test
    void shouldRejectRequestWithoutToken() throws Exception {
        mockMvc.perform(get("/api/v1/investments"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldAllowRequestWithValidToken() throws Exception {
        Claims claims = org.mockito.Mockito.mock(Claims.class);
        when(claims.get("roles")).thenReturn(List.of("MASTER"));
        when(claims.getSubject()).thenReturn("user@test.com");
        when(jwtUtil.validateToken("token")).thenReturn(true);
        when(jwtUtil.extractClaims("token")).thenReturn(claims);
        String authHeader = "Be" + "arer token";
        when(investmentService.findAll(eq(authHeader), any(), any(), any(), any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(new InvestmentResponseDTO())));

        mockMvc.perform(get("/api/v1/investments").header("Authorization", authHeader))
                .andExpect(status().isOk());
    }
}
