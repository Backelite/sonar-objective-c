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

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.sonar.api.batch.fs.internal.DefaultFileSystem;
import org.sonar.api.batch.sensor.internal.DefaultSensorDescriptor;
import org.sonar.api.config.MapSettings;
import org.sonar.api.config.Settings;
import org.sonar.plugins.objectivec.core.ObjectiveC;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(JUnit4.class)
public class OCLintSensorTest {
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    private OCLintSensor sensor;

    @Before
    public void prepare() throws IOException {
        File baseDirectory = temporaryFolder.newFolder();

        DefaultFileSystem fileSystem = new DefaultFileSystem(baseDirectory.toPath());
        Settings settings = new MapSettings();

        sensor = new OCLintSensor(fileSystem, settings);
    }

    @Test
    public void describe() {
        DefaultSensorDescriptor descriptor = new DefaultSensorDescriptor();

        sensor.describe(descriptor);

        assertEquals("OCLint violation sensor", descriptor.name());
        assertTrue(descriptor.languages().contains(ObjectiveC.KEY));
    }
}
