package com.dev.vault.TaskService.util;

import com.dev.vault.TaskService.fegin.client.AuthUserFeignClient;
import com.dev.vault.TaskService.fegin.client.ProjectUtilFeignClient;
import com.dev.vault.TaskService.model.entity.Task;
import com.dev.vault.TaskService.model.request.TaskRequest;
import com.dev.vault.TaskService.model.response.TaskResponse;
import com.dev.vault.TaskService.repository.TaskRepository;
import com.dev.vault.TaskService.repository.TaskUserRepository;
import com.dev.vault.shared.lib.model.dto.ProjectDTO;
import com.dev.vault.shared.lib.model.dto.UserDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * A utility class that provides helper methods for working with tasks.
 * This class contains method for checking if a task with the same name already exists in a project. //TODO
 */
@Service
@RequiredArgsConstructor
public class TaskUtils {

    private final TaskRepository taskRepository;
    private final AuthUserFeignClient authFeignClient;
    private final ProjectUtilFeignClient projectFeignClient;
    private final TaskUserRepository taskUserRepository;

    /**
     * Checks if a task with the same name already exists in the project
     *
     * @param taskRequest the request object containing the details of the task to create
     * @param projectId   the project to check for existing tasks
     * @return true if a task with the same name already exists in the project, false otherwise
     */
    public boolean doesTaskAlreadyExists(TaskRequest taskRequest, Long projectId) {
        Optional<Task> foundTask = taskRepository.findByProjectIdAndTaskName(projectId, taskRequest.getTaskName());
        return foundTask.isPresent();
    }


//    /**
//     * Loops through the list of user IDs and assigns the task to each user.
//     *
//     * @param task       the task to assign
//     * @param projectId  the ID of the project the task belongs to
//     * @param userIdList the list of user IDs to assign the task to
//     */
//    public void assignTaskToUserList(
//            Task task, Long projectId,
//            List<Long> userIdList,
//            Map<String, String> statusResponseMap
//    ) {
//        for (Long userId : userIdList) {
//            // Find the user by ID or throw a RecourseNotFoundException if it doesn't exist
//            UserDTO userDTO = authFeignClient.getUserDTOById(userId);
//
//            // Check if the user is a member of the project, and add a response to the map if they're not
//            if (!projectFeignClient.isMemberOfProject(projectId, userDTO)) {
//                statusResponseMap.put(userDTO.getUsername(), "Fail: User with ID " + userId + " is not a member of project with ID " + projectId);
//                continue;
//            }
//
//            // Check if the task is already assigned to the user skip ahead, and add a response to the map
//            if (taskRepository.findByAssignedUsersAndTaskId(Set.of(userDTO), task.getTaskId()).isPresent()) {
//                statusResponseMap.put(userDTO.getUsername(), "Fail: Task already assigned to user " + userDTO.getUsername());
//                continue;
//            }
//
//            // Assign the user to the task, build a task_user object for managing relationship and add a response to the map
//            task.getAssignedUserIDs().add(userDTO);
//
//            TaskUser taskUser = TaskUser.builder()
//                    .task(task)
//                    .userId(userDTO.getUserId())
//                    .build();
//            taskUserRepository.save(taskUser);
//
//            statusResponseMap.put(userDTO.getUsername(), "Success: Task assigned to user " + userDTO.getUsername());
//
//            // Set the assigned users for the task and save the task
//            task.setAssignedUserIDs(task.getAssignedUserIDs());
//            taskRepository.save(task);
//
//        }
//    }
//
//
//    /**
//     * Builds a TaskResponse object with information about the assigned task and its assigned users.
//     *
//     * @param task      the assigned task
//     * @param projectId the project the task belongs to
//     * @param map       the map of responses for each assigned user
//     * @return a TaskResponse object with information about the assigned task and its assigned users
//     */
//    public TaskResponse buildTaskResponse(
//            Task task,
//            long projectId,
//            Map<String, String> map
//    ) {
//        TaskResponse taskResponse = new TaskResponse();
//        taskResponse.setTaskName(task.getTaskName());
//        taskResponse.setTaskStatus(task.getTaskStatus());
//        taskResponse.setDueDate(task.getDueDate());
//        taskResponse.setProjectName(projectFeignClient.getProjectAsDTO(projectId).getProjectName());
//        taskResponse.setAssignedUsers(map);
//        return taskResponse;
//    }
//
//
//    /**
//     * Validates whether the task belongs to the project and whether the user is a member and leader/admin of the project.
//     *
//     * @param task      the task to validate
//     * @param projectId the project to validate against
//     * @param userId    the user to validate
//     * @throws DevVaultException           if the task does not belong to the project
//     * @throws NotMemberOfProjectException if the user is not a member of the project
//     * @throws NotLeaderOfProjectException if the user is not the leader or admin of the project
//     */
//    public void validateTaskAndProject(Task task, long projectId, long userId) {
//        // Check if the task belongs to the project or throw a DevVaultException if it doesn't
//        if (!task.getProjectId().equals(projectId))
//            throw new DevVaultException(
//                    "😖 Task {" + task.getTaskName() + "} does not belong to project {" + projectId + "} 😖",
//                    EXPECTATION_FAILED,
//                    EXPECTATION_FAILED.value()
//            );
//
//        // Check if the user is a member of the project or throw a NotMemberOfProjectException if they aren't
//        UserDTO userDTO = authFeignClient.getUserDTOById(userId);
//        if (!projectFeignClient.isMemberOfProject(projectId, userDTO))
//            throw new NotMemberOfProjectException(
//                    "Ⓜ️👥 You are not a member of this project 👥Ⓜ️",
//                    FORBIDDEN,
//                    FORBIDDEN.value()
//            );
//
//        // Check if the user is the leader or admin of the project or throw a NotLeaderOfProjectException if they aren't
//        if (!projectFeignClient.isLeaderOrAdminOfProject(projectId, userId))
//            throw new NotLeaderOfProjectException(
//                    "👮🏻You are not the leader or admin of this project👮🏻",
//                    FORBIDDEN,
//                    FORBIDDEN.value()
//            );
//    }
//
//
    /**
     * Builds a TaskResponse object with information about the newly created task.
     *
     * @param task the assigned task
     * @return a TaskResponse object with information about the newly created task
     */
    public TaskResponse buildTaskResponse_ForTaskCreation(Task task) {
        ProjectDTO project = projectFeignClient.getProjectAsDTO(task.getProjectId());

        Map<String, String> assignededUsersMap = new HashMap<>();
        task.getAssignedUsers().forEach(taskUser -> {
            UserDTO assignedUser = authFeignClient.getUserDTOById(taskUser.getTaskUserId());
            assignededUsersMap.put(assignedUser.getUserId().toString(), assignedUser.getUsername());
        });

        /*
            Map<String, String> assignedUsersMap = new HashMap<>();
        if (task.getAssignedUsers().isEmpty()) {
            UserDTO assignedUser = authFeignClient.getUserDTOById(task.getCreatedByUserId());
            TaskUser taskUser = TaskUser.builder()
                    .task(task)
                    .userId(assignedUser.getUserId())
                    .build();

            task.setAssignedUsers(List.of(taskUser));
        } else {
            task.getAssignedUsers().forEach(taskUser -> {
                UserDTO assignedUser = authFeignClient.getUserDTOById(taskUser.getTaskUserId());
                assignedUsersMap.put(assignedUser.getUserId().toString(), assignedUser.getUsername());
            });
        }
        */

        return TaskResponse.builder()
                .taskName(task.getTaskName())
                .projectName(project.getProjectName())
                .taskStatus(task.getTaskStatus())
                .dueDate(task.getDueDate())
                .assignedUsers(assignededUsersMap)
                .build();
    }
//
//
//    /**
//     * Retrieves a set of users associated with a task and a project, and updates the statusResponseMap with the status of the assignment for each user.
//     *
//     * @param task              The task to assign.
//     * @param projectId         The project to which the task belongs.
//     * @param statusResponseMap The map to which the status of the assignment for each user will be added.
//     * @return A set of users associated with the task and the project.
//     */
//    public Set<UserDTO> getUsers(
//            Task task,
//            long projectId,
//            Map<String, String> statusResponseMap
//    ) {
//        return projectMembersRepository.findByProject(projectId)
//                .stream().map(projectMembers -> {
//                    User user = repositoryUtils.findUserById_OrElseThrow_ResourceNoFoundException(projectMembers.getUser().getUserId());
//                    // Check if the task is already assigned to the user, skip ahead and add a response to the map if it is
//                    if (taskRepository.findByAssignedUsersAndTaskId(user, task.getTaskId()).isPresent())
//                        statusResponseMap.put(user.getUsername(), "Fail: Task already assigned to user " + user.getUsername());
//                    else statusResponseMap.put(user.getUsername(), "Success");
//                    user.getTask().add(task);
//
//                    return userRepository.save(user);
//                }).collect(Collectors.toSet());
//    }

}