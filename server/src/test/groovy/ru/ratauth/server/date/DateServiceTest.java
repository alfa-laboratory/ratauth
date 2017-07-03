package ru.ratauth.server.date;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDateTime;

import static junit.framework.TestCase.assertEquals;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = DateServiceTestConfiguration.class)
public class DateServiceTest {

    @Autowired
    private DateService dateService;

    @Test
    public void testNow() throws Exception {
        LocalDateTime now = dateService.now();

        assertEquals(now, LocalDateTime.of(2000, 1, 1, 0, 0));
        assertEquals(now.plusSeconds(120), LocalDateTime.of(2000, 1, 1, 0, 2));
    }

}
