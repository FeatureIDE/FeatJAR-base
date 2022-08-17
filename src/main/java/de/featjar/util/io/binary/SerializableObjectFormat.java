/*
 * Copyright (C) 2022 Sebastian Krieter, Elias Kuiter
 *
 * This file is part of util.
 *
 * util is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3.0 of the License,
 * or (at your option) any later version.
 *
 * util is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with util. If not, see <https://www.gnu.org/licenses/>.
 *
 * See <https://github.com/FeatureIDE/FeatJAR-util> for further information.
 */
package de.featjar.util.io.binary;

import de.featjar.util.data.Result;
import de.featjar.util.io.InputMapper;
import de.featjar.util.io.OutputMapper;
import de.featjar.util.io.format.Format;
import de.featjar.util.logging.Logger;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

/**
 * Reads / Writes a list of configuration.
 *
 * @author Sebastian Krieter
 */
public class SerializableObjectFormat<T> implements Format<T> {

    public static final String ID = SerializableObjectFormat.class.getCanonicalName();

    @Override
    public void write(T object, OutputMapper outputMapper) throws IOException {
        final OutputStream outputStream = outputMapper.get().getOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(outputStream)) {
            oos.writeObject(object);
            oos.flush();
        } catch (final Exception e) {
            Logger.logError(e);
        }
    }

    @Override
    public Result<T> parse(InputMapper inputMapper) {
        try (ObjectInputStream in = new ObjectInputStream(inputMapper.get().getInputStream())) {
            @SuppressWarnings("unchecked")
            final T readObject = (T) in.readObject();
            return Result.of(readObject);
        } catch (final Exception e) {
            Logger.logError(e);
            return Result.empty(e);
        }
    }

    @Override
    public String getFileExtension() {
        return "serialized";
    }

    @Override
    public SerializableObjectFormat<T> getInstance() {
        return this;
    }

    @Override
    public String getIdentifier() {
        return ID;
    }

    @Override
    public boolean supportsSerialize() {
        return true;
    }

    @Override
    public boolean supportsParse() {
        return true;
    }

    @Override
    public String getName() {
        return "SerializableObjectFormat";
    }
}
