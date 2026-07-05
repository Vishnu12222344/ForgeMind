package com.forgemind.security.oauth;

import com.forgemind.auth.dto.AuthResponse;
import com.forgemind.auth.dto.OAuthUserInfo;
import com.forgemind.auth.service.AuthService;
import com.forgemind.common.exception.BadRequestException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler implements org.springframework.security.web.authentication.AuthenticationSuccessHandler {

    private final AuthService authService;
    private final OAuth2AuthorizedClientService authorizedClientService;
    private final RestClient.Builder restClientBuilder;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException, ServletException {

        try {
            if (!(authentication instanceof OAuth2AuthenticationToken oauthToken)) {
                throw new BadRequestException("Invalid OAuth authentication");
            }

            String registrationId = oauthToken.getAuthorizedClientRegistrationId();
            OAuth2User oauth2User = oauthToken.getPrincipal();

            OAuthUserInfo userInfo = switch (registrationId.toLowerCase()) {
                case "google" -> extractGoogleUser(oauth2User);
                case "github" -> extractGithubUser(oauthToken, oauth2User);
                default -> throw new BadRequestException("Unsupported OAuth provider: " + registrationId);
            };

            AuthResponse authResponse = authService.oauthLogin(userInfo);

            String redirectUrl = UriComponentsBuilder
                    .fromUriString(normalizeFrontendUrl() + "/oauth/callback")
                    .queryParam("accessToken", authResponse.accessToken())
                    .queryParam("refreshToken", authResponse.refreshToken())
                    .queryParam("tokenType", authResponse.tokenType())
                    .queryParam("expiresIn", authResponse.expiresIn())
                    .build()
                    .encode()
                    .toUriString();

            response.sendRedirect(redirectUrl);

        } catch (Exception ex) {
            String redirectUrl = UriComponentsBuilder
                    .fromUriString(normalizeFrontendUrl() + "/login")
                    .queryParam("oauthError", ex.getMessage())
                    .build()
                    .encode()
                    .toUriString();

            response.sendRedirect(redirectUrl);
        }
    }

    private OAuthUserInfo extractGoogleUser(OAuth2User user) {
        Map<String, Object> attributes = user.getAttributes();

        String providerId = stringValue(attributes.get("sub"));
        String email = stringValue(attributes.get("email"));
        String name = stringValue(attributes.get("name"));
        String avatarUrl = stringValue(attributes.get("picture"));

        String username = email != null && email.contains("@")
                ? email.substring(0, email.indexOf("@"))
                : name;

        return new OAuthUserInfo(
                "google",
                providerId,
                email,
                name,
                username,
                avatarUrl
        );
    }

    private OAuthUserInfo extractGithubUser(
            OAuth2AuthenticationToken oauthToken,
            OAuth2User user
    ) {
        Map<String, Object> attributes = user.getAttributes();

        String providerId = stringValue(attributes.get("id"));
        String login = stringValue(attributes.get("login"));
        String name = stringValue(attributes.get("name"));
        String avatarUrl = stringValue(attributes.get("avatar_url"));
        String email = stringValue(attributes.get("email"));

        if (email == null || email.isBlank()) {
            email = fetchGithubPrimaryEmail(oauthToken);
        }

        return new OAuthUserInfo(
                "github",
                providerId,
                email,
                name,
                login,
                avatarUrl
        );
    }

    private String fetchGithubPrimaryEmail(OAuth2AuthenticationToken oauthToken) {
        OAuth2AuthorizedClient client = authorizedClientService.loadAuthorizedClient(
                oauthToken.getAuthorizedClientRegistrationId(),
                oauthToken.getName()
        );

        if (client == null || client.getAccessToken() == null) {
            return null;
        }

        String token = client.getAccessToken().getTokenValue();

        try {
            List<GithubEmail> emails = restClientBuilder
                    .baseUrl("https://api.github.com")
                    .defaultHeader("Authorization", "Bearer " + token)
                    .defaultHeader("Accept", "application/vnd.github+json")
                    .build()
                    .get()
                    .uri("/user/emails")
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<GithubEmail>>() {
                    });

            if (emails == null || emails.isEmpty()) {
                return null;
            }

            return emails.stream()
                    .filter(email -> Boolean.TRUE.equals(email.primary()))
                    .filter(email -> Boolean.TRUE.equals(email.verified()))
                    .map(GithubEmail::email)
                    .findFirst()
                    .orElseGet(() -> emails.stream()
                            .filter(email -> Boolean.TRUE.equals(email.verified()))
                            .map(GithubEmail::email)
                            .findFirst()
                            .orElse(null)
                    );

        } catch (Exception ex) {
            return null;
        }
    }

    private String stringValue(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private String normalizeFrontendUrl() {
        if (frontendUrl.endsWith("/")) {
            return frontendUrl.substring(0, frontendUrl.length() - 1);
        }

        return frontendUrl;
    }

    private record GithubEmail(
            String email,
            Boolean primary,
            Boolean verified,
            String visibility
    ) {
    }
}