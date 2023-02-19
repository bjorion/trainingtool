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
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.ldap.authentication.BindAuthenticator;
import org.springframework.security.ldap.authentication.LdapAuthenticationProvider;
import org.springframework.security.ldap.authentication.SpringSecurityAuthenticationSource;
import org.springframework.security.ldap.userdetails.LdapAuthoritiesPopulator;
import org.springframework.security.ldap.userdetails.LdapUserDetails;
import org.springframework.security.web.SecurityFilterChain;

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

    @Value("${ldap.basedn}")
    private String ldapBaseDn;

    @Autowired
    private CustomLdapAuthoritiesPopulator ldapAuthoritiesPopulator;

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http)
            throws Exception {

        http
                // necessary to display the H2 console
                .headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin))
                .csrf(csrf -> csrf.disable())
                // enable form login
                .formLogin(form -> form.loginPage("/login").permitAll());

        http.authorizeRequests()
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
                .anyRequest().authenticated()
                .and()
                
                // error page if access is denied
                .exceptionHandling().accessDeniedPage("/access-denied");

        // enable default logout mechanism
        http.logout()
                // define logout url
                .logoutUrl("/logout")
                // define redirect url after logout
                .logoutSuccessUrl("/done")
                // invalidate http session after logout (true by default)
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID");

        if (usersLdap) {
            log.debug("LDAP enabled");
            http.authenticationProvider(authenticationProvider());
            // TODO solve this
            // http.eraseCredentials(false);
        }

        // Additional authentication providers
        // For testing purposes only
        if (usersInMem) {
            log.debug("LDAP InMemory enabled");
            // TODO solve this
            /*
            http.inMemoryAuthentication()
                    .withUser("admin")
                    .password("{noop}admin")
                    .roles(Role.MEMBER.toString(), Role.ADMIN.toString())
                    .and()
                    .withUser("hr")
                    .password("{noop}hr")
                    .roles(Role.MEMBER.toString(), Role.MANAGER.toString(), Role.HR.toString())
                    .and()
                    .withUser("training")
                    .password("{noop}training")
                    .roles(Role.MEMBER.toString(), Role.TRAINING.toString())
                    .and()
                    .withUser("manager")
                    .password("{noop}manager")
                    .roles(Role.MEMBER.toString(), Role.MANAGER.toString())
                    .and()
                    .withUser("john.doe")
                    .password("{noop}john")
                    .roles(Role.MEMBER.toString())
            ;
            */
        }

        return http.build();
    }

    /**
     * Define an Authentication Source that retrieves the credentials for the LDAP operations from the Principal. This
     * suppresses the need to define a System username/password and make sure each LDAP operations is executed with the
     * rights of the Principal. For this to work, the flag {@code eraseCredentials} needs to be set to false in the
     * {@link AuthenticationManagerBuilder}.
     * <p>
     * See https://docs.spring.io/spring-ldap/docs/2.3.3.RELEASE/reference/#configuration
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
                } else if (principal instanceof User) {
                    User user = (User) principal;
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
                String dn = "CN=" + username + "," + baseDn;
                return dn;
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
        log.debug("ContextSource URL {}, baseDn [{}]", new Object[]{ctx.getUrls(), ctx.getBaseLdapName()});
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
        LdapAuthenticationProvider provider = new LdapAuthenticationProvider(authenticator, this.ldapAuthoritiesPopulator);
        return provider;
    }

}
