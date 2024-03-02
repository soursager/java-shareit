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
import ru.practicum.shareit.validator.RequestValidator;
import ru.practicum.shareit.validator.UserValidator;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.stream.Collectors;

import static ru.practicum.shareit.request.dto.ItemRequestMapper.*;


@Service
@RequiredArgsConstructor
public class ItemRequestServicelmpl implements ItemRequestService {
    private final RequestRepository requestRepository;
    private final UserRepository userRepository;
    private final UserValidator userValidator;
    private final RequestValidator requestValidator;

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
        userValidator.checkingUserIdAndNotReturns(userId);
        return requestRepository.findAllByRequester_Id(userId).stream()
                .map(ItemRequestMapper::toItemRequestDto)
                .collect(Collectors.toList());
    }

    @Override
    public Collection<ItemRequestDto> getAllRequestsToResponse(Long userId, Pageable page) {
        userValidator.checkingUserIdAndNotReturns(userId);
        return requestRepository.findAllByAllOtherUsers(userId, page).stream()
                .map(ItemRequestMapper::toItemRequestDto)
                .collect(Collectors.toList());
    }

    @Override
    public ItemRequestDto getRequestById(Long userId, Long requestId) {
        userValidator.checkingUserIdAndNotReturns(userId);
        ItemRequest request = requestValidator.validateItemRequestIdAndReturns(requestId);
        return toItemRequestDto(request);
    }
}
