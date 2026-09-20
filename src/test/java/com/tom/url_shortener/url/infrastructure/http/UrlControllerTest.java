package com.tom.url_shortener.url.infrastructure.http;

import com.tom.url_shortener.url.application.dto.ShortenUrlCommand;
import com.tom.url_shortener.url.application.dto.ShortenUrlResponse;
import com.tom.url_shortener.url.application.usecase.GetOriginalUrlUseCase;
import com.tom.url_shortener.url.application.usecase.ShortenUrlUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UrlController.class)
class UrlControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ShortenUrlUseCase shortenUrlUseCase;

    @MockitoBean
    private GetOriginalUrlUseCase getOriginalUrlUseCase;

    @Test
    @DisplayName("Deve retornar 201 Created quando o payload contiver uma URL valida")
    void shouldReturn201WhenUrlIsValid() throws Exception {
        ShortenUrlResponse response = ShortenUrlResponse.builder()
                .id(1L)
                .originalUrl("https://example.com")
                .shortCode("abc1234567")
                .build();

        when(shortenUrlUseCase.execute(any(ShortenUrlCommand.class))).thenReturn(response);

        String jsonPayload = """
                {
                    "url": "https://example.com"
                }
                """;

        mockMvc.perform(post("/api/urls")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.originalUrl").value("https://example.com"))
                .andExpect(jsonPath("$.shortCode").value("abc1234567"))
                .andExpect(jsonPath("$.shortUrl").isNotEmpty());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   "})
    @DisplayName("Deve retornar 400 Bad Request quando a URL for vazia ou em branco")
    void shouldReturn400WhenUrlIsBlank(String blankUrl) throws Exception {
        String jsonPayload = String.format("""
                {
                    "url": "%s"
                }
                """, blankUrl);

        mockMvc.perform(post("/api/urls")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Bad Request"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors.url").isNotEmpty());
    }

    @ParameterizedTest
    @ValueSource(strings = {"ftp://example.com", "not-a-valid-url", "javascript:alert(1)"})
    @DisplayName("Deve retornar 400 Bad Request quando a URL tiver protocolo ou formato invalido")
    void shouldReturn400WhenUrlHasInvalidProtocolOrFormat(String invalidUrl) throws Exception {
        String jsonPayload = String.format("""
                {
                    "url": "%s"
                }
                """, invalidUrl);

        mockMvc.perform(post("/api/urls")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Bad Request"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors.url").isNotEmpty());
    }

    @Test
    @DisplayName("Deve retornar 400 Bad Request quando a URL exceder 2048 caracteres")
    void shouldReturn400WhenUrlExceedsMaxSize() throws Exception {
        String excessivelyLongUrl = "https://example.com/" + "a".repeat(2050);
        String jsonPayload = String.format("""
                {
                    "url": "%s"
                }
                """, excessivelyLongUrl);

        mockMvc.perform(post("/api/urls")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Bad Request"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors.url").isNotEmpty());
    }
}
