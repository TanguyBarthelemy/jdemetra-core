/*
 * Copyright 2013 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved
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
package demetra.timeseries;

import demetra.data.HasEmptyCause;
import demetra.data.Seq;
import demetra.util.Collections2;
import nbbrd.design.LombokWorkaround;
import nbbrd.design.StaticFactoryMethod;
import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * @author Jean Palate
 */
@lombok.Value
@lombok.Builder(toBuilder = true)
public class TsCollection implements Seq<Ts>, HasEmptyCause {

    @lombok.NonNull
    private TsMoniker moniker;

    @lombok.NonNull
    private TsInformationType type;

    @lombok.NonNull
    private String name;

    @lombok.Singular("meta")
    private Map<String, String> meta;

    @lombok.Singular
    private List<Ts> items;

    @Nullable
    private String emptyCause;

    @LombokWorkaround
    public static Builder builder() {
        return new Builder()
                .moniker(TsMoniker.NULL)
                .type(TsInformationType.UserDefined)
                .name("");
    }

    public static final TsCollection EMPTY = TsCollection.builder().build();

    @StaticFactoryMethod
    public static @NonNull TsCollection of(@NonNull Iterable<Ts> items) {
        Builder result = builder();
        items.forEach(result::item);
        return result.build();
    }

    @StaticFactoryMethod
    public static @NonNull TsCollection of(@NonNull Ts item) {
        Objects.requireNonNull(item);
        return builder().item(item).build();
    }

    public static @NonNull Collector<Ts, ?, TsCollection> toTsCollection() {
        return Collectors.collectingAndThen(Collectors.toList(), TsCollection::of);
    }

    @Override
    public @NonNegative int length() {
        return items.size();
    }

    @Override
    public Ts get(@NonNegative int index) throws IndexOutOfBoundsException {
        return items.get(index);
    }

    @Override
    public @Nullable String getEmptyCause() {
        return emptyCause;
    }

    public @NonNull TsCollection load(@NonNull TsInformationType info, @NonNull TsFactory factory) {
        Objects.requireNonNull(info);
        Objects.requireNonNull(factory);

        if (!moniker.isProvided()) {
            return stream().map(ts -> ts.load(info, factory)).collect(toTsCollection());
        }
        if (type.encompass(info)) {
            return this;
        }
        return factory.makeTsCollection(moniker, info);
    }

    public @NonNull TsCollection replaceAll(@NonNull Iterable<Ts> col) {
        Map<TsMoniker, Ts> tsByMoniker = Collections2.streamOf(col).collect(Collectors.toMap(Ts::getMoniker, Function.identity()));

        TsCollection.Builder result = builder().moniker(getMoniker());

        boolean modified = false;
        for (Ts original : this) {
            Ts replaced = tsByMoniker.get(original.getMoniker());
            if (replaced != null) {
                modified = true;
                result.item(replaced);
            } else {
                result.item(original);
            }
        }

        return modified ? result.build() : this;
    }
}
