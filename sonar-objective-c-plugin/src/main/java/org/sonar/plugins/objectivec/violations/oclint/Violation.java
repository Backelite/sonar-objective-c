/**
 * backelite-sonar-objective-c-plugin - Enables analysis of Objective-C projects into SonarQube.
 * Copyright © 2012 OCTO Technology, Backelite (${email})
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.sonar.plugins.objectivec.violations.oclint;

import javax.annotation.Nonnull;
import java.util.Objects;

final class Violation {
    private final String path;
    private final int startLine;
    private final String rule;
    private final String message;

    Violation(Builder builder) {
        this.path = builder.path;
        this.startLine = builder.startLine;
        this.rule = builder.rule;
        this.message = builder.message;
    }

    @Nonnull
    public String getPath() {
        return path;
    }

    public int getStartLine() {
        return startLine;
    }

    @Nonnull
    public String getRule() {
        return rule;
    }

    @Nonnull
    public String getMessage() {
        return message;
    }

    @Nonnull
    static Builder builder() {
        return new Builder();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof Violation)) {
            return false;
        }

        Violation violation = (Violation) o;
        return startLine == violation.startLine &&
                Objects.equals(path, violation.path) &&
                Objects.equals(rule, violation.rule) &&
                Objects.equals(message, violation.message);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                path,
                startLine,
                rule,
                message
        );
    }

    static class Builder {
        private String path = "";
        private int startLine;
        private String rule = "";
        private String message = "";

        private Builder() {
        }

        @Nonnull
        Builder setPath(@Nonnull String path) {
            this.path = path;
            return this;
        }

        @Nonnull
        Builder setStartLine(int startLine) {
            this.startLine = startLine;
            return this;
        }

        @Nonnull
        Builder setRule(@Nonnull String rule) {
            this.rule = rule;
            return this;
        }

        @Nonnull
        Builder setMessage(@Nonnull String message) {
            this.message = message;
            return this;
        }

        @Nonnull
        Violation build() {
            return new Violation(this);
        }
    }
}
