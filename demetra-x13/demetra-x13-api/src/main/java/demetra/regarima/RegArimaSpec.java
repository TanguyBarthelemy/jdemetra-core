/*
 * Copyright 2020 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 * https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package demetra.regarima;

import demetra.arima.SarimaSpec;
import demetra.design.Development;
import demetra.design.LombokWorkaround;
import demetra.modelling.RegressionTestSpec;
import demetra.modelling.TransformationType;
import demetra.timeseries.calendars.LengthOfPeriodType;
import demetra.timeseries.regression.TradingDaysType;
import demetra.util.Validatable;
import lombok.NonNull;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Beta)
@lombok.Value

@lombok.Builder(toBuilder = true, builderClassName = "Builder", buildMethodName = "buildWithoutValidation")
public final class RegArimaSpec implements Validatable<RegArimaSpec> {

    private static final RegArimaSpec DEFAULT = RegArimaSpec.builder().build();

    private BasicSpec basic;
    private TransformSpec transform;
    private RegressionSpec regression;
    private OutlierSpec outliers;
    private AutoModelSpec autoModel;
    private SarimaSpec arima;
    private EstimateSpec estimate;

    @LombokWorkaround
    public static Builder builder() {
        SarimaSpec arima = SarimaSpec.airline();
        return new Builder()
                .basic(BasicSpec.builder().build())
                .transform(TransformSpec.builder().build())
                .estimate(EstimateSpec.builder().build())
                .autoModel(AutoModelSpec.builder().build())
                .outliers(OutlierSpec.builder().build())
                .arima(arima)
                .regression(RegressionSpec.builder().build());
    }

    public boolean isUsingAutoModel() {
        return autoModel.isEnabled();
    }

    @Override
    public RegArimaSpec validate() throws IllegalArgumentException {
        basic.validate();
        transform.validate();
        regression.validate();
        outliers.validate();
        autoModel.validate();
        estimate.validate();
        return this;
    }

    public boolean isDefault() {
        return this.equals(DEFAULT);
    }

    public static class Builder implements Validatable.Builder<RegArimaSpec> {

        public Builder usingAutoModel(boolean enableAutoModel) {
            this.autoModel = autoModel.toBuilder().enabled(enableAutoModel).build();
            return this;
        }

        public Builder arima(@NonNull SarimaSpec sarima) {
            this.arima = sarima;
            if (this.autoModel == null) {
                this.autoModel = AutoModelSpec.builder().build();
            }
            return this;
        }
    }

    //<editor-fold defaultstate="collapsed" desc="Default specifications">
    public static final RegArimaSpec RGDISABLED, RG0, RG1, RG2, RG3, RG4, RG5;

    public static final RegArimaSpec[] allSpecifications() {
        return new RegArimaSpec[]{RG0, RG1, RG2, RG3, RG4, RG5};
    }

    static {
        RGDISABLED = RegArimaSpec.builder()
                .basic(BasicSpec.builder().preProcessing(false).build())
                .build();

        TransformSpec tr = TransformSpec.builder()
                .function(TransformationType.Auto)
                .build();

        EasterSpec easter = EasterSpec.builder()
                .easterSpec(true)
                .build();

        TradingDaysSpec wd = TradingDaysSpec.td(TradingDaysType.WorkingDays, LengthOfPeriodType.LeapYear, RegressionTestSpec.Remove, true);

        TradingDaysSpec td = TradingDaysSpec.td(TradingDaysType.TradingDays, LengthOfPeriodType.LeapYear, RegressionTestSpec.Remove, true);

        RegressionSpec rwd = RegressionSpec.builder()
                .easter(easter)
                .tradingDays(wd)
                .build();

        RegressionSpec rtd = RegressionSpec.builder()
                .easter(easter)
                .tradingDays(td)
                .build();

        OutlierSpec o = OutlierSpec.builder()
                .type(SingleOutlierSpec.builder().type("AO").build())
                .type(SingleOutlierSpec.builder().type("TC").build())
                .type(SingleOutlierSpec.builder().type("LS").build())
                .build();

        RG0 = RegArimaSpec.builder()
                .build();

        RG1 = RegArimaSpec.builder()
                .transform(tr)
                .outliers(o)
                .build();

        RG2 = RegArimaSpec.builder()
                .transform(tr)
                .outliers(o)
                .regression(rwd)
                .build();
        RG3 = RegArimaSpec.builder()
                .transform(tr)
                .outliers(o)
                .usingAutoModel(true)
                .build();
        RG4 = RegArimaSpec.builder()
                .transform(tr)
                .outliers(o)
                .regression(rwd)
                .usingAutoModel(true)
                .build();

        RG5 = RegArimaSpec.builder()
                .transform(tr)
                .outliers(o)
                .regression(rtd)
                .usingAutoModel(true)
                .build();
    }

    public static RegArimaSpec fromString(String name) {
        switch (name) {
            case "RG0":
                return RG0;
            case "RG1":
                return RG1;
            case "RG2c":
                return RG2;
            case "RG3":
                return RG3;
            case "RG4c":
                return RG4;
            case "RG5c":
                return RG5;
            default:
                throw new RegArimaException();
        }
    }
    //</editor-fold>
}