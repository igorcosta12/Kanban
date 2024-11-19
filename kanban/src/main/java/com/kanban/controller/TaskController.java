package com.kanban.controller;

import com.kanban.domain.Priority;
import com.kanban.domain.Task;
import com.kanban.domain.TaskStatus;
import com.kanban.dto.TaskDTO;
import com.kanban.services.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/tasks")
public class TaskController {
    private final TaskService service;
    @Autowired
    public TaskController(TaskService taskService) {
        this.service = taskService;
    }

    @GetMapping
    public List<Task> getAllGrouped() {
        return service.getAllGrouped();
    }

    @GetMapping("/pending")
    public List<Task> getAllPending() {
        return service.getTasksByStatus(TaskStatus.PENDING);
    }

    @GetMapping("/progress")
    public List<Task> getAllProgress() {
        return service.getTasksByStatus(TaskStatus.IN_PROGRESS);
    }

    @GetMapping("/completed")
    public List<Task> getAllCompleted() {
        return service.getTasksByStatus(TaskStatus.COMPLETED);
    }

    @PostMapping
    public ResponseEntity<?> createTask(@RequestBody TaskDTO taskDTO) {
        Task task = new Task();

        //título (obrigatório)
        if(taskDTO.getTitle()!=null){
            task.setTitle(taskDTO.getTitle());
        } else {
            return ResponseEntity.badRequest().body("O título é obrigatório!");
        }

        //descrição (opcional)
        if (taskDTO.getDescription()!=null) {task.setDescription(taskDTO.getDescription());}

        //data limite (opcional)
        if (taskDTO.getDueDate()!=null) {task.setDue_limit_date(taskDTO.getDueDate());}

        //status (automático)
        task.setStatus(TaskStatus.PENDING);

        //prioridade (obrigatório)
        if(taskDTO.getPriority()!=null){
            task.setPriority(Priority.valueOf(taskDTO.getPriority().toUpperCase()));
        } else {
            return ResponseEntity.badRequest().body("Informe a prioridade!");
        }

        service.save(task);

        return ResponseEntity.ok("Tarefa criada com sucesso!\n" + task.toString());
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateTask(@PathVariable Long id, @RequestBody TaskDTO taskDTO) {
        Optional<Task> optionalTask = Optional.ofNullable(service.findById(id));

        //existe?
        if (optionalTask.isEmpty()){return ResponseEntity.notFound().build();}

        Task task = optionalTask.get();

        if (taskDTO.getTitle() != null) {
            task.setTitle(taskDTO.getTitle());
        }

        if (taskDTO.getDescription() != null) {
            task.setDescription(taskDTO.getDescription());
        }

        if (taskDTO.getDueDate() != null) {
            task.setDue_limit_date(taskDTO.getDueDate());
        }

        if (taskDTO.getPriority() != null) {
            task.setPriority(Priority.valueOf(taskDTO.getPriority().toUpperCase()));
        }

        service.save(task);

        return ResponseEntity.ok("Tarefa atualizada com sucesso!\n" + task.toString());
    }

    @PutMapping("/{id}/move")
    public ResponseEntity<?> moveTask(@PathVariable Long id) {
        Optional<Task> optionalTask = Optional.ofNullable(service.findById(id));

        if (optionalTask.isEmpty()){return ResponseEntity.notFound().build();}

        Task task = optionalTask.get();

        task.setStatus(service.nextStatus(task.getStatus()));

        service.save(task);

        return ResponseEntity.ok("Tarefa alterada para: " + task.getStatus() + "\n" + task.toString());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTask(@PathVariable Long id) {
        Optional<Task> optionalTask = Optional.ofNullable(service.findById(id));
        if (optionalTask.isEmpty()){return ResponseEntity.notFound().build();}
        Task task = optionalTask.get();
        service.delete(task.getId());
        return ResponseEntity.ok("Tarefa deletada com sucesso!\n" + task.toString());
    }
}
