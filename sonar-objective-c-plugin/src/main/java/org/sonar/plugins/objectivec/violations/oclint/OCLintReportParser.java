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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.annotation.Nonnull;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

final class OCLintReportParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(OCLintReportParser.class);

    private static final String VIOLATION = "violation";
    private static final String PATH = "path";
    private static final String START_LINE = "startline";
    private static final String RULE = "rule";
    private static final String MESSAGE = "message";

    @Nonnull
    private static NodeList getViolationElements(@Nonnull Document document) {
        return document.getElementsByTagName(VIOLATION);
    }

    @Nonnull
    List<Violation> parse(@Nonnull final File xmlFile) {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(xmlFile);

            return parse(document);
        } catch (final FileNotFoundException e){
            LOGGER.error("OCLint report not found {}", xmlFile, e);
        } catch (final ParserConfigurationException e) {
            LOGGER.error("Error parsing file named {}", xmlFile, e);
        } catch (final IOException | SAXException e) {
            LOGGER.error("Error processing file named {}", xmlFile, e);
        }

        return Collections.emptyList();
    }

    @Nonnull
    List<Violation> parse(@Nonnull final Document document) {
        List<Violation> violations = new ArrayList<>();

        NodeList violationElements = getViolationElements(document);
        for (int i = 0; i < violationElements.getLength(); i++) {
            Node node = violationElements.item(i);
            if (isNotElement(node)) {
                continue;
            }

            Element element = (Element) node;
            violations.add(buildViolation(element));
        }

        return violations;
    }

    private boolean isNotElement(@Nonnull Node item) {
        return item.getNodeType() != Node.ELEMENT_NODE;
    }

    @Nonnull
    private Violation buildViolation(@Nonnull Element element) {
        return Violation.builder()
                .setPath(element.getAttribute(PATH))
                .setStartLine(Integer.parseInt(element.getAttribute(START_LINE)))
                .setRule(element.getAttribute(RULE))
                .setMessage(element.getAttribute(MESSAGE))
                .build();
    }
}
