package com.himadri.engine;

import com.himadri.dto.UserRequest;
import com.himadri.model.rendering.Item;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.supercsv.cellprocessor.Optional;
import org.supercsv.cellprocessor.ParseLong;
import org.supercsv.cellprocessor.constraint.StrNotNullOrEmpty;
import org.supercsv.cellprocessor.constraint.UniqueHashCode;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.CsvBeanReader;
import org.supercsv.io.ICsvBeanReader;
import org.supercsv.prefs.CsvPreference;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

@Component
public class CatalogueReader {
    private static final Logger LOGGER = LoggerFactory.getLogger(CatalogueReader.class);

    public List<Item> readWithCsvBeanReader(UserRequest userRequest) throws IOException {

        final List<Item> beanArrayList = new ArrayList<>();
        try (ICsvBeanReader beanReader = new CsvBeanReader(new InputStreamReader(userRequest.getCsvInputStream()),
                CsvPreference.EXCEL_NORTH_EUROPE_PREFERENCE)) {
            final String[] header = beanReader.getHeader(true);
            final CellProcessor[] processors = getProcessors();

            Item catalogueBean;
            while( (catalogueBean = beanReader.read(Item.class, header, processors)) != null ) {
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
                new Optional(new ParseLong()), // cikksorrend
                new Optional(), // kepnev
                new Optional(), // gyartokepnev
        };
    }
}
