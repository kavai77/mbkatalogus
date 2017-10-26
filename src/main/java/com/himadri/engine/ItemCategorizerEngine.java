package com.himadri.engine;

import com.himadri.exception.ValidationException;
import com.himadri.model.rendering.Item;
import com.himadri.model.service.UserSession;

import java.util.List;

public interface ItemCategorizerEngine {
    List<ProductGroup> itemsPerProductGroupPerBox(List<Item> items, UserSession userSession) throws ValidationException;

    class ProductGroup {
        private final String name;
        private final List<BoxItems> boxes;


        public ProductGroup(String name, List<BoxItems> boxes) {
            this.name = name;
            this.boxes = boxes;
        }

        public String getName() {
            return name;
        }

        public List<BoxItems> getBoxes() {
            return boxes;
        }
    }

    class BoxItems {
        private final List<Item> items;

        public BoxItems(List<Item> items) {
            this.items = items;
        }

        public List<Item> getItems() {
            return items;
        }
    }
}
