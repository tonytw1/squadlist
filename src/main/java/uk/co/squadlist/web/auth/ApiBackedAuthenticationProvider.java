package uk.co.squadlist.web.auth;

import java.util.Collection;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.AbstractUserDetailsAuthenticationProvider;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import uk.co.squadlist.web.api.InstanceSpecificApiClient;
import uk.co.squadlist.web.model.Member;

import com.google.common.collect.Lists;

@Component("authenticationProvider")
public class ApiBackedAuthenticationProvider extends AbstractUserDetailsAuthenticationProvider {

    private final static Logger log = Logger.getLogger(ApiBackedAuthenticationProvider.class);

    private static final String INVALID_USERNAME_OR_PASSWORD = "Invalid username or password";

    private final InstanceSpecificApiClient api;

    @Autowired
    public ApiBackedAuthenticationProvider(InstanceSpecificApiClient api) {
        this.api = api;
    }

    @Override
    protected void additionalAuthenticationChecks(UserDetails userDetails, UsernamePasswordAuthenticationToken authToken) throws AuthenticationException {
    }

    @Override
    protected UserDetails retrieveUser(String username, UsernamePasswordAuthenticationToken authToken) throws AuthenticationException {
        log.info("User details requested for: " + username);

        final String password = authToken.getCredentials().toString();
        log.info("Attempting to auth user: " + username);

        final String authenticatedUsersAccessToken = api.auth(username, password);
        if (authenticatedUsersAccessToken != null) {
            Member authenticatedMember = api.verify(authenticatedUsersAccessToken);

            if (authenticatedMember != null) {
                log.info("Auth successful for user: " + username);

                Collection<SimpleGrantedAuthority> authorities = Lists.newArrayList();
                authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
                User user = new User(authenticatedMember.getId(), password, authorities);
                return user;
            }
        }

        log.info("Auth attempt unsuccessful for user: " + username);
        throw new BadCredentialsException(INVALID_USERNAME_OR_PASSWORD);
    }

}
