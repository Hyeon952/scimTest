package ai.duclo.scimtest.controller;

import ai.duclo.scimtest.model.ListResponseDTO;
import ai.duclo.scimtest.model.UrnIetfParamsEnum;
import ai.duclo.scimtest.model.User;
import ai.duclo.scimtest.service.ScimService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.lang.reflect.Field;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/scim/v1")
public class ScimController {
    private final ScimService scimService;

    public ScimController(ScimService scimService) {
        this.scimService = scimService;
    }

    @GetMapping("/Users")
    @ResponseStatus(HttpStatus.OK)
    public Mono<Object> getUser() {
        ListResponseDTO<User> responseDTO = new ListResponseDTO<>();
        responseDTO.setSchemas(List.of(UrnIetfParamsEnum.LIST_RESPONSE.getValue()));
        responseDTO.setTotalResults(0);
        responseDTO.setStartIndex(1);
        responseDTO.setItemsPerPage(0);
        responseDTO.setResources(List.of());
        return Mono.just(responseDTO);

//        ErrorResponseDTO errorResponseDTO = new ErrorResponseDTO();
//        errorResponseDTO.setStatus(HttpStatus.NOT_FOUND.value());
//        errorResponseDTO.setDetail("No matching resource found");
//        errorResponseDTO.setSchemas(List.of(UrnIetfParamsEnum.ERROR.getValue()));
//        return Mono.just(errorResponseDTO);
    }


    @PostMapping("/Users")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Void> createUser(@RequestBody User user) {
        printObjectFields(user, "User");
//        return scimService.processUserCreation(user);
        return Mono.empty();
    }

    @PutMapping("/Users/{id}")
    public Mono<Void> updateUser(@PathVariable String id, @RequestBody User user) {
        log.info("Received user update request for ID: {}", id);
        printObjectFields(user, "User");
//        return scimService.processUserUpdate(id, user);
        return Mono.empty();
    }

    @DeleteMapping("/Users/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> deleteUser(@PathVariable String id) {
        log.info("Received user delete request for ID: {}", id);
//        return scimService.processUserDeletion(id);
        return Mono.empty();
    }


    // 객체의 모든 필드와 값을 출력하는 유틸리티 메서드
    public static void printObjectFields(Object obj, String className) {
        log.info("Printing fields for class: " + className);
        if (obj == null) {
            log.info("Object is null");
            return;
        }
        Class<?> clazz = obj.getClass();
        log.info("Class: " + clazz.getName());
        while (clazz != null) {
            Field[] fields = clazz.getDeclaredFields();
            for (Field field : fields) {
                field.setAccessible(true); // private 필드 접근 가능
                try {
                    Object value = field.get(obj);
                    log.info(field.getName() + " = " + value);   // <- File Name 확인하여 get 가능 (identity_provider) ignorecase
                } catch (IllegalAccessException e) {
                    log.info(field.getName() + " = [access denied]");
                }
            }
            clazz = clazz.getSuperclass(); // 상위 클래스도 출력
        }
    }
}
