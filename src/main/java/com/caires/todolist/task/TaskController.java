package com.caires.todolist.task;

import com.caires.todolist.utils.Utils;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/tasks")
public class TaskController {

    private final ITaskRepository iTaskRepository;

    public TaskController(ITaskRepository iTaskRepository){
        this.iTaskRepository = iTaskRepository;
    }

    @PostMapping("/")
    ResponseEntity create(@RequestBody TaskModel taskModel, HttpServletRequest request){
        taskModel.setIdUser((UUID) request.getAttribute("idUser"));

        var currentDate = LocalDateTime.now();
        if (currentDate.isAfter(taskModel.getStartAt()) || currentDate.isAfter(taskModel.getEndAt())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("A data de inicio/término deve ser maior do que a data atual");
        }

        if (taskModel.getStartAt().isAfter(taskModel.getEndAt())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("A data de inicio não deve ser maior do que a data final");
        }

        return ResponseEntity.status(HttpStatus.OK).body(this.iTaskRepository.save(taskModel));
    }

    @GetMapping("/")
    List<TaskModel> list(HttpServletRequest request){
        return this.iTaskRepository.findByIdUser((UUID) request.getAttribute("idUser"));
    }

    @PutMapping("/{id}")
    ResponseEntity update(@RequestBody TaskModel taskModel, HttpServletRequest request, @PathVariable UUID id) {
        var task = this.iTaskRepository.findById(id).orElse(null);
        if (task == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Tarefa não encontrada");
        }

        var idUser = request.getAttribute("idUser");
        if (!task.getIdUser().equals(idUser)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Usuário não tem permissão para alterar essa tarefa");
        }

        Utils.copyNonNullProprties(taskModel, task);

        return ResponseEntity.status(HttpStatus.OK).body(this.iTaskRepository.save(task));
    }

    @DeleteMapping("/{id}")
    ResponseEntity delete(HttpServletRequest request, @PathVariable UUID id) {
        var task = this.iTaskRepository.findById(id).orElse(null);
        if (task == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Tarefa não encontrada");
        }

        var idUser = request.getAttribute("idUser");
        if (!task.getIdUser().equals(idUser)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Usuário não tem permissão para apagar essa tarefa");
        }
        this.iTaskRepository.delete(task);

        return ResponseEntity.status(HttpStatus.OK).body("Tarefa deletada");
    }
}
