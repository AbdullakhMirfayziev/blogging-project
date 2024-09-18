package uz.smartup.academy.bloggingplatform.dto;

import org.springframework.stereotype.Component;
import uz.smartup.academy.bloggingplatform.entity.User;

import java.time.LocalDate;
import java.util.List;

@Component
public class UserDtoUtil {
    public User toEntity(UserDTO userDTO) {
        User user = new User();
        user.setId(userDTO.getId());
        user.setFirstName(userDTO.getFirst_name());
        user.setLastName(userDTO.getLast_name());
        user.setEmail(userDTO.getEmail());
        user.setEnabled(user.getEnabled());
        user.setBio(userDTO.getBio());
        user.setPhoto(userDTO.getPhoto());
        user.setPassword(userDTO.getPassword());
        user.setUsername(userDTO.getUsername());
        user.setRegistered(LocalDate.now());
        user.setRoles(userDTO.getRoles());
        return user;
    }

    public UserDTO toDTO(User user) {
        if (user == null) return null;

        UserDTO userDTO = new UserDTO();
        userDTO.setId(user.getId());
        userDTO.setFirst_name(user.getFirstName());
        userDTO.setLast_name(user.getLastName());
        userDTO.setEmail(user.getEmail());
        userDTO.setPhoto(user.getPhoto());
        userDTO.setBio(user.getBio());
        userDTO.setPassword(user.getPassword());
        userDTO.setUsername(user.getUsername());
        userDTO.setRegistered(user.getRegistered());
        userDTO.setRoles(user.getRoles());

        return userDTO;
    }

    public List<UserDTO> toDTOs(List<User> users) {
        return users.stream().map(this::toDTO).toList();
    }

    public List<UserDTO> toEntities(List<User> users) {
        return users.stream().map(this::toDTO).toList();
    }

    public User userMergeEntity(User user, UserDTO userDTO) {
        //       user.setId(userDTO.getId());
        user.setFirstName(userDTO.getFirst_name());
        user.setLastName(userDTO.getLast_name());
        user.setEmail(userDTO.getEmail());
        user.setPhoto(userDTO.getPhoto());
        user.setBio(userDTO.getBio());
        user.setPassword(userDTO.getPassword());
        user.setUsername(userDTO.getUsername());
//        user.setRegistered(LocalDate.now());
        return user;
    }


}
