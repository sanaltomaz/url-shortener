package com.tom.url_shortener.url.infrastructure.http.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.URL;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShortenUrlRequest {

    @NotBlank(message = "A URL não pode estar em branco")
    @Size(max = 2048, message = "A URL não pode exceder 2048 caracteres")
    @URL(regexp = "^https?://.+", message = "A URL deve ser válida e utilizar o protocolo http ou https")
    private String url;
}
