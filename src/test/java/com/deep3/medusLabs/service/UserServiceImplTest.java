package com.deep3.medusLabs.service;

import com.deep3.medusLabs.aws.exceptions.InvalidPasswordComplexity;
import com.deep3.medusLabs.aws.exceptions.ObjectNotFoundException;
import com.deep3.medusLabs.model.User;
import com.deep3.medusLabs.repository.UserRepository;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;


@RunWith(SpringJUnit4ClassRunner.class)
public class UserServiceImplTest {

    @Mock
    User userTest;

    @Mock
    UserRepository userRepository;

    @Before
    public void initialize()
    {
        MockitoAnnotations.initMocks(this);
    }


    @Test
    public void testFindOne() throws ObjectNotFoundException {

        UserServiceImpl userServiceImpl = new UserServiceImpl(userRepository);

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(userTest));

        User user = userServiceImpl.findOne(anyLong());

        Assert.assertNotNull(user);
    }

    @Test
    public void testFindByUserNameNotNull() throws ObjectNotFoundException {

        UserServiceImpl userServiceImpl = new UserServiceImpl(userRepository);

        when(userServiceImpl.findByUsername(anyString())).thenReturn(userTest);

        User user = userServiceImpl.findByUsername(anyString());

        Assert.assertNotNull(user);

    }

    @Test
    public void testFindByUserNameNull() throws ObjectNotFoundException {

        UserServiceImpl userServiceImpl = new UserServiceImpl(userRepository);

        User user = userServiceImpl.findByUsername("");

        Assert.assertNull(user);

    }

    @Test
    public void testFindAllNotNull() {
        UserServiceImpl userServiceImpl = new UserServiceImpl(userRepository);

        when(userServiceImpl.findAll()).thenReturn(Collections.singletonList(userTest));

        List<User> userList = userServiceImpl.findAll();

        Assert.assertNotNull(userList);
    }

    @Test
    public void testFindAllNull() {
        UserServiceImpl userServiceImpl = new UserServiceImpl(userRepository);

        when(userServiceImpl.findAll()).thenReturn(Collections.emptyList());

        List<User> userList = userServiceImpl.findAll();

        Assert.assertTrue(userList.isEmpty());
    }

    @Test
    public void testSaveUser() throws InvalidPasswordComplexity {
        UserServiceImpl userServiceImpl = new UserServiceImpl(userRepository);

        User testUser = new User ("Test-Username", "Test-Password1");
        when(userServiceImpl.save(testUser)).thenReturn(testUser);

        User user = userServiceImpl.save(testUser);

        Assert.assertNotNull(user);
    }

    @Test
    public void testModify() throws InvalidPasswordComplexity, ObjectNotFoundException {
        UserServiceImpl userServiceImpl = new UserServiceImpl(userRepository);

        User testUserStored = new User (1l, "Test-Username", "Test-Password");
        User testUserModified = new User (1l, "Test-Username-Modified", "Test-Password-Modified");

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(testUserStored));
        when(userServiceImpl.modify(testUserModified)).thenReturn(testUserModified);

        User user = userServiceImpl.modify(testUserModified);

        Assert.assertEquals(user, testUserModified);

    }

    @Test
    public void testDelete() throws ObjectNotFoundException {

        UserServiceImpl userServiceSpy = Mockito.spy(new UserServiceImpl(userRepository));

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(userTest));

        userServiceSpy.delete(anyLong());

        verify(userServiceSpy, atLeast(1)).delete(anyLong());
    }

}