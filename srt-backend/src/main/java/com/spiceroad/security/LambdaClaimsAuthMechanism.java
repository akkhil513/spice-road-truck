package com.spiceroad.security;

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import io.quarkus.security.identity.IdentityProviderManager;
import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.security.identity.request.AuthenticationRequest;
import io.quarkus.security.runtime.QuarkusPrincipal;
import io.quarkus.security.runtime.QuarkusSecurityIdentity;
import io.quarkus.vertx.http.runtime.QuarkusHttpHeaders;
import io.quarkus.vertx.http.runtime.security.ChallengeData;
import io.quarkus.vertx.http.runtime.security.HttpAuthenticationMechanism;
import io.quarkus.vertx.http.runtime.security.HttpCredentialTransport;
import io.smallrye.mutiny.Uni;
import io.vertx.ext.web.RoutingContext;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Builds the SecurityIdentity from the claims API Gateway's JWT authorizer verified.
 * Only claims inside the Lambda event are trusted; request headers are never used.
 */
@ApplicationScoped
public class LambdaClaimsAuthMechanism implements HttpAuthenticationMechanism {

    private static final String GROUPS_CLAIM = "cognito:groups";
    private static final Pattern GROUP = Pattern.compile("[^\\[\\],\\s]+");

    @Override
    public Uni<SecurityIdentity> authenticate(RoutingContext context, IdentityProviderManager idp) {
        if (!(context.request().headers() instanceof QuarkusHttpHeaders headers)) {
            return Uni.createFrom().nullItem();
        }
        Object raw = headers.getContextObjects().get(APIGatewayV2HTTPEvent.class);
        if (!(raw instanceof APIGatewayV2HTTPEvent event)
                || event.getRequestContext() == null
                || event.getRequestContext().getAuthorizer() == null
                || event.getRequestContext().getAuthorizer().getJwt() == null
                || event.getRequestContext().getAuthorizer().getJwt().getClaims() == null) {
            return Uni.createFrom().nullItem();
        }
        Map<String, String> claims = event.getRequestContext().getAuthorizer().getJwt().getClaims();
        String user = claims.getOrDefault("cognito:username", claims.get("sub"));
        if (user == null) {
            return Uni.createFrom().nullItem();
        }
        QuarkusSecurityIdentity.Builder builder = QuarkusSecurityIdentity.builder()
                .setPrincipal(new QuarkusPrincipal(user));
        String groups = claims.get(GROUPS_CLAIM);
        if (groups != null) {
            Matcher m = GROUP.matcher(groups);
            while (m.find()) {
                builder.addRole(m.group());
            }
        }
        return Uni.createFrom().item(builder.build());
    }

    @Override
    public Uni<ChallengeData> getChallenge(RoutingContext context) {
        return Uni.createFrom().item(new ChallengeData(401, null, null));
    }

    @Override
    public Set<Class<? extends AuthenticationRequest>> getCredentialTypes() {
        return Set.of();
    }

    @Override
    public HttpCredentialTransport getCredentialTransport() {
        return null;
    }
}
