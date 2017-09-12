package com.himadri.model.rendering;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.text.Collator;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class Index {
    private final List<Record> productNumberIndex = new ArrayList<>();
    private final List<Record> productNameIndex = new ArrayList<>();

    public List<Record> getProductNumberIndex() {
        return productNumberIndex;
    }
    public List<Record> getProductNameIndex() {
        return productNameIndex;
    }

    public static class Record implements Comparable<Record>{
        private static final Collator HU_COLLATOR = Collator.getInstance(new Locale("hu"));
        private final String key;
        private final int pageNumber;

        public Record(String key, int pageNumber) {
            this.key = key;
            this.pageNumber = pageNumber;
        }

        public String getKey() {
            return key;
        }

        public int getPageNumber() {
            return pageNumber;
        }

        @Override
        public int compareTo(Record o) {
            return HU_COLLATOR.compare(key, o.key);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;

            if (o == null || getClass() != o.getClass()) return false;

            Record record = (Record) o;

            return new EqualsBuilder()
                    .append(pageNumber, record.pageNumber)
                    .append(key, record.key)
                    .isEquals();
        }

        @Override
        public int hashCode() {
            return new HashCodeBuilder()
                    .append(key)
                    .append(pageNumber)
                    .toHashCode();
        }
    }
}
