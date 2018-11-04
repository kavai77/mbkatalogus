package com.himadri.engine;

import com.himadri.exception.ValidationException;
import com.himadri.model.rendering.CsvItem;
import com.himadri.model.service.UserSession;

import java.util.List;

public interface ItemCategorizerEngine {
    List<CsvProductGroup> groupCsvItems(List<CsvItem> items, UserSession userSession) throws ValidationException;

    class CsvProductGroup {
        private final String name;
        private final List<CsvItemGroup> itemGroups;


        public CsvProductGroup(String name, List<CsvItemGroup> itemGroups) {
            this.name = name;
            this.itemGroups = itemGroups;
        }

        public String getName() {
            return name;
        }

        public List<CsvItemGroup> getItemGroups() {
            return itemGroups;
        }
    }

    class CsvItemGroup {
        private final List<CsvItem> items;

        public CsvItemGroup(List<CsvItem> items) {
            this.items = items;
        }

        public List<CsvItem> getItems() {
            return items;
        }
    }
}
