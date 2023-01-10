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
package de.featjar.base.data.identifier;

/**
 * Identifies an object with a given {@link UUIDIdentifier}.
 *
 * @author Elias Kuiter
 */
public class UUIDIdentifier extends AIdentifier {
    protected final java.util.UUID uuid;

    public UUIDIdentifier(java.util.UUID uuid, Factory factory) {
        super(factory);
        this.uuid = uuid;
    }

    public java.util.UUID getUUID() {
        return uuid;
    }

    @Override
    public String toString() {
        return uuid.toString();
    }

    /**
     * Creates random {@link UUIDIdentifier} identifiers.
     */
    public static class Factory implements IIdentifierFactory {

        @Override
        public AIdentifier get() {
            return new UUIDIdentifier(java.util.UUID.randomUUID(), this);
        }

        @Override
        public AIdentifier parse(String identifierString) {
            return new UUIDIdentifier(java.util.UUID.fromString(identifierString), this);
        }
    }
}
