/*
 * Copyright (C) 2023 Sebastian Krieter, Elias Kuiter
 *
 * This file is part of FeatJAR-base.
 *
 * base is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3.0 of the License,
 * or (at your option) any later version.
 *
 * base is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with base. If not, see <https://www.gnu.org/licenses/>.
 *
 * See <https://github.com/FeatureIDE/FeatJAR-base> for further information.
 */
package de.featjar.base.cli;

import static org.junit.jupiter.api.Assertions.*;

import de.featjar.base.data.Result;
import de.featjar.base.log.Log;
import org.junit.jupiter.api.Test;

class ArgumentParserTest {

    OptionList parser(String... args) {
        return new OptionList(args);
    }

    @Test
    void parsePositionalArguments() {
        assertTrue(parser().getCommands().isEmpty());
        //        assertEquals("arg", parser("arg").commandNameRegex);
        //        assertEquals("arg1", parser("arg1", "arg2").commandNameRegex);
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
