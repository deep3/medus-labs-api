package com.deep3.medusLabs.security;

import com.deep3.medusLabs.security.service.UserAccountService;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.mockito.Mockito.*;

@RunWith(SpringJUnit4ClassRunner.class)
public class TokenHandlerTest {

    @Mock
    UserAccountService userAccountService;

    @After
    public void validate() {
        validateMockitoUsage();
    }

    @Test
    public void testBuildToken() {
        TokenHandler tokenHandler = new TokenHandler(userAccountService);

        tokenHandler.TOKEN_SECRET = "ASECRETKEY";

        String result = tokenHandler.buildToken(RandomStringUtils.random(10, "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789" ));

        Assert.assertNotNull(result);
    }

    @Test
    public void testBuildTokenEnsureDistinct() {
        TokenHandler tokenHandler = new TokenHandler(userAccountService);
        tokenHandler.TOKEN_SECRET = "ASECRETKEY";
        List<String> test = new ArrayList();

        for (int i = 0; i < 100; i++){
            test.add(tokenHandler.buildToken(RandomStringUtils.random(10, "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789" )));
        }

        Assert.assertEquals(test.stream().distinct().collect(Collectors.toList()).size(), test.size());
    }

}
