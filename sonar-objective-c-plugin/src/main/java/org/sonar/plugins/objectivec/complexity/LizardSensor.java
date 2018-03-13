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
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.sensor.Sensor;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.config.Settings;
import org.sonar.plugins.objectivec.ObjectiveCPlugin;
import org.sonar.plugins.objectivec.core.ObjectiveC;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * This sensor searches for the report generated from the tool Lizard
 * in order to save complexity metrics.
 *
 * @author Andres Gil Herrera
 * @since 28/05/15
 */
public class LizardSensor implements Sensor {

    private static final Logger LOGGER = LoggerFactory.getLogger(LizardSensor.class);

    private static final String NAME = "Lizard complexity sensor";

    public static final String REPORT_PATH_KEY = ObjectiveCPlugin.PROPERTY_PREFIX + ".lizard.report";
    public static final String DEFAULT_REPORT_PATH = "sonar-reports/lizard-report.xml";

    private final LizardReportParser parser = new LizardReportParser();

    private final Settings conf;
    private final FileSystem fileSystem;

    public LizardSensor(final FileSystem moduleFileSystem, final Settings config) {
        this.conf = config;
        this.fileSystem = moduleFileSystem;
    }

    @Override
    public void describe(@Nonnull SensorDescriptor descriptor) {
        descriptor.name(NAME);
        descriptor.onlyOnLanguage(ObjectiveC.KEY);
    }

    @Override
    public void execute(@Nonnull SensorContext context) {
        final String projectBaseDir = fileSystem.baseDir().getPath();
        Map<String, List<LizardMeasure<Integer>>> measures = parseReportsIn(projectBaseDir, parser);
        if (measures.isEmpty()) {
            return;
        }

        LOGGER.info("Saving results of complexity analysis");
        new LizardMeasurePersistor(context, fileSystem)
                .saveMeasures(measures);
    }

    /**
     *
     * @param baseDir base directory of the project to search the report
     * @param parser LizardReportParser to parse the report
     * @return Map containing as key the name of the file and as value a list containing the measures for that file
     */
    private <T extends Serializable> Map<String, List<LizardMeasure<T>>> parseReportsIn(final String baseDir, LizardReportParser parser) {
        final String reportFileName = buildReportPath(baseDir);
        final File reportFile = new File(reportFileName);
        if (reportFile.exists() && reportFile.isFile()) {
            LOGGER.info("Processing complexity report");
            return parser.parseReport(new File(reportFileName));
        }

        LOGGER.warn("No complexity report is available for parsing");
        return Collections.emptyMap();
    }

    /**
     * Build path for the report file using the {@code basePath} as prefix
     * @param basePath Base path for the project.
     * @return the default report path or the one specified in the sonar-project.properties
     */
    String buildReportPath(String basePath) {
        String reportPath = conf.getString(REPORT_PATH_KEY);

        if (reportPath == null) {
            LOGGER.debug("No value specified for \"" + REPORT_PATH_KEY + "\" using default path");
            reportPath = DEFAULT_REPORT_PATH;
        }

        return String.format("%s/%s",basePath, reportPath);
    }
}