package com.afarms.user.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.afarms.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class UserServiceTest {

    @Test
    void shouldCreateServiceImpl() {
        UserRepository repository = Mockito.mock(UserRepository.class);
        UserServiceImpl service = new UserServiceImpl(repository);
        assertNotNull(service);
    }
}
