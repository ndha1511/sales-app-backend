package com.code.salesappbackend.oauth2;

import com.code.salesappbackend.dtos.responses.auth.LoginResponse;
import com.code.salesappbackend.models.auth.Token;
import com.code.salesappbackend.models.user.User;
import com.code.salesappbackend.models.auth.UserDetail;
import com.code.salesappbackend.models.enums.Role;
import com.code.salesappbackend.repositories.user.UserRepository;
import com.code.salesappbackend.services.interfaces.auth.JwtService;
import com.code.salesappbackend.services.interfaces.auth.TokenService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class Oauth2SuccessLogin implements AuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final TokenService tokenService;
    @Value("${front-end.url}")
    private String frontendUrl;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) throws IOException {

        OAuth2User principal = (OAuth2User) authentication.getPrincipal();

        OAuth2AuthenticationToken oAuth2AuthenticationToken = (OAuth2AuthenticationToken) authentication;
        String registrationId = oAuth2AuthenticationToken.getAuthorizedClientRegistrationId();
        User user = new User();
        user.setPhoneNumber("");
        user.setVerify(true);
        user.setPassword("");
        user.setRole(Role.ROLE_USER);
        switch (registrationId) {
            case "facebook": {
                FacebookAccount facebookAccount = new FacebookAccount(
                        principal.getName(),
                        Objects.requireNonNull(principal.getAttribute("name")).toString(),
                        Objects.requireNonNull(principal.getAttribute("email")).toString()
                );
                user.setEmail(facebookAccount.getEmail());
                user.setName(facebookAccount.getName());
                user.setFacebookAccountId(facebookAccount.getAccountId());
                break;
            }
            case "google": {
                GoogleAccount googleAccount = new GoogleAccount(
                        principal.getName(),
                        Objects.requireNonNull(principal.getAttribute("name")).toString(),
                        Objects.requireNonNull(principal.getAttribute("email")).toString(),
                        Objects.requireNonNull(principal.getAttribute("picture")).toString()
                );
                user.setEmail(googleAccount.getEmail());
                user.setName(googleAccount.getName());
                user.setGoogleAccountId(googleAccount.getAccountId());
                user.setAvatarUrl(googleAccount.getPictureUrl());
                break;
            }
            default: {
                break;
            }
        }

        User userDb = userRepository.findByEmail(user.getEmail()).orElse(user);
        if(userDb.getFacebookAccountId() == null) userDb.setFacebookAccountId(user.getFacebookAccountId());
        if(userDb.getFacebookAccountId() == null) userDb.setGoogleAccountId(user.getGoogleAccountId());
        userRepository.save(userDb);
        LoginResponse loginResponse = handlerLogin(userDb);

        // Set cookies
        Cookie accessTokenCookie = new Cookie("accessToken", loginResponse.getAccessToken());
//        accessTokenCookie.setSecure(true); for https
        accessTokenCookie.setPath("/");
        accessTokenCookie.setMaxAge(30);

        Cookie refreshTokenCookie = new Cookie("refreshToken", loginResponse.getRefreshToken());
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setMaxAge(30);

        response.addCookie(accessTokenCookie);
        response.addCookie(refreshTokenCookie);

        String url = String.format("%s/auth/login-success?email=%s", frontendUrl, userDb.getEmail());
        response.sendRedirect(url);
    }

    private LoginResponse handlerLogin(User user) {
        UserDetail userDetail = new UserDetail(user);
        Token token = new Token();
        token.setUser(user);
        token.setAccessToken(jwtService.generateToken(userDetail));
        token.setRefreshToken(jwtService.generateRefreshToken(new HashMap<>(), userDetail));
        token.setExpiredDate(LocalDateTime.now());
        tokenService.saveToken(user, token);
        return LoginResponse.builder()
                .accessToken(token.getAccessToken())
                .refreshToken(token.getRefreshToken())
                .build();
    }


}
