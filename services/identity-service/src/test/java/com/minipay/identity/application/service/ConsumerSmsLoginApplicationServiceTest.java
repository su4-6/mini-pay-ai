package com.minipay.identity.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.minipay.identity.application.port.ConsumerAccountPort;
import com.minipay.identity.application.port.ConsumerAuthorizationCodePort;
import com.minipay.identity.application.port.ConsumerAuthorizationCodePort.IssuedAuthorizationCode;
import com.minipay.identity.application.port.LoginAuditPort;
import com.minipay.identity.application.service.ConsumerSmsChallengeService.VerifiedMobile;
import com.minipay.identity.application.service.ConsumerSmsLoginApplicationService.ConsumerSmsLoginCommand;
import com.minipay.identity.application.service.ConsumerSmsLoginApplicationService.ConsumerSmsLoginResult;
import com.minipay.identity.domain.model.ConsumerPrincipal;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ConsumerSmsLoginApplicationServiceTest {
    private final ConsumerSmsChallengeService challenges = mock(ConsumerSmsChallengeService.class);
    private final PhoneNumberService phoneNumbers = mock(PhoneNumberService.class);
    private final ConsumerAccountPort accounts = mock(ConsumerAccountPort.class);
    private final ConsumerAuthorizationCodePort authorizationCodes = mock(ConsumerAuthorizationCodePort.class);
    private final LoginAuditPort audits = mock(LoginAuditPort.class);
    private final MerchantLoginPasswordService merchantPasswords = mock(MerchantLoginPasswordService.class);
    private final ConsumerSmsLoginApplicationService service = new ConsumerSmsLoginApplicationService(
            challenges, phoneNumbers, accounts, authorizationCodes, audits, merchantPasswords);

    @Test
    void verifiesSmsThenCreatesOrFindsAccountIssuesCodeAndWritesSuccessAudit() {
        UUID userId = UUID.randomUUID();
        ConsumerPrincipal consumer = new ConsumerPrincipal(
                userId, "Test Consumer", true, false, "VERIFIED", false);
        Instant expiresAt = Instant.parse("2026-08-26T00:05:00Z");

        when(challenges.consume("challenge-1", "123456"))
                .thenReturn(new VerifiedMobile("13800138000"));
        when(phoneNumbers.hash("13800138000")).thenReturn(new byte[] {1, 2, 3});
        when(phoneNumbers.mask("13800138000")).thenReturn("138****8000");
        when(accounts.findOrCreate(any(), eq("request-1"))).thenReturn(consumer);
        when(authorizationCodes.issue(
                eq(consumer), eq("consumer-app"), eq("minipay://callback"),
                eq("challenge"), eq("S256"), eq("device-1")))
                .thenReturn(new IssuedAuthorizationCode("authorization-code", expiresAt));
        when(merchantPasswords.configured(userId)).thenReturn(false);

        ConsumerSmsLoginResult result = service.verify(command());

        assertThat(result.authorizationCode()).isEqualTo("authorization-code");
        assertThat(result.phone()).isEqualTo("13800138000");
        assertThat(result.onboardingRequired()).isTrue();
        verify(accounts).recordVerifiedPhone(userId, "13800138000", "138****8000");
        verify(audits).appendLogin(
                userId, "13800138000", "CONSUMER_SMS", "SUCCESS",
                "127.0.0.1", "test-agent", "request-1");
    }

    @Test
    void recordsFailureAuditWhenSmsVerificationIsRejected() {
        when(challenges.consume("challenge-1", "123456"))
                .thenThrow(new LoginRejectedException("SMS_INVALID"));

        assertThatThrownBy(() -> service.verify(command()))
                .isInstanceOf(LoginRejectedException.class);

        verify(audits).appendLogin(
                null, "challenge-1", "CONSUMER_SMS", "SMS_FAILED",
                "127.0.0.1", "test-agent", "request-1");
    }

    private ConsumerSmsLoginCommand command() {
        return new ConsumerSmsLoginCommand(
                "challenge-1",
                "123456",
                "consumer-app",
                "minipay://callback",
                "challenge",
                "S256",
                "device-1",
                "127.0.0.1",
                "test-agent",
                "request-1");
    }
}
