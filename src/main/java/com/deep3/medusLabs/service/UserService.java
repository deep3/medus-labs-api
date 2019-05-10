package com.deep3.medusLabs.service;

import com.deep3.medusLabs.aws.exceptions.InvalidPasswordComplexity;
import com.deep3.medusLabs.aws.exceptions.ObjectNotFoundException;
import com.deep3.medusLabs.model.User;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface UserService {

    User findOne(Long id) throws ObjectNotFoundException;

    List<User> findAll();

    User findByUsername(String username) throws ObjectNotFoundException;

    User save(User user) throws InvalidPasswordComplexity;

    void delete (long id) throws ObjectNotFoundException;;

    User modify(User user) throws InvalidPasswordComplexity, ObjectNotFoundException;
}
