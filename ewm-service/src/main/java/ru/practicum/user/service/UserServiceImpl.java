package ru.practicum.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.exception.NotFoundException;
import ru.practicum.user.dto.UserDto;
import ru.practicum.user.mapper.UserMapper;
import ru.practicum.user.model.User;
import ru.practicum.user.repository.UserRepository;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Override
    @Transactional
    public UserDto addUser(UserDto userDto) {
        User userForSave = UserMapper.toUser(userDto);
        log.info("Зарегистрирован новый пользователь {}", userForSave.getEmail());
        return UserMapper.toUserDto(userRepository.save(userForSave));
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        userRepository.findById(id).orElseThrow(() -> new NotFoundException(String.format("Пользователь с id=%d не найден", id)));
        log.info("Пользователь с id = {} успешно удален", id);
        userRepository.deleteById(id);
    }

    @Override
    public Collection<UserDto> getAllUsers(List<Long> ids, Integer from, Integer size) {
        Pageable page = PageRequest.of(from / size, size, Sort.by("id").ascending());
        Page<User> users = (ids == null || ids.isEmpty())
                ? userRepository.findAll(page)
                : userRepository.findAllByIdIn(ids, page);
        return users.stream().map(UserMapper::toUserDto).collect(Collectors.toList());
    }
}
