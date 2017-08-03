package com.himadri.csv;

import com.himadri.Settings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.supercsv.cellprocessor.Optional;
import org.supercsv.cellprocessor.constraint.StrNotNullOrEmpty;
import org.supercsv.cellprocessor.constraint.UniqueHashCode;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.CsvBeanReader;
import org.supercsv.io.ICsvBeanReader;
import org.supercsv.prefs.CsvPreference;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
public class CatalogueReader {
    private static final Logger LOGGER = LoggerFactory.getLogger(CatalogueReader.class);
    public List<CatalogueBean> readWithCsvBeanReader() throws IOException {

        final List<CatalogueBean> beanArrayList = new ArrayList<>();
        try (ICsvBeanReader beanReader = new CsvBeanReader(new FileReader(Settings.CSV_LOCATION),
                CsvPreference.EXCEL_NORTH_EUROPE_PREFERENCE)) {
            final String[] header = beanReader.getHeader(true);
            final CellProcessor[] processors = getProcessors();

            CatalogueBean catalogueBean;
            while( (catalogueBean = beanReader.read(CatalogueBean.class, header, processors)) != null ) {
                beanArrayList.add(catalogueBean);
            }

        }
        return beanArrayList;
    }

    private CellProcessor[] getProcessors() {
        return new CellProcessor[] {
                new UniqueHashCode(), // cikkszam
                new StrNotNullOrEmpty(), // cikknev
                new StrNotNullOrEmpty(), // cikkfajta
                new StrNotNullOrEmpty(), // cikkcsoportnev
                new Optional(), // gyarto
                new StrNotNullOrEmpty(), // kiskerar
                new StrNotNullOrEmpty(), // nagykerar
                new Optional(), // m1
                new Optional(), // m2
                new Optional(), // m3
                new Optional(), // termekinfo
                new Optional(), // me
                new Optional(), // cikkcsopsorrend
                new Optional(), // cikksorrend
                new Optional(), // kepnev
                new Optional(), // gyartokepnev
        };
    }
}
