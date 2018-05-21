package com.himadri.engine;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.himadri.dto.ErrorItem;
import com.himadri.exception.ValidationException;
import com.himadri.model.rendering.Item;
import com.himadri.model.service.UserSession;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

import static com.himadri.dto.ErrorItem.ErrorCategory.FORMATTING;
import static com.himadri.dto.ErrorItem.Severity.ERROR;
import static org.apache.commons.lang3.StringUtils.*;

public class StrictItemCategorizerEngine implements ItemCategorizerEngine {
    @Autowired
    private PersistenceService persistenceService;

    @Override
    public List<ProductGroup> itemsPerProductGroupPerBox(List<Item> items, UserSession userSession) throws ValidationException {
        Set<String> productGroupSetWithoutChapter = new HashSet<>(
                persistenceService.getInstanceProperties().getProductGroupsWithoutChapter());
        List<ProductGroup> productGroups = new ArrayList<>();
        Optional<String> firstProductGroupName = items.stream()
                .map(this::getProductGroupName)
                .filter(i -> !productGroupSetWithoutChapter.contains(i))
                .findFirst();
        if (!firstProductGroupName.isPresent()) {
            throw new ValidationException(ERROR, FORMATTING, "Nincs egyetlen fejezet sem!");
        }
        productGroups.add(new ProductGroup(firstProductGroupName.get(), new ArrayList<>()));
        Set<String> previousProductGroups = Sets.newHashSet(firstProductGroupName.get());
        Map<String, String> previousPictures = new HashMap<>();

        for (int i = 0; i < items.size(); i++) {
            Item item = items.get(i);
            final String productGroupName = getProductGroupName(item);
            final String productPictureKey = getProductPictureKey(item);
            final boolean sameProductPictureWithPreviousItem = i > 0 &&
                    StringUtils.equals(productPictureKey, getProductPictureKey(items.get(i - 1)));
            if (sameProductPictureWithPreviousItem) { // same box as previous
                if (lastItem(productGroups).getBoxes().isEmpty()) {
                    throw new ValidationException(ERROR, FORMATTING,
                        String.format("Két egy boxba tarozó cikknek más cikkcsoportja van: %s és %s",
                            items.get(i - 1).getCikkszam(), item.getCikkszam()));
                }
                lastItem(lastItem(productGroups).getBoxes()).getItems().add(item);
            } else { // new box
                if (previousPictures.containsKey(productPictureKey)) {
                    userSession.addErrorItem(ErrorItem.Severity.WARN, FORMATTING,
                            String.format("Két terméket egy boxba kellene csoportosítani, viszont sorrendben " +
                                    "nincsenek egymás mellett. Így most külön boxban fognak megjelenni: %s és %s",
                                    previousPictures.get(productPictureKey), item.getCikkszam()));
                }
                previousPictures.put(productPictureKey, item.getCikkszam());
                final boolean sameProductGroup = productGroupSetWithoutChapter.contains(productGroupName) ||
                        StringUtils.equals(productGroupName, lastItem(productGroups).getName());
                if (!sameProductGroup) {
                    if (previousProductGroups.contains(productGroupName)) {
                        throw new ValidationException(ERROR, FORMATTING, String.format("A %s cikk sorrendben kívül " +
                                "áll a cikkcsoportja fejezetén: %s.", item.getCikkszam(), productGroupName));
                    }
                    productGroups.add(new ProductGroup(productGroupName, new ArrayList<>()));
                    previousProductGroups.add(productGroupName);
                }
                lastItem(productGroups).getBoxes().add(new BoxItems(Lists.newArrayList(item)));
            }
        }

        return productGroups;
    }

    private String getProductGroupName(Item item) {
        return stripToEmpty(item.getCikkcsoportnev());
    }

    private String getProductPictureKey(Item item) {
        return isNotBlank(item.getKepnev()) ? strip(item.getKepnev()) : UUID.randomUUID().toString();
    }

    private <T> T lastItem(List<T> list) {
        return list.get(list.size() - 1);
    }
}
