package com.himadri.engine;

import com.himadri.dto.UserRequest;
import com.himadri.model.rendering.Item;
import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.bean.CsvToBeanFilter;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;

public class CatalogueReaderWithOpenCsv implements CatalogueReader {
    @Override
    public List<Item> readWithCsvBeanReader(UserRequest userRequest) throws IOException {
        return new CsvToBeanBuilder<Item>(new InputStreamReader(userRequest.getCsvInputStream()))
                .withType(Item.class)
                .withOrderedResults(true)
                .withSeparator(';')
                .withFilter(new CsvToBeanFilter() {
                    @Override
                    public boolean allowLine(String[] strings) {
                        return Arrays.stream(strings).anyMatch(StringUtils::isNotBlank);
                    }
                })
                .build()
                .parse();
    }
}
