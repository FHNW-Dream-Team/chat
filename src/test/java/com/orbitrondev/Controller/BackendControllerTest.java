package com.orbitrondev.Controller;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.ArrayList;

import static org.testng.Assert.*;

public class BackendControllerTest {

    private final String SERVER_IP = "147.86.8.31";
    private final int SERVER_PORT = 50001;
    private BackendController backend;

    private final String TEST_USERNAME = "TestUser";
    private final String TEST_PASSWORD = "TestUser";
    private final String TEST_CHATROOM = "TestChatRoom";
    private String token;

    @BeforeMethod
    public void setUp() throws Exception {
        backend = new BackendController(SERVER_IP, SERVER_PORT);
    }

    @AfterMethod
    public void tearDown() throws Exception {
        backend.close();
    }

    @Test(priority=1)
    public void sendCreateLogin() {
        try {
            boolean result1 = backend.sendCreateLogin(TEST_USERNAME, TEST_PASSWORD);
            boolean result2 = backend.sendCreateLogin(TEST_USERNAME, TEST_PASSWORD);
            assertTrue(result1);
            assertFalse(result2);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test(priority=2)
    public void sendLogin() {
        try {
            String result1 = backend.sendLogin(TEST_USERNAME, "WRONG_PASSWORD");
            String result2 = backend.sendLogin(TEST_USERNAME, TEST_PASSWORD);
            assertNull(result1);
            assertNotNull(result2);
            token = result2;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test(priority=3)
    public void sendChangePassword() {
        try {
            assertFalse(backend.sendChangePassword("WRONG_TOKEN", "WRONG_TOKEN_ANYWAYS"));
            assertTrue(backend.sendChangePassword(token, TEST_PASSWORD));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test(priority=4)
    public void sendCreateChatroom() {
        try {
            assertFalse(backend.sendCreateChatroom("WRONG_TOKEN", "WRONG_TOKEN_ANYWAYS", true));
            assertTrue(backend.sendCreateChatroom(token, TEST_CHATROOM, true));
            assertFalse(backend.sendCreateChatroom(token, TEST_CHATROOM, true));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test(priority=5)
    public void sendListChatrooms() {
        try {
            assertNull(backend.sendListChatrooms("WRONG_TOKEN"));
            assertSame(ArrayList.class, backend.sendListChatrooms(token));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test(priority=6)
    public void sendJoinChatroom() {
        try {
            assertFalse(backend.sendJoinChatroom("WRONG_TOKEN", "WRONG_TOKEN_ANYWAYS", "WRONG_TOKEN_ANYWAYS"));
            assertFalse(backend.sendJoinChatroom(token, "WRONG_ROOM", "WRONG_ROOM_ANYWAYS"));
            assertFalse(backend.sendJoinChatroom(token, TEST_CHATROOM, "WRONG_USER"));
            assertTrue(backend.sendJoinChatroom(token, TEST_CHATROOM, TEST_USERNAME));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test(priority=7)
    public void sendSendMessage() {
        try {
            assertNull(backend.sendSendMessage("WRONG_TOKEN", "WRONG_TOKEN_ANYWAYS", "WRONG_TOKEN_ANYWAYS"));
            assertNull(backend.sendSendMessage(token, "WRONG_TARGET", "WRONG_TARGET_ANYWAYS"));
            assertNotNull(backend.sendSendMessage(token, TEST_CHATROOM, "Running the test of the chat client!"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test(priority=8)
    public void sendUserOnline() {
        try {
            assertFalse(backend.sendUserOnline("WRONG_TOKEN", "WRONG_TOKEN_ANYWAYS"));
            assertFalse(backend.sendUserOnline(token, "WRONG_USER"));
            assertTrue(backend.sendUserOnline(token, TEST_USERNAME));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test(priority=9)
    public void sendListChatroomUsers() {
        try {
            assertNull(backend.sendListChatroomUsers("WRONG_TOKEN", "WRONG_TOKEN_ANYWAYS"));
            assertNull(backend.sendListChatroomUsers(token, "WRONG_CHATROOM"));
            assertSame(ArrayList.class, backend.sendListChatroomUsers(token, TEST_CHATROOM));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test(priority=10)
    public void sendLeaveChatroom() {
        try {
            assertFalse(backend.sendLeaveChatroom("WRONG_TOKEN", "WRONG_TOKEN_ANYWAYS", "WRONG_TOKEN_ANYWAYS"));
            assertFalse(backend.sendLeaveChatroom(token, "WRONG_CHATROOM", "WRONG_CHATROOM_ANYWAYS"));
            assertFalse(backend.sendLeaveChatroom(token, TEST_CHATROOM, "WRONG_USER"));
            assertTrue(backend.sendLeaveChatroom(token, TEST_CHATROOM, TEST_USERNAME));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test(priority=11)
    public void sendDeleteChatroom() {
        try {
            assertFalse(backend.sendDeleteChatroom("WRONG_TOKEN", "WRONG_TOKEN_ANYWAYS"));
            assertFalse(backend.sendDeleteChatroom(token, "WRONG_CHATROOM"));
            assertTrue(backend.sendDeleteChatroom(token, TEST_CHATROOM));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test(priority=12)
    public void sendPing() {
        try {
            assertTrue(backend.sendPing());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test(priority=13)
    public void sendPingWithToken() {
        try {
            assertTrue(backend.sendPing(token));
            assertFalse(backend.sendPing("WRONG_TOKEN"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test(priority=14)
    public void sendDeleteLogin() {
        try {
            assertFalse(backend.sendDeleteLogin("WRONG_TOKEN"));
            assertTrue(backend.sendDeleteLogin(token));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test(priority=15)
    public void sendLogout() {
        try {
            assertTrue(backend.sendLogout());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Test
    public void isValidIpAddress() {
        assertTrue(BackendController.isValidIpAddress(SERVER_IP));
        assertFalse(BackendController.isValidIpAddress("31r.213.23.21"));
    }

    @Test
    public void isValidPortNumber() {
        assertFalse(BackendController.isValidPortNumber(1023));
        assertTrue(BackendController.isValidPortNumber(SERVER_PORT));
        assertFalse(BackendController.isValidPortNumber(65536));
    }
}
