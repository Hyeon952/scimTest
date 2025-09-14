package ai.duclo.scimtest.controller;

import ai.duclo.scimtest.model.User;
import ai.duclo.scimtest.service.ScimService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/scim/v1")
public class ScimController {
    private final ScimService scimService;

    public ScimController(ScimService scimService) {
        this.scimService = scimService;
    }

    @PostMapping("/Users")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<User> createUser(@RequestBody User user) {
        return scimService.processUserCreation(user);
    }

    @PutMapping("/Users/{id}")
    public Mono<User> updateUser(@PathVariable String id, @RequestBody User user) {
        return scimService.processUserUpdate(id, user);
    }

    @DeleteMapping("/Users/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> deleteUser(@PathVariable String id) {
        return scimService.processUserDeletion(id);
    }
}
