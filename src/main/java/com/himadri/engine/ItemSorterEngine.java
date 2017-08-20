package com.himadri.engine;

import com.himadri.dto.ErrorItem;
import com.himadri.exception.ValidationException;
import com.himadri.model.rendering.Item;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Component
public class ItemSorterEngine {

    private static final long MAX_CIKKSORREND = 0x7FFFFFFF;

    public void sortItems(List<Item> items) throws ValidationException {
        items.stream().filter(a -> a.getCikksorrend() == null).forEach(a -> a.setCikksorrend(MAX_CIKKSORREND));
        final Optional<Item> firstBigCikksorrend = items.stream().filter(a -> a.getCikksorrend() > MAX_CIKKSORREND).findFirst();
        if (firstBigCikksorrend.isPresent()) {
            throw new ValidationException(ErrorItem.Severity.ERROR, ErrorItem.ErrorCategory.INFO, String.format(
                    "A maximális érték a cikk sorrendnél %d de a %s cikkszámú cikknél a cikksorrend %d",
                    MAX_CIKKSORREND, firstBigCikksorrend.get().getCikkszam(), firstBigCikksorrend.get().getCikksorrend()));
        }
        for (int i = 0; i < items.size(); i++) {
            final Item item = items.get(i);
            item.setCikksorrend((item.getCikksorrend() << 32) | i);
        }
        items.sort(Comparator.comparing(Item::getCikksorrend));
    }
}
