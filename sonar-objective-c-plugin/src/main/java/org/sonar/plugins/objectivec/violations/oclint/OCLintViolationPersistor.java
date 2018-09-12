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

import org.sonar.api.batch.fs.FilePredicate;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.issue.NewIssue;
import org.sonar.api.batch.sensor.issue.NewIssueLocation;
import org.sonar.api.rule.RuleKey;

import javax.annotation.Nonnull;
import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

final class OCLintViolationPersistor {
    private final SensorContext context;
    private final FileSystem fileSystem;

    private OCLintViolationPersistor(@Nonnull final SensorContext context, @Nonnull final FileSystem fileSystem) {
        this.context = context;
        this.fileSystem = fileSystem;
    }

    @Nonnull
    static OCLintViolationPersistor create(@Nonnull final SensorContext context) {
        return new OCLintViolationPersistor(context, context.fileSystem());
    }

    void saveViolations(@Nonnull List<Violation> violations) {
        violations.stream()
                .collect(Collectors.groupingBy(Violation::getPath))
                .forEach(this::saveViolationsGroupedByFile);
    }

    private void saveViolationsGroupedByFile(@Nonnull String absoluteFilePath, @Nonnull List<Violation> violations) {
        Optional<InputFile> value = buildInputFile(absoluteFilePath);
        value.ifPresent(inputFile -> {
            for (Violation violation : violations) {
                RuleKey rule = RuleKey.of(OCLintRulesDefinition.REPOSITORY_KEY, violation.getRule());
                NewIssue newIssue = context.newIssue().forRule(rule);

                NewIssueLocation location = newIssue.newLocation()
                        .on(inputFile)
                        .at(inputFile.selectLine(violation.getStartLine()))
                        .message(violation.getMessage());

                newIssue.at(location)
                        .save();
            }
        });
    }

    @Nonnull
    private Optional<InputFile> buildInputFile(@Nonnull String absoluteFilePath) {
        File file = new File(absoluteFilePath);
        FilePredicate predicate = fileSystem.predicates().hasAbsolutePath(file.getAbsolutePath());

        return Optional.ofNullable(fileSystem.inputFile(predicate));
    }
}
