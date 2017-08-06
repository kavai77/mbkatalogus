package com.himadri;

import com.himadri.model.Box;
import com.himadri.model.Page;
import com.himadri.renderer.DocumentRenderer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Component
public class RenderingTest {

    @Autowired
    private DocumentRenderer documentRenderer;

    public List<String> testRendering() throws IOException {

        Box[] boxes = new Box[2];
        boxes[0]  = new Box("3645.psd", "EXTOL PREMIUM 5.psd", "mélykúti csőszivattyú, 750W, 3\"",
                "CS 2", "Munkaruházat", 0, 1, Collections.singletonList(new Box.Article("8895010", "56400", "3\" testátmérő, szállító teljesítmény: 1,9m3/h, max.száll. 102m, csőcsonk: 1\"")));

        boxes[1]  = new Box("19101a.psd", "EXTOL PREMIUM_.psd", "NiCd pótakku 14,4V a 402313 akkus csavarhúzókhoz",
                "CS 2", "Pneumatikus gépek", 1, 1, Collections.singletonList(new Box.Article("8895200", "43900", "max./névleges nyomás: 140/100 Bar, max. 400 liter/óra, 5m tömlő, max. 40 °C, mosószer tartály, 12,7 kg, papír doboz")));
        Page[] pages = new Page[2];
        pages[0] = new Page("Kéziszerszám Katalógus", "Munkaruházat", 16, Page.Orientation.LEFT,
                new Random().ints(2, 0, boxes.length).mapToObj(i -> boxes[i]).collect(Collectors.toList()));
        pages[1] = new Page("Kéziszerszám Katalógus", "Pneumatikus gépek", 17, Page.Orientation.RIGHT,
                new Random().ints(1, 0, boxes.length).mapToObj(i -> boxes[i]).collect(Collectors.toList()));

        return documentRenderer.renderDocument(Arrays.asList(pages));
    }


}
