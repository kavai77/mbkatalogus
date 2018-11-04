package com.himadri.engine;

import com.himadri.dto.UserRequest;
import com.himadri.model.rendering.CsvItem;

import java.io.IOException;
import java.util.List;

public interface CatalogueReader {
    List<CsvItem> readWithCsvBeanReader(UserRequest userRequest) throws IOException;
}
