package org.jorion.trainingtool.training;

import org.jorion.trainingtool.type.Period;
import org.jorion.trainingtool.type.Provider;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit test for {@link Training}.
 */
class TrainingValidatorTest {

    @Test
    void testCheckBusinessErrors_OK() {

        Training t;
        List<String> errors;

        t = new Training();
        t.setTitle("title");
        t.setDescription("description");
        t.setProvider(Provider.INTERNAL);
        t.setUrl("http://www.example.org");
        t.setPrice("123456");
        t.setJustification("justification");
        errors = new TrainingValidator(t).validate();
        assertEquals(0, errors.size());

        t = new Training();
        t.setTitle("title");
        t.setDescription("description");
        t.setProvider(Provider.CEVORA);
        t.setUrl("http://www.example.org");
        t.setPrice("123456");
        t.setPeriod(Period.DAY);
        t.setTotalHour("0");

        LocalDate startDate = LocalDate.now();
        t.setStartDate(startDate);
        t.setEndDate(startDate);
        errors = new TrainingValidator(t).validate();
        assertEquals(0, errors.size());
    }

    @Test
    void testCheckBusinessError_NOK() {

        Training t;
        List<String> errors;

        t = new Training();
        t.setEnabledFrom(LocalDate.now().minusDays(1));
        t.setEnabledUntil(LocalDate.now().minusDays(2));
        t.setTitle(null);
        t.setDescription(null);
        t.setProvider(null);
        t.setUrl(null);
        t.setPrice("abcdef");
        t.setJustification(null);
        t.setStartDate(LocalDate.now().minusDays(1));
        t.setEndDate(LocalDate.now().minusDays(2));
        errors = new TrainingValidator(t).validate();
        assertEquals(7, errors.size());
    }
}
