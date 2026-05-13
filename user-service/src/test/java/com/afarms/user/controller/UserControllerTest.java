package com.afarms.user.controller;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;

class UserControllerTest {

    @Test
    void shouldCreateController() {
        UserService service = mock(UserService.class);
        UserController controller = new UserController(service);
        assertNotNull(controller);
    }
}
