package com.caires.todolist.user;

import at.favre.lib.crypto.bcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
public class UserController {

    private final IUserRepository iUserRepository;

    public UserController(IUserRepository iUserRepository){
        this.iUserRepository = iUserRepository;
    }

    @PostMapping("/")
    ResponseEntity create(@RequestBody UserModel userModel){
        var user = this.iUserRepository.findByUsername(userModel.getUsername());
        if (user != null){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Usuario já existe");
        }
        // Criptografando senha para armazenar no banco
        var passwordHashred = BCrypt.withDefaults()
                .hashToString(12, userModel.getPassword().toCharArray());
        userModel.setPassword(passwordHashred);
        return ResponseEntity.status(HttpStatus.CREATED).body(this.iUserRepository.save(userModel));
    }
}
