package ru.practicum.shareit.request.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.RequestRepository;
import ru.practicum.shareit.user.model.User;
import  ru.practicum.shareit.request.dto.ItemRequestMapper;
import ru.practicum.shareit.user.repository.UserRepository;
import ru.practicum.shareit.validator.RequestValidatorService;
import ru.practicum.shareit.validator.UserValidatorService;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.stream.Collectors;

import static ru.practicum.shareit.request.dto.ItemRequestMapper.*;


@Service
@RequiredArgsConstructor
public class ItemRequestServicelmpl implements ItemRequestService {
    private final RequestRepository requestRepository;
    private final UserRepository userRepository;
    private final UserValidatorService userValidator;
    private final RequestValidatorService requestValidator;

    @Override
    public ItemRequestDto addNewRequest(ItemRequestDto requestDto, Long userId) {
        User requester = userValidator.returnUserIfExists(userId);
        requestDto.setCreated(LocalDateTime.now());
        requestValidator.validateItemRequestData(requestDto);
        return toItemRequestDto(requestRepository.save(ItemRequestMapper.toItemRequest(requestDto,
                requester)));
    }

    @Override
    public Collection<ItemRequestDto> getAllUserRequestsWithResponses(Long userId) {
        userValidator.checkingUserIdAndNotReturn(userId);
        return requestRepository.findAllByRequester_Id(userId).stream()
                .map(ItemRequestMapper::toItemRequestDto)
                .collect(Collectors.toList());
    }

    @Override
    public Collection<ItemRequestDto> getAllRequestsToResponse(Long userId, Pageable page) {
        userValidator.checkingUserIdAndNotReturn(userId);
        return requestRepository.findAllByAllOtherUsers(userId, page).stream()
                .map(ItemRequestMapper::toItemRequestDto)
                .collect(Collectors.toList());
    }

    @Override
    public ItemRequestDto getRequestById(Long userId, Long requestId) {
        userValidator.checkingUserIdAndNotReturn(userId);
        ItemRequest request = requestValidator.validateItemRequestIdAndReturn(requestId);
        return toItemRequestDto(request);
    }
}
