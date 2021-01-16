package org.jorion.trainingtool.repository;

import static org.junit.Assert.assertEquals;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import org.jorion.trainingtool.entities.User;
import org.jorion.trainingtool.repositories.IUserRepository;

/**
 * Unit test for {@link IUserRepository}.
 */
@RunWith(SpringRunner.class)
@DataJpaTest
@ActiveProfiles("local")
public class UserRepositoryIntegrationTest
{
    // --- Constants ---
    private static final Logger LOG = LoggerFactory.getLogger(UserRepositoryIntegrationTest.class);

    // --- Variables ---
    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private IUserRepository userRepository;

    // --- Methods ---
    @Test
    @Ignore
    public void testFindUserByUsername()
    {
        // given
        User user = new User("billy");
        user.setPnr("pnr-billy");
        user.setLastname("lastname");
        user.setFirstname("firstname");
        entityManager.persist(user);
        entityManager.flush();

        // when
        User found = userRepository.findUserByUsername("billy").orElseGet(() -> null);
        LOG.debug(found.toString());

        // then
        assertEquals("billy", user.getUsername());
    }
}
