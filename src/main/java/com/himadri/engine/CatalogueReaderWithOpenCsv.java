package com.himadri.engine;

import com.himadri.dto.UserRequest;
import com.himadri.model.rendering.CsvItem;
import com.opencsv.bean.CsvToBeanBuilder;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;

public class CatalogueReaderWithOpenCsv implements CatalogueReader {
    @Override
    public List<CsvItem> readWithCsvBeanReader(UserRequest userRequest) throws IOException {
        return new CsvToBeanBuilder<CsvItem>(new InputStreamReader(userRequest.getCsvInputStream()))
                .withType(CsvItem.class)
                .withOrderedResults(true)
                .withSeparator(';')
                .withFilter(strings -> Arrays.stream(strings).anyMatch(StringUtils::isNotBlank))
                .build()
                .parse();
    }
}
