/*
 * Copyright (C) 2024 FeatJAR-Development-Team
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
package de.featjar.base.io.xml;

import de.featjar.base.FeatJAR;
import de.featjar.base.data.Problem;
import de.featjar.base.data.Result;
import de.featjar.base.io.format.IFormat;
import de.featjar.base.io.format.ParseException;
import de.featjar.base.io.format.ParseProblem;
import de.featjar.base.io.input.AInputMapper;
import de.featjar.base.io.input.InputHeader;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 * Helpers for parsing and writing an object from and into an XML file.
 *
 * @param <T> the type of read/written data
 * @author Sebastian Krieter
 * @author Elias Kuiter
 */
public abstract class AXMLFormat<T> implements IFormat<T> {
    protected static final Pattern completeTagPattern = Pattern.compile("<(\\w+)[^/]*>.*</\\1.*>");
    protected static final Pattern incompleteTagPattern = Pattern.compile("(<\\w+[^/>]*>)|(</\\w+[^>]*>)");

    protected List<Problem> parseProblems = new ArrayList<>();

    protected abstract T parseDocument(Document document) throws ParseException;

    protected abstract void writeDocument(T object, Document doc);

    protected abstract Pattern getInputHeaderPattern();

    @Override
    public String getFileExtension() {
        return "xml";
    }

    @Override
    public boolean supportsContent(InputHeader inputHeader) {
        return supportsParse()
                && getInputHeaderPattern().matcher(inputHeader.get()).find();
    }

    protected List<Element> getElements(NodeList nodeList) {
        final ArrayList<Element> elements = new ArrayList<>(nodeList.getLength());
        for (int temp = 0; temp < nodeList.getLength(); temp++) {
            final org.w3c.dom.Node nNode = nodeList.item(temp);
            if (nNode.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
                final Element eElement = (Element) nNode;
                elements.add(eElement);
            }
        }
        return elements;
    }

    protected List<Element> getElements(final Element element, final String nodeName) {
        return getElements(element.getElementsByTagName(nodeName));
    }

    protected Element getElement(final Element element, final String nodeName) throws ParseException {
        final List<Element> elements = getElements(element, nodeName);
        if (elements.size() > 1) {
            addParseProblem("Multiple nodes of " + nodeName + " defined.", element, Problem.Severity.WARNING);
        } else if (elements.isEmpty()) {
            addParseProblem("Node " + nodeName + " not defined!", element, Problem.Severity.ERROR);
        }
        return elements.get(0);
    }

    protected Result<Element> getElementResult(final Element element, final String nodeName) {
        final List<Element> elements = getElements(element, nodeName);
        if (elements.size() > 1) {
            try {
                addParseProblem("Multiple nodes of " + nodeName + " defined.", element, Problem.Severity.WARNING);
            } catch (ParseException ignored) {
            }
        } else if (elements.isEmpty()) {
            return Result.empty();
        }
        return Result.of(elements.get(0));
    }

    protected Element getDocumentElement(final Document document, final String... nodeNames) throws ParseException {
        final Element element = document.getDocumentElement();
        if (element == null || Arrays.stream(nodeNames).noneMatch(element.getNodeName()::equals)) {
            addParseProblem("Node " + Arrays.toString(nodeNames) + " not defined!", element, Problem.Severity.ERROR);
        }
        return element;
    }

    protected void addParseProblem(String message, org.w3c.dom.Node node, Problem.Severity severity)
            throws ParseException {
        int lineNumber = node != null
                ? Integer.parseInt(node.getUserData(PositionalXMLHandler.LINE_NUMBER_KEY_NAME)
                        .toString())
                : 0;
        if (severity.equals(Problem.Severity.ERROR)) {
            throw new ParseException(message, lineNumber);
        } else {
            parseProblems.add(new ParseProblem(message, severity, lineNumber));
        }
    }

    @Override
    public Result<T> parse(AInputMapper inputMapper) {
        try {
            parseProblems.clear();
            final Document document =
                    DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
            SAXParserFactory.newInstance()
                    .newSAXParser()
                    .parse(new InputSource(inputMapper.get().getReader()), new PositionalXMLHandler(document));
            document.getDocumentElement().normalize();
            return Result.of(parseDocument(document), parseProblems);
        } catch (final ParseException e) {
            return Result.empty(new ParseProblem(e.getMessage(), Problem.Severity.ERROR, e.getLineNumber()));
        } catch (final Exception e) {
            return Result.empty(new Problem(e));
        }
    }

    @Override
    public Result<String> serialize(T object) {
        final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        dbf.setIgnoringComments(true);
        dbf.setIgnoringElementContentWhitespace(false);
        dbf.setCoalescing(true);
        dbf.setExpandEntityReferences(true);
        DocumentBuilder db;
        try {
            db = dbf.newDocumentBuilder();
        } catch (final ParserConfigurationException pce) {
            return Result.empty(pce);
        }
        final Document doc = db.newDocument();
        writeDocument(object, doc);

        try (StringWriter stringWriter = new StringWriter()) {
            final StreamResult streamResult = new StreamResult(stringWriter);
            final TransformerFactory factory = TransformerFactory.newInstance();
            factory.setAttribute("indent-number", 4);
            final Transformer transformer = factory.newTransformer();
            transformer.setOutputProperty(OutputKeys.METHOD, "xml");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.transform(new DOMSource(doc), streamResult);
            return Result.of(prettyPrint(streamResult.getWriter().toString()));
        } catch (final IOException | TransformerException e) {
            return Result.empty(e);
        }
    }

    protected String prettyPrint(String text) {
        final StringBuilder result = new StringBuilder();
        int indentLevel = 0;
        try (final BufferedReader reader = new BufferedReader(new StringReader(text))) {
            for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                final String trimmedLine = line.trim();
                if (!trimmedLine.isEmpty()) {
                    if (completeTagPattern.matcher(trimmedLine).matches()) {
                        appendLine(result, indentLevel, trimmedLine);
                    } else {
                        final Matcher matcher = incompleteTagPattern.matcher(trimmedLine);
                        int start = 0;
                        while (matcher.find()) {
                            appendLine(result, indentLevel, trimmedLine.substring(start, matcher.start()));
                            final String openTag = matcher.group(1);
                            final String closeTag = matcher.group(2);
                            if (openTag != null) {
                                appendLine(result, indentLevel, openTag);
                                indentLevel++;
                            } else if (closeTag != null) {
                                indentLevel--;
                                appendLine(result, indentLevel, closeTag);
                            }
                            start = matcher.end();
                        }
                        appendLine(result, indentLevel, trimmedLine.substring(start));
                    }
                }
            }
        } catch (final IOException e) {
            FeatJAR.log().error(e);
        }
        return result.toString();
    }

    private void appendLine(final StringBuilder result, int indentLevel, String line) {
        final String trimmedLine = line.trim();
        if (!trimmedLine.isEmpty()) {
            result.append("\t".repeat(Math.max(0, indentLevel)));
            result.append(trimmedLine);
            result.append("\n");
        }
    }
}
