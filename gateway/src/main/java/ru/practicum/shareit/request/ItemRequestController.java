package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.validator.PageValidator;
import ru.practicum.shareit.validator.RequestValidator;


/**
 * TODO Sprint add-item-requests.
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/requests")
public class ItemRequestController {
    private static final String USER_ID = "X-Sharer-User-Id";
    private final PageValidator pageValidator;
    private final RequestValidator requestValidator;
    private final RequestClient requestClient;

    @PostMapping
    public ResponseEntity<Object> addNewRequest(@RequestBody ItemRequestDto requestDto,
                                                @RequestHeader(USER_ID) Long requesterId) {
        log.info("Получен POST-запрос к эндпоинту: '/requests' " +
                "на создание запроса вещи от пользователя с ID={}", requesterId);
        requestValidator.validateItemRequestData(requestDto);
        return requestClient.addNewRequest(requesterId, requestDto);
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<Object> getItemRequestById(@PathVariable("requestId") Long requestId,
                                             @RequestHeader(USER_ID) Long userId) {
        log.info("Получен GET-запрос к эндпоинту: '/requests' на получение запроса с ID={}", requestId);
        return requestClient.getRequestById(userId, requestId);
    }

    @GetMapping
    public ResponseEntity<Object> getOwnItemRequests(@RequestHeader(USER_ID) Long userId) {
        log.info("Получен GET-запрос к эндпоинту: '/requests' на получение запросов пользователя ID={}",
                userId);
        return requestClient.getAllUserRequestsWithResponses(userId);
    }

    @GetMapping("/all")
    public ResponseEntity<Object> getAllItemRequests(@RequestHeader(USER_ID) Long userId,
                                                   @RequestParam(defaultValue = "0") Integer from,
                                                   @RequestParam(defaultValue = "10") Integer size) {
        pageValidator.checkingPageableParams(from, size);
        log.info("Получен GET-запрос к эндпоинту: '/requests/all' от пользователя с ID={} на получение всех запросов",
                userId);
        return requestClient.getAllRequestsToResponse(userId, from, size);
    }
}
