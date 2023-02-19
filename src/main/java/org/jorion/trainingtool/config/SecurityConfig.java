package org.jorion.trainingtool.config;

import lombok.extern.slf4j.Slf4j;
import org.jorion.trainingtool.config.ldap.CustomLdapAuthoritiesPopulator;
import org.jorion.trainingtool.type.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ldap.core.AuthenticationSource;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.ldap.authentication.BindAuthenticator;
import org.springframework.security.ldap.authentication.LdapAuthenticationProvider;
import org.springframework.security.ldap.authentication.SpringSecurityAuthenticationSource;
import org.springframework.security.ldap.userdetails.LdapAuthoritiesPopulator;
import org.springframework.security.ldap.userdetails.LdapUserDetails;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

import static org.jorion.trainingtool.type.Role.*;

/**
 * Security configuration class.
 */
@Slf4j
@Configuration
@EnableWebSecurity
// @EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true)
public class SecurityConfig {

    @Value("${app.users.ldap:true}")
    private boolean usersLdap;

    @Value("${app.users.inMemory:false}")
    private boolean usersInMem;

    @Value("${ldap.url}")
    private String ldapUrl;

    @Value("${ldap.baseDn}")
    private String ldapBaseDn;

    @Autowired
    private CustomLdapAuthoritiesPopulator ldapAuthoritiesPopulator;

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http)
            throws Exception {

        http
                // necessary to display the H2 console
                .headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin))
                .csrf(AbstractHttpConfigurer::disable)
                // enable form login
                .formLogin(form -> form.loginPage("/login").permitAll());

        http.authorizeHttpRequests(req -> req
                        // everyone
                        .requestMatchers("/error").permitAll()
                        .requestMatchers("/done").permitAll()
                        .requestMatchers("/access-denied").permitAll()
                        .requestMatchers("/styles/**").permitAll()
                        .requestMatchers("/img/**").permitAll()
                        .requestMatchers("/webjars/**").permitAll()
                        .requestMatchers("/favicon.ico").permitAll()

                        // REST
                        .requestMatchers("/swagger-ui/**").hasAnyRole(Role.getSupervisors())
                        .requestMatchers("/v2/api-docs").hasAnyRole(Role.getSupervisors())
                        .requestMatchers("/v3/api-docs").hasAnyRole(Role.getSupervisors())
                        .requestMatchers("/REST/v1/**").hasAnyRole(Role.getSupervisors())

                        // managers and above
                        .requestMatchers("/report/**").hasAnyRole(Role.getSupervisors())
                        .requestMatchers("/export-report/**").hasAnyRole(Role.getSupervisors())
                        .requestMatchers("/select-member**").hasAnyRole(Role.getSupervisors())
                        .requestMatchers("/save-registrations").hasAnyRole(Role.getSupervisors())
                        .requestMatchers("/actuator/**").hasAnyRole(Role.getSupervisors())

                        // trainings
                        .requestMatchers("/trainings/**").hasAnyRole(Role.getOffices())
                        .requestMatchers("/edit-training/**").hasAnyRole(Role.getOffices())
                        .requestMatchers("/save-training/**").hasAnyRole(Role.getOffices())
                        .requestMatchers("/delete-training/**").hasAnyRole(Role.getOffices())

                        // admin only
                        .requestMatchers("/h2-console/**").hasAnyRole(Role.getOffices())

                        // logged users
                        .anyRequest().authenticated())

                // error page if access is denied
                .exceptionHandling(e -> e.accessDeniedPage("/access-denied"));

        // enable default logout mechanism
        http.logout(out -> out.logoutUrl("/logout")
                .logoutSuccessUrl("/done")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID"));

        if (usersLdap) {
            log.debug("LDAP enabled");
            http.authenticationProvider(authenticationProvider());
            // TODO solve this
            // http.eraseCredentials(false);
        }

        if (usersInMem) {
            log.debug("InMemory enabled");
            http.userDetailsService(inMemoryUsers());
        }

        return http.build();
    }

    @Bean
    public UserDetailsService inMemoryUsers() {

        // TODO check this
        log.debug("Add InMemory users");

        @SuppressWarnings("deprecation")
        User.UserBuilder userBuilder = User.withDefaultPasswordEncoder();

        UserDetails admin = userBuilder
                .username("admin")
                .password("{noop}admin")
                .roles(MEMBER.toString(), ADMIN.toString())
                .build();
        UserDetails hr = userBuilder
                .username("hr")
                .password("{noop}hr")
                .roles(MEMBER.toString(), MANAGER.toString(), Role.HR.toString())
                .build();
        UserDetails training = userBuilder
                .username("hr")
                .password("{noop}training")
                .roles(MEMBER.toString(), TRAINING.toString())
                .build();
        UserDetails manager = userBuilder
                .username("hr")
                .password("{noop}manager")
                .roles(MEMBER.toString(), MANAGER.toString())
                .build();
        UserDetails john = userBuilder
                .username("john.doe")
                .password("{noop}john")
                .roles(MEMBER.toString())
                .build();

        return new InMemoryUserDetailsManager(admin, hr, training, manager, john);
    }

    /**
     * Define an Authentication Source that retrieves the credentials for the LDAP operations from the Principal. This
     * suppresses the need to define a System username/password and make sure each LDAP operations is executed with the
     * rights of the Principal. For this to work, the flag {@code eraseCredentials} needs to be set to false in the
     * {@link AuthenticationManagerBuilder}.
     * <p>
     * See <a href="https://docs.spring.io/spring-ldap/docs/2.3.3.RELEASE/reference/#configuration">...</a>
     */
    @Bean
    public AuthenticationSource authenticationSource() {

        // we need to override the default implementation to support both LDAP and InMem authentication
        return new SpringSecurityAuthenticationSource() {

            @Override
            public String getPrincipal() {

                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                Object principal = authentication.getPrincipal();
                String dn;
                if (principal instanceof LdapUserDetails) {
                    dn = super.getPrincipal();
                } else if (principal instanceof User user) {
                    dn = user.getUsername();
                } else {
                    dn = null;
                }
                return dn;
            }
        };
    }

    /**
     * Alternate authentication source useful for some tests. Here the DN and password are hard-coded.
     */
    @Bean
    @Deprecated
    public AuthenticationSource authenticationSourceAlt() {

        final String username = "username";
        final String password = "xxxxxxxx";
        final String baseDn = this.ldapBaseDn;

        return new SpringSecurityAuthenticationSource() {

            @Override
            public String getPrincipal() {

                log.warn("Use of the alternate authentication source. Use it only locally for testing purposes.");
                return "CN=" + username + "," + baseDn;
            }

            @Override
            public String getCredentials() {
                return password;
            }
        };
    }

    /**
     * Create an LDAP Context Source.
     */
    @Bean
    public LdapContextSource contextSource() {

        LdapContextSource ctx = new LdapContextSource();
        ctx.setUrl(ldapUrl);
        ctx.setBase(ldapBaseDn);
        ctx.setAuthenticationSource(authenticationSource());
        ctx.afterPropertiesSet();
        log.debug("ContextSource URL {}, baseDn [{}]", ctx.getUrls(), ctx.getBaseLdapName());
        return ctx;
    }

    @Bean
    public LdapTemplate ldapTemplate() {

        return new LdapTemplate(contextSource());
    }

    /**
     * Return an instance of {@code LdapAuthenticationProvider} with an adapted {@link BindAuthenticator} and a custom
     * {@link LdapAuthoritiesPopulator}.
     */
    @Bean
    public LdapAuthenticationProvider authenticationProvider() {

        BindAuthenticator authenticator = new BindAuthenticator(contextSource());
        authenticator.setUserDnPatterns(new String[]{"cn={0}"});
        return new LdapAuthenticationProvider(authenticator, this.ldapAuthoritiesPopulator);
    }

}
