package de.featjar.base.cli;

import static org.junit.jupiter.api.Assertions.*;

import de.featjar.base.data.Result;
import de.featjar.base.log.Log;
import org.junit.jupiter.api.Test;

class ArgumentParserTest {

    ArgumentParser parser(String... args) {
        return new ArgumentParser(args);
    }

    @Test
    void parsePositionalArguments() {
        assertTrue(parser().getCommands().isEmpty());
        assertEquals("arg", parser("arg").commandNameRegex);
        assertEquals("arg1", parser("arg1", "arg2").commandNameRegex);
    }

    @Test
    void parseCommand() {
        // todo: needs mocking of extension points
    }

    @Test
    void getVerbosity() {
        // assertEquals(Log.Verbosity.DEBUG, parser("arg", "--verbosity").getVerbosity()); todo: mock System.exit
        assertEquals(Log.Verbosity.DEBUG, parser("arg", "--verbosity", "debug").getVerbosity());
    }

    @Test
    void isHelpOption() {
        assertFalse(parser("arg").hasHelpOption());
        assertTrue(parser("arg", "--help").hasHelpOption());
    }

    @Test
    void parseOption() {
        Option<Integer> option = new Option<>("--x", Result.mapReturnValue(Integer::valueOf));
        assertEquals(Result.empty(), option.parseFrom(parser("arg")));
        assertEquals(Result.of(42), option.parseFrom(parser("arg", "--x", "42")));
    }
}
