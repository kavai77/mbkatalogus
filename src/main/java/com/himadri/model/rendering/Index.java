package com.himadri.model.rendering;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.text.Collator;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Getter
@NoArgsConstructor
public class Index {
    private final List<Record> productNumberIndex = new ArrayList<>();
    private final List<Record> productNameIndex = new ArrayList<>();

    @Getter
    @AllArgsConstructor
    @EqualsAndHashCode
    public static class Record implements Comparable<Record>{
        private static final Collator HU_COLLATOR = Collator.getInstance(new Locale("hu"));
        private final String key;
        private final int pageNumber;

        @Override
        public int compareTo(Record o) {
            return HU_COLLATOR.compare(key, o.key);
        }
    }
}
