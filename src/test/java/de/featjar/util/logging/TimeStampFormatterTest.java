package de.featjar.util.logging;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TimeStampFormatterTest {
    private MockedStatic<Clock> clockMock;

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @BeforeEach
    public void setup() {
        Clock spyClock = spy(Clock.class);
        clockMock = mockStatic(Clock.class);
        clockMock.when(Clock::systemUTC).thenReturn(spyClock);
        when(spyClock.instant()).thenReturn(Instant.ofEpochSecond(1659344400));
    }

    @AfterEach
    public void destroy() {
        clockMock.close();
    }

    @Test
    void mockInstant() {
        assertEquals("2022-08-01T09:00:00Z", Instant.now().toString());
    }

    @Test
    void getDefaultPrefix() {
        assertEquals("01/08/2022, 11:00 ", new TimeStampFormatter().getPrefix());
    }

    @Test
    void getCustomPrefix() {
        TimeStampFormatter timeStampFormatter = new TimeStampFormatter();
        timeStampFormatter.setFormatter(DateTimeFormatter
                .ofPattern("yyyy/MM/dd-HH:mm:ss")
                .withZone(ZoneId.systemDefault()));
        assertEquals("2022/08/01-11:00:00 ", timeStampFormatter.getPrefix());
    }
}