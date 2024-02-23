/*
 * Copyright (C) 2024 FeatJAR-Development-Team
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
import java.util.List;
import org.junit.jupiter.api.Test;

class ArgumentParserTest {

    OptionList parser(String... args) {
        OptionList optionList = new OptionList(args);
        optionList.parseArguments();
        return optionList;
    }

    @Test
    void parseCommand() {
        // todo: needs mocking of extension points
    }

    @Test
    void getVerbosity() {
        // assertEquals(Log.Verbosity.DEBUG, parser("arg", "--log-info").getVerbosity()); todo: mock System.exit
        assertEquals(
                Log.Verbosity.DEBUG,
                parser("arg", "--log-info", "debug")
                        .parseArguments()
                        .get(OptionList.LOG_INFO_OPTION)
                        .get(0));
    }

    @Test
    void parseOption1() {
        Option<Integer> option = new Option<>("x", Integer::valueOf);
        OptionList parser = parser("arg").addOptions(List.of(option)).parseArguments();
        assertEquals(Result.empty(), parser.getResult(option));
    }

    @Test
    void parseOption2() {
        Option<Integer> option = new Option<>("x", Integer::valueOf);
        OptionList parser =
                parser("arg", "--x", "42").addOptions(List.of(option)).parseArguments();
        assertEquals(Result.of(42), parser.getResult(option));
    }
}
