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
package de.featjar.base.env;

import de.featjar.base.FeatJAR;
import de.featjar.base.data.Result;
import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.util.Scanner;

/**
 * An object that can be displayed in a web browser.
 * Can be passed an argument to influence what is displayed.
 *
 * @param <T> the type of the argument
 * @author Elias Kuiter
 */
public interface IBrowsable<T> {
    static void browse(String urlString) {
        browse(URI.create(urlString));
    }

    static void browse(URI uri) {
        try {
            Desktop.getDesktop().browse(uri);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    Result<URI> getBrowseURI(T argument);

    default void browse(T argument) {
        Result<URI> browseURI = getBrowseURI(argument);
        if (browseURI.isEmpty()) {
            FeatJAR.log().error("cannot display " + this + " in browser");
            return;
        }
        FeatJAR.log().info("displaying " + this + " in browser");
        browse(browseURI.get());
    }

    default void debugBrowse(T argument) {
        browse(argument);
        FeatJAR.log().info("press return to continue");
        new Scanner(System.in).nextLine();
    }
}
