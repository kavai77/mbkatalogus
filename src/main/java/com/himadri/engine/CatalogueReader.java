package com.himadri.engine;

import com.himadri.dto.UserRequest;
import com.himadri.model.rendering.Item;

import java.io.IOException;
import java.util.List;

public interface CatalogueReader {
    List<Item> readWithCsvBeanReader(UserRequest userRequest) throws IOException;
}
