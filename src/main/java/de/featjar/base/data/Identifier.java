/*
 * Copyright (C) 2022 Elias Kuiter
 *
 * This file is part of model.
 *
 * model is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3.0 of the License,
 * or (at your option) any later version.
 *
 * model is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with model. If not, see <https://www.gnu.org/licenses/>.
 *
 * See <https://github.com/FeatureIDE/FeatJAR-model> for further information.
 */
package de.featjar.base.data;

import java.util.Objects;

/**
 * Uniquely identifies an {@link Identifiable} object.
 * Implementors are responsible for guaranteeing uniqueness within a self-chosen scope (e.g., during program execution).
 *
 * @author Elias Kuiter
 */
public abstract class Identifier {
    protected final Factory factory;

    private Identifier(Factory factory) {
        Objects.requireNonNull(factory);
        this.factory = factory;
    }

    public Factory getFactory() {
        return factory;
    }

    public Identifier getNewIdentifier() {
        return factory.get();
    }

    @Override
    public String toString() {
        return String.valueOf(System.identityHashCode(this));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Identifier that = (Identifier) o;
        return toString().equals(that.toString());
    }

    @Override
    public int hashCode() {
        return Objects.hash(toString());
    }

    /**
     * Parses a string into an {@link Identifier}.
     */
    public abstract static class Factory implements de.featjar.base.data.Factory<Identifier> {
        public abstract Identifier parse(String identifierString);
    }

    /**
     * Identifies an object with a given number.
     */
    public static class Counter extends Identifier {
        protected final long counter;

        public Counter(long counter, Factory factory) {
            super(factory);
            this.counter = counter;
        }

        public long getCounter() {
            return counter;
        }

        @Override
        public String toString() {
            return String.valueOf(counter);
        }

        /**
         * Creates counter identifiers by incrementing a number.
         */
        public static class Factory extends Identifier.Factory {
            long counter = 0;

            @Override
            public Identifier get() {
                return new Counter(++counter, this);
            }

            @Override
            public Identifier parse(String identifierString) {
                return new Counter(Long.parseLong(identifierString), this);
            }
        }
    }

    /**
     * Identifies an object with a given {@link UUID}.
     */
    public static class UUID extends Identifier {
        protected final java.util.UUID uuid;

        public UUID(java.util.UUID uuid, Factory factory) {
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
         * Creates random {@link UUID} identifiers.
         */
        public static class Factory extends Identifier.Factory {

            @Override
            public Identifier get() {
                return new UUID(java.util.UUID.randomUUID(), this);
            }

            @Override
            public Identifier parse(String identifierString) {
                return new UUID(java.util.UUID.fromString(identifierString), this);
            }
        }
    }

    public static Identifier newCounter() {
        return new Counter.Factory().get();
    }

    public static Identifier newUUID() {
        return new UUID.Factory().get();
    }
}
