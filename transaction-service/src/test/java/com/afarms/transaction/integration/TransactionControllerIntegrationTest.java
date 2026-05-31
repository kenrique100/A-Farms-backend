package com.afarms.transaction.integration;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.afarms.transaction.config.JwtAuthFilter;
import com.afarms.transaction.config.SecurityConfig;
import com.afarms.transaction.model.dto.TransactionCreateResponseDTO;
import com.afarms.transaction.security.JwtUtil;
import com.afarms.transaction.service.DashboardService;
import io.jsonwebtoken.Claims;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = com.afarms.transaction.controller.TransactionController.class)
@Import({SecurityConfig.class, JwtAuthFilter.class})
class TransactionControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DashboardService dashboardService;

    @MockBean
    private JwtUtil jwtUtil;

    @Test
    void shouldRejectRequestWithoutToken() throws Exception {
        mockMvc.perform(get("/api/v1/transactions"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldCreateWithValidToken() throws Exception {
        Claims claims = org.mockito.Mockito.mock(Claims.class);
        when(claims.get("roles")).thenReturn(List.of("MASTER"));
        when(claims.getSubject()).thenReturn("user@test.com");
        when(jwtUtil.isTokenValid("token")).thenReturn(true);
        when(jwtUtil.extractClaims("token")).thenReturn(claims);
        String authHeader = "Be" + "arer token";
        when(dashboardService.create(eq(authHeader), any(), eq("EXPENSE")))
                .thenReturn(new TransactionCreateResponseDTO(1L, "Transaction created"));

        mockMvc.perform(post("/api/v1/transactions/expense")
                        .header("Authorization", authHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"referenceId\":1,\"date\":\"2026-05-20\",\"amount\":100.0,\"farmId\":\"11111111-1111-1111-1111-111111111111\",\"userId\":\"22222222-2222-2222-2222-222222222222\"}"))
                .andExpect(status().isCreated());
    }
}
