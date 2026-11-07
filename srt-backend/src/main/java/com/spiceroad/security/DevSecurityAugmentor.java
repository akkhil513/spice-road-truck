package com.spiceroad.security;

import io.quarkus.arc.profile.IfBuildProfile;
import io.quarkus.security.identity.AuthenticationRequestContext;
import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.security.identity.SecurityIdentityAugmentor;
import io.quarkus.security.runtime.QuarkusSecurityIdentity;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;

import static com.spiceroad.config.RoleConstants.*;

@ApplicationScoped
@IfBuildProfile("dev")
public class DevSecurityAugmentor implements SecurityIdentityAugmentor {

    @Override
    public Uni<SecurityIdentity> augment(SecurityIdentity identity,
                                         AuthenticationRequestContext ctx) {
        return Uni.createFrom().item(
                QuarkusSecurityIdentity.builder(identity)
                        .addRole(ADMIN)
                        .addRole(MANAGER)
                        .addRole(STAFF)
                        .build()
        );
    }
}