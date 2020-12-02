package org.mockserver.serialization.model;

import org.junit.Test;
import org.mockserver.model.Delay;

import java.util.concurrent.TimeUnit;

import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

/**
 * @author jamesdbloom
 */
public class DelayDTOTest {

    @Test
    public void shouldReturnValuesSetInConstructor() {
        // when
        DelayDTO delay = new DelayDTO(new Delay(TimeUnit.DAYS, 5));

        // then
        assertThat(delay.getTimeUnit(), is(TimeUnit.DAYS));
        assertThat(delay.getValue(), is(5L));
    }

    @Test
    public void shouldReturnValuesSetInSetter() {
        // when
        DelayDTO delay = new DelayDTO();
        delay.setTimeUnit(TimeUnit.DAYS);
        delay.setValue(5);

        // then
        assertThat(delay.getTimeUnit(), is(TimeUnit.DAYS));
        assertThat(delay.getValue(), is(5L));
    }

    @Test
    public void shouldHandleNullInput() {
        // when
        DelayDTO delay = new DelayDTO(null);

        // then
        assertThat(delay.getTimeUnit(), is(nullValue()));
        assertThat(delay.getValue(), is(0L));
    }
}
