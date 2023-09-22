package com.dev.vault.ProjectService.util;

import com.dev.vault.ProjectService.feign.client.AuthUserFeignClient;
import com.dev.vault.ProjectService.model.dto.UserMembersDto;
import com.dev.vault.ProjectService.model.entity.Project;
import com.dev.vault.ProjectService.model.entity.ProjectMembers;
import com.dev.vault.ProjectService.model.entity.UserProjectRole;
import com.dev.vault.ProjectService.repository.ProjectMembersRepository;
import com.dev.vault.ProjectService.repository.UserProjectRoleRepository;
import com.dev.vault.shared.lib.model.dto.RolesDTO;
import com.dev.vault.shared.lib.model.dto.UserDTO;
import com.dev.vault.shared.lib.model.enums.Role;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Primary implementation of ProjectUtils for regular project membership checks.
 */
@Primary
@Service
@RequiredArgsConstructor
@Slf4j
public class ProjectUtilsImpl implements ProjectUtils {

    private final UserProjectRoleRepository userProjectRoleRepository;
    private final ProjectMembersRepository projectMembersRepository;
    private final AuthUserFeignClient authUserFeignClient;

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isLeaderOrAdminOfProject(Project project, UserDTO userDTO) {
        // Find the user's role
        RolesDTO leaderOrAdminRole = userDTO.getRoles().stream()
                .filter(roles ->
                        roles.getRole().equals(Role.PROJECT_LEADER) || roles.getRole().equals(Role.PROJECT_ADMIN)
                ).findFirst()
                .orElseGet(() -> {
                    log.error("😖 Oops... You ({}) are NOT a project `LEADER or ADMIN` 😖", userDTO.getUserId());
                    return new RolesDTO(4L, Role.NOT_ALLOWED);
                });

        // Find the user's role FOR THE SPECIFIED project
        Optional<UserProjectRole> userProjectRole
                = userProjectRoleRepository.findByUserIdAndRoleIdAndProject(userDTO.getUserId(), leaderOrAdminRole.getRoleId(), project);

        // Return true if the user has the leader or admin role IN THE project, false otherwise
        return userProjectRole.isPresent() &&
                userProjectRole.get().getRoleId().equals(leaderOrAdminRole.getRoleId());
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isMemberOfProject(Project project, UserDTO userDTO) {
        Optional<ProjectMembers> members = projectMembersRepository.findByProject_ProjectNameAndUserId(project.getProjectName(), userDTO.getUserId());
        return members.isPresent();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public List<UserMembersDto> getUserDtoList(Project project) {
        // Get all project members associated with the given project
        List<ProjectMembers> members = projectMembersRepository.findByProject(project);

        // Create a list of UserDto objects for the project members
        ArrayList<UserMembersDto> userDtos = new ArrayList<>();
        for (ProjectMembers projectMembers : members) {
            UserDTO memberUserDTO = authUserFeignClient.getUserDTOById(projectMembers.getUserId());

            UserMembersDto userDto = UserMembersDto.builder()
                    .username(memberUserDTO.getUsername())
                    .major(memberUserDTO.getMajor())
                    .education(memberUserDTO.getEducation())
                    .role(memberUserDTO.getRoles()
                            .stream().map(roles -> roles.getRole().name()).toList()
                    ).build();
            userDtos.add(userDto);
        }
        return userDtos;
    }

}
