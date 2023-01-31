/*
 * Copyright 2017 National Bank of Belgium
 * 
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package internal.workspace.file;

import com.google.common.io.MoreFiles;
import ec.demetra.workspace.WorkspaceFamily;
import static ec.demetra.workspace.WorkspaceFamily.SA_DOC_TRAMOSEATS;
import static ec.demetra.workspace.WorkspaceFamily.SA_DOC_X13;
import static ec.demetra.workspace.WorkspaceFamily.SA_MULTI;
import static ec.demetra.workspace.WorkspaceFamily.SA_SPEC_TRAMOSEATS;
import static ec.demetra.workspace.WorkspaceFamily.SA_SPEC_X13;
import static ec.demetra.workspace.WorkspaceFamily.UTIL_CAL;
import static ec.demetra.workspace.WorkspaceFamily.UTIL_VAR;
import internal.workspace.file.xml.XmlLegacyWorkspace;
import internal.workspace.file.xml.XmlLegacyWorkspaceItem;
import internal.io.JaxbUtil;
import java.io.IOException;
import java.nio.file.FileSystemException;
import java.nio.file.Path;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.function.Predicate;
import nbbrd.io.xml.Xml;
import nbbrd.io.xml.bind.Jaxb;

/**
 *
 * @author Philippe Charles
 */
final class LegacyIndexer implements Indexer {

    static boolean isValid(Path file) throws IOException {
        try {
            unmarshalIndex(file);
            return true;
        } catch (FileSystemException ex) {
            throw ex;
        } catch (IOException ex) {
            return false;
        }
    }

    private final Path file;

    LegacyIndexer(Path file) {
        this.file = file;
    }

    @Override
    public void checkId(Index.Key key) throws IOException {
        if (WorkspaceFamily.UTIL_CAL.equals(key.getFamily())) {
            if (!key.getId().equals("Calendars")) {
                throw new IOException("Only one calendar file is allowed");
            }
        } else if (WorkspaceFamily.UTIL_VAR.equals(key.getFamily())) {
            if (!key.getId().equals("Variables")) {
                throw new IOException("Only one variable file is allowed");
            }
        }
    }

    @Override
    public Index loadIndex() throws IOException {
        return xmlToIndex(unmarshalIndex(file), MoreFiles.getNameWithoutExtension(file));
    }

    @Override
    public void storeIndex(Index index) throws IOException {
        marshalIndex(file, indexToXml(index));
    }

    @Override
    public void close() throws IOException {
        // do nothing
    }

    private static Index xmlToIndex(XmlLegacyWorkspace xml, String name) {
        Index.Builder result = Index.builder().name(name);

        JaxbUtil.forSingle(xml.calendars, pusher(result, UTIL_CAL));
        JaxbUtil.forSingle(xml.variables, pusher(result, UTIL_VAR));

        JaxbUtil.forEach(xml.saProcessing, pusher(result, SA_MULTI));
        JaxbUtil.forEach(xml.tramoseatsDocs, pusher(result, SA_DOC_TRAMOSEATS));
        JaxbUtil.forEach(xml.tramoseatsSpecs, pusher(result, SA_SPEC_TRAMOSEATS));
        JaxbUtil.forEach(xml.x12Docs, pusher(result, SA_DOC_X13));
        JaxbUtil.forEach(xml.x12Specs, pusher(result, SA_SPEC_X13));

        return result.build();
    }

    private static Consumer<XmlLegacyWorkspaceItem> pusher(Index.Builder result, WorkspaceFamily family) {
        return o -> result.item(getIndexKey(o, family), getIndexValue(o));
    }

    private static Index.Key getIndexKey(XmlLegacyWorkspaceItem xml, WorkspaceFamily family) {
        return new Index.Key(family, xml.file != null ? xml.file : xml.name);
    }

    private static Index.Value getIndexValue(XmlLegacyWorkspaceItem xml) {
        return new Index.Value(xml.name, xml.readOnly, null);
    }

    private static XmlLegacyWorkspace indexToXml(Index index) {
        XmlLegacyWorkspace result = new XmlLegacyWorkspace();

        result.calendars = toSingleItem(index, UTIL_CAL);
        result.variables = toSingleItem(index, UTIL_VAR);

        result.saProcessing = toEachItem(index, SA_MULTI);
        result.tramoseatsDocs = toEachItem(index, SA_DOC_TRAMOSEATS);
        result.tramoseatsSpecs = toEachItem(index, SA_SPEC_TRAMOSEATS);
        result.x12Docs = toEachItem(index, SA_DOC_X13);
        result.x12Specs = toEachItem(index, SA_SPEC_X13);

        return result;
    }

    private static XmlLegacyWorkspaceItem toSingleItem(Index index, WorkspaceFamily family) {
        return index.getItems().entrySet().stream()
                .filter(filterOnFamily(family))
                .map(LegacyIndexer::map)
                .findFirst()
                .orElse(null);
    }

    private static XmlLegacyWorkspaceItem[] toEachItem(Index index, WorkspaceFamily family) {
        return index.getItems().entrySet().stream()
                .filter(filterOnFamily(family))
                .map(LegacyIndexer::map)
                .toArray(XmlLegacyWorkspaceItem[]::new);
    }

    private static Predicate<Entry<Index.Key, Index.Value>> filterOnFamily(WorkspaceFamily family) {
        return o -> o.getKey().getFamily().equals(family);
    }

    private static XmlLegacyWorkspaceItem map(Entry<Index.Key, Index.Value> o) {
        XmlLegacyWorkspaceItem result = new XmlLegacyWorkspaceItem();
        result.file = o.getKey().getId();
        result.name = o.getValue().getLabel();
        result.readOnly = o.getValue().isReadOnly();
        return result;
    }

    private static final Xml.Parser<XmlLegacyWorkspace> PARSER;
    private static final Xml.Formatter<XmlLegacyWorkspace> FORMATTER;

    static {
        PARSER = Jaxb.Parser.of(XmlLegacyWorkspace.class);
        FORMATTER = Jaxb.Formatter.of(XmlLegacyWorkspace.class).withFormatted(true);
    }

    private static XmlLegacyWorkspace unmarshalIndex(Path file) throws IOException {
        return PARSER.parsePath(file);
    }

    private static void marshalIndex(Path file, XmlLegacyWorkspace jaxbElement) throws IOException {
        FORMATTER.formatPath(jaxbElement, file);
    }
}
