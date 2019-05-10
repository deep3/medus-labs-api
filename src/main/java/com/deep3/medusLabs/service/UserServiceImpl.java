package com.deep3.medusLabs.service;

import com.deep3.medusLabs.aws.exceptions.InvalidPasswordComplexity;
import com.deep3.medusLabs.aws.exceptions.ObjectNotFoundException;
import com.deep3.medusLabs.model.User;
import com.deep3.medusLabs.repository.UserRepository;
import org.apache.commons.collections4.IteratorUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import java.util.List;

@Repository
@Service("userService")
public class UserServiceImpl implements UserService
{
    private UserRepository userRepository;

    @Autowired
    public UserServiceImpl(UserRepository userRepository)
    {
        this.userRepository = userRepository;
    }

    /**
     * Find A User in the local data store by querying it's ID
     * @param id The ID of the User to find
     * @return A User object
     *  @throws ObjectNotFoundException Thrown if supplied 'id' does not match an entity
     *  */
    @Override
    public User findOne(Long id) throws ObjectNotFoundException {

        try {
            User user = userRepository.findById(id).orElse(null);

            if (user == null) {
                throw new ObjectNotFoundException(User.class, "id", id.toString());
            }
            return user;
        }
        catch (Exception ex)
        {
            throw new ObjectNotFoundException(User.class, "id", id.toString());
        }
    }

    /**
     * Get A List of Users from the local data store
     * @return A List of User objects
     */
    @Override
    public List<User> findAll()
    {
        return IteratorUtils.toList(userRepository.findAll().iterator());
    }

    /**
     * Find A User in the local data store by querying it's username
     * @param username The Username of the User to find
     * @return A User object
     */
    @Override
    public User findByUsername(String username) throws ObjectNotFoundException {
        try {
            return userRepository.findByUsername(username);
        }
        catch (Exception e)
        {
            throw new ObjectNotFoundException(User.class, "username", username);
        }
    }

    /**
     * Save a user into the local data store
     * @param user The User object to Save
     * @return A User Object
     * @throws InvalidPasswordComplexity - Thrown if supplied password does not match complexity requirements
     */
    @Override
    public User save(User user) throws InvalidPasswordComplexity {
        try {
            return userRepository.save(user);
        }
        catch (Exception e) {
            throw new InvalidPasswordComplexity();
        }
    }

    /**
     * Deletes a local user from the data store
     * @param userId The ID of the User to delete
     * @throws ObjectNotFoundException Thrown if a user by the given ID has not been found.
     */
    @Override
    public void delete(long userId) throws ObjectNotFoundException {
        userRepository.delete(findOne(userId));
    }

    /**
     * Modify an existing User in the local data store
     * @param user The Modified User object
     * @return A User Object
     * @throws InvalidPasswordComplexity -hrown if supplied password does not match complexity requirements
     * @throws ObjectNotFoundException Thrown if a user by the given ID has not been found.
     */
    @Override
    public User modify(User user) throws InvalidPasswordComplexity, ObjectNotFoundException {
        User toUpdate;
        String messages;

        try {
            toUpdate = findOne(user.getId());
        }
        catch (Exception e){
            throw new ObjectNotFoundException(User.class, messages = "User not found");
        }

        try {
            toUpdate = user;
            return userRepository.save(toUpdate);

        } catch (Exception e) {
            throw new InvalidPasswordComplexity();
        }
    }
}
