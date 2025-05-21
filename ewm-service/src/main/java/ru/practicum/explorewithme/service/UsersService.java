package ru.practicum.explorewithme.service;

import ru.practicum.explorewithme.dto.user.NewUserRequest;
import ru.practicum.explorewithme.dto.user.UserDto;

import java.util.ArrayList;
import java.util.List;

public interface UsersService {

    List<UserDto> getUsers(ArrayList<Long> ids, Integer from, Integer size);

    UserDto createUser(NewUserRequest newUserRequest);

    void deleteUser(Long userId);
}
