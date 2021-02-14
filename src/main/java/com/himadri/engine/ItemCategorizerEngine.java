package com.himadri.engine;

import com.himadri.exception.ValidationException;
import com.himadri.model.rendering.CsvItem;
import com.himadri.model.service.UserSession;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;

public interface ItemCategorizerEngine {
    List<CsvProductGroup> groupCsvItems(List<CsvItem> items, UserSession userSession) throws ValidationException;

    @Data
    @AllArgsConstructor
    @RequiredArgsConstructor
    class CsvProductGroup {
        private final String name;
        private Optional<String> color = Optional.empty();
        private final List<CsvItemGroup> itemGroups;
    }

    @Data
    class CsvItemGroup {
        private final List<CsvItem> items;
    }
}
