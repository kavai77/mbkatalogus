package com.himadri.engine;

import com.google.common.cache.Cache;
import com.himadri.dto.UserRequest;
import com.himadri.model.rendering.Item;
import com.himadri.model.service.UserSession;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static com.google.common.collect.ImmutableList.of;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ItemToBoxConverterTest {
    private static final String USER_REQUEST_ID = "USER_REQUEST_ID";

    @Mock
    private UserRequest userRequest;

    @Mock
    private Cache<String, UserSession> userSessionCacheMock;

    @Mock
    private UserSession userSessionMock;

    private ItemToBoxConverter sut;

    @Before
    public void setUp() throws Exception {
        sut = new ItemToBoxConverter();
        sut.userSessionCache = userSessionCacheMock;
        when(userRequest.getRequestId()).thenReturn(USER_REQUEST_ID);
        when(userSessionCacheMock.getIfPresent(USER_REQUEST_ID)).thenReturn(userSessionMock);
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