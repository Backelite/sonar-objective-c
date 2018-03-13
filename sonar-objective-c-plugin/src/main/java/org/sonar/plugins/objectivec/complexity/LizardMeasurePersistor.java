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
package org.sonar.plugins.objectivec.complexity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.fs.FilePredicate;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.measure.Metric;
import org.sonar.api.batch.sensor.SensorContext;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * This class is used to save the measures created by the lizardReportParser in the sonar database
 *
 * @author Andres Gil Herrera
 * @since 28/05/15.
 */
final class LizardMeasurePersistor {

    private static final Logger LOGGER = LoggerFactory.getLogger(LizardMeasurePersistor.class);

    private final SensorContext sensorContext;
    private final FileSystem fileSystem;

    LizardMeasurePersistor(@Nonnull final SensorContext sensorContext, @Nonnull final FileSystem fileSystem) {
        this.sensorContext = sensorContext;
        this.fileSystem = fileSystem;
    }

    /**
     *
     * @param measures Map containing as key the name of the file and as value a list containing the measures for that file
     */
    <T extends Serializable> void saveMeasures(@Nonnull final Map<String, List<LizardMeasure<T>>> measures) {
        for (Map.Entry<String, List<LizardMeasure<T>>> entry : measures.entrySet()) {
            Optional<InputFile> value = buildInputFile(entry.getKey());
            if (value.isPresent()) {
                entry.getValue().forEach(measure -> saveMeasure(value.get(), measure));
                continue;
            }

            LOGGER.warn("File not included {}", entry.getKey());
        }
    }

    @Nonnull
    private Optional<InputFile> buildInputFile(@Nonnull String relativeFilePath) {
        File file = new File(fileSystem.baseDir(), relativeFilePath);
        FilePredicate predicate = fileSystem.predicates().hasAbsolutePath(file.getAbsolutePath());

        return Optional.ofNullable(fileSystem.inputFile(predicate));
    }

    private <T extends Serializable> void saveMeasure(@Nonnull InputFile inputFile, @Nonnull LizardMeasure<T> measure) {
        Metric<T> metric = measure.getMetric();

        try {
            LOGGER.debug("Save measure {} for file {}", metric.key(), inputFile.relativePath());
            sensorContext.<T>newMeasure()
                    .on(inputFile)
                    .forMetric(metric)
                    .withValue(measure.getValue())
                    .save();
        } catch (Exception e) {
            LOGGER.error(" Exception -> {} -> {}", inputFile.relativePath(), metric.key(), e);
        }
    }

}