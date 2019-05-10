package com.deep3.medusLabs.controller;

import com.deep3.medusLabs.aws.exceptions.InvalidPasswordComplexity;
import com.deep3.medusLabs.aws.exceptions.ObjectNotFoundException;
import com.deep3.medusLabs.model.APIResponse;
import com.deep3.medusLabs.model.User;
import com.deep3.medusLabs.service.UserServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class UserController {

    private UserServiceImpl userService;

    @Autowired
    public UserController(UserServiceImpl userService)
    {
        this.userService = userService;
    }

    /**
     * Get All users from the local data source.
     * @return - A Response Entity containing an APIResponse.
     */
    @GetMapping(path = ("/"), produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<APIResponse> getAllUsers() {
        APIResponse response = new APIResponse<>(HttpStatus.OK, userService.findAll());
        return new ResponseEntity<>(response, response.getHttpStatus());
    }

    /**
     * Get A user from the local data source by querying using the user ID.
     * @return - A Response Entity containing an APIResponse.
     */
    @GetMapping(path = ("/{id}"), produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<APIResponse> getUser(@PathVariable("id") long id) throws ObjectNotFoundException {
        APIResponse response = new APIResponse<>(HttpStatus.OK, userService.findOne(id));
        return new ResponseEntity<>(response, response.getHttpStatus());
    }

     /**
     * Create a new user in the local data store.
     * @return - A Response Entity containing an APIResponse.
     */
    @PostMapping(path = ("/"), produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<APIResponse> createUser(@RequestBody User user) throws InvalidPasswordComplexity {
        APIResponse response = new APIResponse<>(HttpStatus.CREATED, userService.save(user));
        return new ResponseEntity<>(response, response.getHttpStatus());
    }

    /**
     * Modify an existing user in the local data store.
     * @return - A Response Entity containing an APIResponse.
     */
    @PutMapping(path = ("/"), produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<APIResponse> modifyUser(@RequestBody User user) throws ObjectNotFoundException, InvalidPasswordComplexity {
        APIResponse response = new APIResponse<>(HttpStatus.OK, userService.modify(user));
        return new ResponseEntity<>(response, response.getHttpStatus());
    }

    /**
     * Delete a User from the the local data store.
     * @param id - The id of the User to delete.
     * @return - A Response Entity containing an APIResponse.
     */
    @DeleteMapping(value = "/{id}")
    public ResponseEntity<APIResponse> deleteUser(@PathVariable("id") long id) throws ObjectNotFoundException {
        userService.delete(id);
        APIResponse response = new APIResponse(HttpStatus.OK);
        return new ResponseEntity<>(response, response.getHttpStatus());
    }

    /**
     * Check if a user exists in the local data store and returns the relative response.
     * @return - A Response Entity containing an APIResponse.
     */
    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<APIResponse> getUserExists(@RequestParam(value="username") String username) throws ObjectNotFoundException {
        APIResponse response = new APIResponse<>(HttpStatus.OK, userService.findByUsername(username));
        return new ResponseEntity<>(response, response.getHttpStatus());
    }
}