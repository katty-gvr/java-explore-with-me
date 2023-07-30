package ru.practicum.user.service;

import ru.practicum.user.dto.UserDto;

import java.util.Collection;
import java.util.List;

public interface UserService {

    UserDto addUser(UserDto userDto);

    void deleteUser(Long id);

    Collection<UserDto> getAllUsers(List<Long> ids, Integer from, Integer size);
}
