package com.himadri.engine;

import com.himadri.dto.UserRequest;
import com.himadri.model.rendering.Item;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static com.google.common.collect.ImmutableList.of;
import static org.junit.Assert.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class ItemToBoxConverterTest {

    @Mock
    private UserRequest userRequest;

    private ItemToBoxConverter sut;

    @Before
    public void setUp() throws Exception {
        sut = new ItemToBoxConverter();
    }

    @Test
    public void testBoxTitle() throws Exception {
        assertEquals("keretes fafűrész 300mm", sut.getBoxTitle(of(
                itemWithCikkNev(" keretes fafűrész 300mm ")), userRequest));

        assertEquals("keretes fafűrész", sut.getBoxTitle(of(
              itemWithCikkNev("keretes fafűrész ; 300mm/12\"; fűrészlap")), userRequest));

        assertEquals("szeg fejjel a 8865042 pneumatikus tűzőgéphez, 480 db", sut.getBoxTitle(of(
                itemWithCikkNev("szeg fejjel a 8865042 pneumatikus tűzőgéphez, 480 db; szár átm:3,05mm, fej átm:7mm, hossz.:50mm, 40 db-onkét papírszalag"),
                itemWithCikkNev("szeg fejjel a 8865042 pneumatikus tűzőgéphez, 480 db; szár átm:3,05mm, fej átm:7mm, hossz.:75mm, 40 db-onkét papírszalag"),
                itemWithCikkNev("szeg fejjel a 8865042 pneumatikus tűzőgéphez, 480 db; szár átm:3,05mm, fej átm:7mm, hossz.:90mm, 40 db-onkét papírszalag")
        ), userRequest));

        assertEquals("órás csavarhúzó klt.", sut.getBoxTitle(of(
                itemWithCikkNev("órás csavarhúzó klt. 7 db Cr.V. mágneses"),
                itemWithCikkNev("órás csavarhúzó klt. 22 db C.V. bitekkel")
        ), userRequest));

        assertEquals("órás csavarhúzó", sut.getBoxTitle(of(
                itemWithCikkNev("órás csavarhúzó kerekes 7 db Cr.V."),
                itemWithCikkNev("órás csavarhúzó kerti 22 db C.V.")
        ), userRequest));
    }


    @Test
    public void testDescription() throws Exception {
        assertEquals("szár átm:3,05mm",
                sut.convertItemToArticle(itemWithCikkNev(
                        "szeg fejjel a 8865042 pneumatikus tűzőgéphez, 480 db; szár átm:3,05mm"),
                        "szeg fejjel a 8865042 pneumatikus tűzőgéphez, 480 db").getDescription());
        assertEquals("szár átm:3,05mm",
                sut.convertItemToArticle(itemWithCikkNev(
                        "szeg fejjel a 8865042 pneumatikus tűzőgéphez, 480 db ; szár átm:3,05mm"),
                        "szeg fejjel a 8865042 pneumatikus tűzőgéphez, 480 db").getDescription());
        assertEquals("",
                sut.convertItemToArticle(itemWithCikkNev(
                        "szeg fejjel a 8865042"),
                        "szeg fejjel a 8865042").getDescription());
    }

    private Item itemWithCikkNev(String cikkNev) {
        Item item = new Item();
        item.setCikknev(cikkNev);
        return item;
    }
}