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
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.annotation.Nonnull;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(JUnit4.class)
public class OCLintReportParserTest {
    private final Path resourcePath = Paths.get("src", "test", "resources", "oclint");

    private DocumentBuilder builder;
    private OCLintReportParser parser;

    @Before
    public void setUp() throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        builder = factory.newDocumentBuilder();

        parser = new OCLintReportParser();
    }

    @Test
    public void parse_withEmptyDocument() throws Exception {
        Path documentPath = Paths.get(resourcePath.toString(), "empty.xml");
        Document document = builder.parse(documentPath.toFile());

        List<Violation> actual = parser.parse(document);

        assertTrue(actual.isEmpty());
    }

    @Test
    public void parse_withSampleDocument() throws IOException, SAXException {
        Path documentPath = Paths.get(resourcePath.toString(), "oclint.xml");
        Document document = builder.parse(documentPath.toFile());
        List<Violation> expected = buildExpectedViolationsForSample();

        List<Violation> actual = parser.parse(document);

        assertEquals(expected, actual);
    }

    @Nonnull
    private List<Violation> buildExpectedViolationsForSample() {
        List<Violation> expected = new ArrayList<>();
        Violation violation;

        violation = Violation.builder()
                .setPath("RASqlite/RASqlite.m")
                .setStartLine(281)
                .setRule("deep nested block")
                .setMessage("Block depth of 6 exceeds limit of 5")
                .build();
        expected.add(violation);

        violation = Violation.builder()
                .setPath("RASqlite/RASqlite.m")
                .setStartLine(305)
                .setRule("ivar assignment outside accessors or init")
                .build();
        expected.add(violation);

        violation = Violation.builder()
                .setPath("RASqlite/RASqlite.m")
                .setStartLine(707)
                .setRule("unused method parameter")
                .setMessage("The parameter 'commit' is unused.")
                .build();
        expected.add(violation);

        return expected;
    }
}
