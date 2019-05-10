package com.deep3.medusLabs.security.service;

import com.deep3.medusLabs.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AccountStatusUserDetailsChecker;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import static java.util.Collections.emptyList;

@Service("userDetailsService")
public class UserAccountService implements UserDetailsService {

    private UserRepository accountRepository;
    private final AccountStatusUserDetailsChecker detailsChecker = new AccountStatusUserDetailsChecker();

    @Autowired
    public UserAccountService(UserRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        // Fetch the account corresponding to the given username
        com.deep3.medusLabs.model.User user = accountRepository.findByUsername(username);

        // If the account doesn't exist
        if (user == null) {
            throw new UsernameNotFoundException("User " + username + " not found");
        }

        User userDetail = new User(user.getUsername(), user.getPassword(), true, true, true, true, emptyList());

        // Use built in Spring class to check if the user account is locked/disabled/expired etc
        // throws an exception if there is an issue - caught by the service
        detailsChecker.check(userDetail);

        return userDetail;
    }

}