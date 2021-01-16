package org.jorion.trainingtool.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.ldap.authentication.BindAuthenticator;
import org.springframework.security.ldap.authentication.LdapAuthenticationProvider;
import org.springframework.security.ldap.authentication.SpringSecurityAuthenticationSource;
import org.springframework.security.ldap.userdetails.LdapUserDetails;

import org.jorion.trainingtool.config.ldap.CustomLdapAuthoritiesPopulator;
import org.jorion.trainingtool.types.Role;

/**
 * Security configuration class. Extending {@link WebSecurityConfigurerAdapter} allows it to override the
 * {@code configure(...)} methods.
 */
@Configuration
@EnableWebSecurity
// @EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter
{
    // --- Constants ---
    private static final Logger LOG = LoggerFactory.getLogger(SecurityConfig.class);

    // --- Variables ---
    @Value("${app.users.ldap:true}")
    private boolean usersLdap;

    @Value("${app.users.inmemory:false}")
    private boolean usersInMem;

    @Value("${ldap.url}")
    private String ldapUrl;

    @Value("${ldap.basedn}")
    private String ldapBaseDn;

    @Autowired
    private CustomLdapAuthoritiesPopulator ldapAuthoritiesPopulator;

    // --- Methods ---
    /**
     * Configure the authorization.
     */
    @Override
    protected void configure(HttpSecurity http)
            throws Exception
    {
        // necessary to display the H2 console
        http.headers().frameOptions().sameOrigin();
        http.csrf().disable();

        // @formatter:off

        // enable form login
        http.formLogin()
            .loginPage("/login")
            .permitAll();

        http.httpBasic();
        // http.httpBasic().authenticationEntryPoint(authenticationEntryPoint);

        // 'mvcMatchers' is safer (more restrictive) than 'antMatchers'
        http.authorizeRequests()
            // everyone
            .mvcMatchers("/error").permitAll()
            .mvcMatchers("/done").permitAll()
            .mvcMatchers("/accessDenied").permitAll()
            .mvcMatchers("/styles/**").permitAll()
            .mvcMatchers("/img/**").permitAll()
            .mvcMatchers("/webjars/**").permitAll()
            .mvcMatchers("/favicon.ico").permitAll()
            // REST
            .mvcMatchers("/swagger-ui/**").hasAnyRole(Role.getSupervisors())
            .mvcMatchers("/v2/api-docs").hasAnyRole(Role.getSupervisors())
            .mvcMatchers("/v3/api-docs").hasAnyRole(Role.getSupervisors())
            .mvcMatchers("/REST/v1/**").authenticated()
            // everyone but members
            .mvcMatchers("/report/**").hasAnyRole(Role.getSupervisors())
            .mvcMatchers("/exportReport/**").hasAnyRole(Role.getSupervisors())
            .mvcMatchers("/selectMember**").hasAnyRole(Role.getSupervisors())
            .mvcMatchers("/saveRegistrations").hasAnyRole(Role.getSupervisors())
            .mvcMatchers("/actuator/**").hasAnyRole(Role.getSupervisors())
            // admin only
            .mvcMatchers("/h2-console/**").hasRole(Role.ADMIN.name())
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

        // @formatter:on
    }

    /**
     * Configure the authentication.
     */
    @Override
    protected void configure(AuthenticationManagerBuilder auth)
            throws Exception
    {
        if (usersLdap) {
            LOG.debug("LDAP enabled");
            auth.authenticationProvider(authenticationProvider());
            auth.eraseCredentials(false);
        }

        // @formatter:off
        // Additional authentication providers
        // For testing purposes only
        if (usersInMem) {
            LOG.debug("LDAP InMemory enabled");
            auth.inMemoryAuthentication()
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
                .withUser("john")
                    .password("{noop}john")
                    .roles(Role.MEMBER.toString());
        }
        // @formatter:on

        // For REST calls
        auth.inMemoryAuthentication().withUser("restsystem").password("{noop}restsystem").roles(Role.MEMBER.toString());
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
    public AuthenticationSource authenticationSource()
    {
        // we need to override the default implementation to support both LDAP and InMem authentication
        return new SpringSecurityAuthenticationSource() {

            @Override
            public String getPrincipal()
            {
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                Object principal = authentication.getPrincipal();
                String dn = null;
                if (principal instanceof LdapUserDetails) {
                    dn = super.getPrincipal();
                }
                else if (principal instanceof User) {
                    User user = (User) principal;
                    dn = user.getUsername();
                }
                return dn;
            }
        };
    }

    /**
     * Alternate authentication source useful for some tests. Here the DN and password are hard-coded.
     */
    @Bean
    public AuthenticationSource authenticationSourceAlt()
    {
        final String username = "username";
        final String password = "xxxxxxxx";
        final String baseDn = this.ldapBaseDn;

        return new SpringSecurityAuthenticationSource() {
            @Override
            public String getPrincipal()
            {
                LOG.warn("Use of the alternate authentication source. Use it only locally for testing purposes.");
                String dn = "CN=" + username + "," + baseDn;
                return dn;
            }

            @Override
            public String getCredentials()
            {
                return password;
            }
        };
    }

    /**
     * Create an LDAP Context Source.
     */
    @Bean
    public LdapContextSource contextSource()
    {
        LdapContextSource ctx = new LdapContextSource();
        ctx.setUrl(ldapUrl);
        ctx.setBase(ldapBaseDn);
        ctx.setAuthenticationSource(authenticationSource());
        ctx.afterPropertiesSet();
        LOG.debug("ContextSource URL {}, baseDn [{}]", new Object[] { ctx.getUrls(), ctx.getBaseLdapName() });
        return ctx;
    }

    @Bean
    public LdapTemplate ldapTemplate()
    {
        return new LdapTemplate(contextSource());
    }

    /**
     * Custom LDAP Authentication Provider.
     */
    @Bean
    public LdapAuthenticationProvider authenticationProvider()
    {
        BindAuthenticator authenticator = new BindAuthenticator(contextSource());
        authenticator.setUserDnPatterns(new String[] { "cn={0}" });
        LdapAuthenticationProvider provider = new LdapAuthenticationProvider(authenticator, this.ldapAuthoritiesPopulator);
        return provider;
    }

}
