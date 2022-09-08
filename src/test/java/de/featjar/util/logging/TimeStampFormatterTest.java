package de.featjar.util.logging;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TimeStampFormatterTest {
    TimeStampFormatter timeStampFormatter;

    @BeforeEach
    public void setup() {
        timeStampFormatter = Mockito.spy(TimeStampFormatter.class);
        doReturn(Instant.ofEpochSecond(1659344400)).when(timeStampFormatter).getInstant();
    }

    @Test
    void mockInstant() {
        assertEquals("2022-08-01T09:00:00Z", timeStampFormatter.getInstant().toString());
    }

    @Test
    void getDefaultPrefix() {
        assertEquals("01/08/2022, 11:00 ", timeStampFormatter.getPrefix());
    }

    @Test
    void getCustomPrefix() {
        timeStampFormatter.setFormatter(DateTimeFormatter
                .ofPattern("yyyy/MM/dd-HH:mm:ss")
                .withZone(ZoneId.systemDefault()));
        assertEquals("2022/08/01-11:00:00 ", timeStampFormatter.getPrefix());
    }
}