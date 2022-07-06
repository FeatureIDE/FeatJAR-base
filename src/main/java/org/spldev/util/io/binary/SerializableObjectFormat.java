/* -----------------------------------------------------------------------------
 * Util Lib - Miscellaneous utility functions.
 * Copyright (C) 2021-2022  Sebastian Krieter
 * 
 * This file is part of Util Lib.
 * 
 * Util Lib is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 * 
 * Util Lib is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with Util Lib.  If not, see <https://www.gnu.org/licenses/>.
 * 
 * See <https://github.com/skrieter/utils> for further information.
 * -----------------------------------------------------------------------------
 */
package org.spldev.util.io.binary;

import java.io.*;

import org.spldev.util.data.Result;
import org.spldev.util.io.file.InputFileMapper;
import org.spldev.util.io.file.OutputFileMapper;
import org.spldev.util.io.format.*;
import org.spldev.util.logging.*;

/**
 * Reads / Writes a list of configuration.
 *
 * @author Sebastian Krieter
 */
public class SerializableObjectFormat<T> implements Format<T> {

	public static final String ID = SerializableObjectFormat.class.getCanonicalName();

	@Override
	public void write(T object, OutputFileMapper outputFileMapper) throws IOException {
		final OutputStream outputStream = outputFileMapper.getMainFile().getOutputStream();
		try (ObjectOutputStream oos = new ObjectOutputStream(outputStream)) {
			oos.writeObject(object);
			oos.flush();
		} catch (final Exception e) {
			Logger.logError(e);
		}

	}

	@Override
	public Result<T> parse(InputFileMapper inputFileMapper) {
		try (ObjectInputStream in = new ObjectInputStream(inputFileMapper.getMainFile().getInputStream())) {
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
