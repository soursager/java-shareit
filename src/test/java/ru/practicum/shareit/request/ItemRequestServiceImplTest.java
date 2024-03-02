package ru.practicum.shareit.request;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.exception.DataNotFoundException;
import ru.practicum.shareit.exception.DataValidationException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.RequestRepository;
import ru.practicum.shareit.request.service.ItemRequestServicelmpl;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.validator.RequestValidator;
import ru.practicum.shareit.validator.UserValidator;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static ru.practicum.shareit.request.dto.ItemRequestMapper.*;

@ExtendWith(MockitoExtension.class)
public class ItemRequestServiceImplTest {

    private User owner;
    private Item item;
    private ItemRequest itemRequest;

    @Mock
    RequestRepository requestRepository;

    @Mock
    UserValidator userValidator;

    @Mock
    RequestValidator itemRequestValidator;

    @InjectMocks
    ItemRequestServicelmpl requestService;

    @BeforeEach
    public void fillData() {

        owner = User.builder()
                .id(1L)
                .name("name")
                .email("email@mai.ru")
                .build();

        itemRequest = ItemRequest.builder()
                .id(1L)
                .requester(owner)
                .description("text")
                .creationDate(LocalDateTime.now())
                .build();

        item = Item.builder()
                .id(1L)
                .available(true)
                .description("desc")
                .name("name")
                .owner(owner)
                .request(itemRequest)
                .build();
    }

    @Test
    void addNewRequest_whenUserExists_thenReturnItemRequestDto() {
        itemRequest.setResponsesToRequest(List.of(item));
        when(userValidator.returnUserIfExists(anyLong())).thenReturn(owner);
        when(requestRepository.save(any(ItemRequest.class))).thenReturn(itemRequest);

        ItemRequestDto actualRequest = requestService.addNewRequest(toItemRequestDto(itemRequest), 1L);

        assertEquals(actualRequest.getDescription(), itemRequest.getDescription());
        verify(itemRequestValidator, times(1))
                .validateItemRequestData(any(ItemRequestDto.class));
    }

    @Test
    void addNewRequest_whenUserNotExists_thenThrowEntityNotFoundException() {
        when(requestRepository.save(any(ItemRequest.class)))
                .thenThrow(new DataNotFoundException("Пользователь не существует!"));

        DataNotFoundException dataNotFoundException = assertThrows(DataNotFoundException.class,
                () -> requestService.addNewRequest(toItemRequestDto(new ItemRequest()), 1L));

        assertEquals(dataNotFoundException.getMessage(), "Пользователь не существует!");
    }

    @Test
    void addNewRequest_whenUserExistsAndRequestDataIsIncorrect_thenThrowIncorrectDataException() {
        itemRequest.setResponsesToRequest(List.of(item));
        when(userValidator.returnUserIfExists(anyLong()))
                .thenThrow(new DataValidationException("Описание не может быть пустым"));

        DataValidationException dataValidationException = assertThrows(DataValidationException.class,
                () -> requestService.addNewRequest(toItemRequestDto(itemRequest), 1L));

        assertEquals(dataValidationException.getMessage(), "Описание не может быть пустым");
    }

    @Test
    void getAllUserRequestsWithResponses_whenUserExists_thenReturnListOfResponses() {
        doNothing().when(userValidator).checkingUserIdAndNotReturns(anyLong());
        when(requestRepository.findAllByRequester_Id(anyLong())).thenReturn(List.of(new ItemRequest()));

        List<ItemRequestDto> requests = new ArrayList<>(requestService.getAllUserRequestsWithResponses(1L));

        assertEquals(requests.size(), 1);
    }

    @Test
    void getAllUserRequestsWithResponses_whenUserNotExists_thenThrowEntityNotFoundException() {
        doThrow(new DataNotFoundException("Пользователь не существует!"))
                .when(userValidator).checkingUserIdAndNotReturns(anyLong());

        DataNotFoundException entityNotFoundException = assertThrows(DataNotFoundException.class,
                () -> requestService.getAllUserRequestsWithResponses(1L));

        assertEquals(entityNotFoundException.getMessage(), "Пользователь не существует!");
    }

    @Test
    void getAllRequestsToResponse_whenUserExists_thenReturnListOfRequests() {
        doNothing().when(userValidator).checkingUserIdAndNotReturns(anyLong());
        when(requestRepository.findAllByAllOtherUsers(anyLong(),
                any(Pageable.class))).thenReturn(List.of(new ItemRequest()));

        List<ItemRequestDto> requests = new ArrayList<>(requestService.getAllRequestsToResponse(1L,
                PageRequest.of(0, 10)));

        assertEquals(requests.size(), 1);
    }

    @Test
    void getAllRequestsToResponse_whenUserNotExists_thenThrowEntityNotFoundException() {
        doThrow(new DataNotFoundException("Пользователя с номером 1 не существует!"))
                .when(userValidator).checkingUserIdAndNotReturns(anyLong());

        DataNotFoundException entityNotFoundException = assertThrows(DataNotFoundException.class,
                () -> requestService.getAllRequestsToResponse(1L, PageRequest.of(0, 10)));

        assertEquals(entityNotFoundException.getMessage(), "Пользователя с номером 1 не существует!");
    }

    @Test
    void getRequestById_whenUserAndRequestExist_thenReturnRequest() {
        ItemRequest expectedRequest = new ItemRequest();
        doNothing().when(userValidator).checkingUserIdAndNotReturns(anyLong());
        when(itemRequestValidator.validateItemRequestIdAndReturns(anyLong())).thenReturn(new ItemRequest());

        ItemRequestDto actualRequest = requestService.getRequestById(1L, 1L);

        assertEquals(actualRequest, toItemRequestDto(expectedRequest));
    }

    @Test
    void getRequestById_whenUserNotFound_thenThrowEntityNotFoundException() {
        doThrow(new DataNotFoundException("Пользователь не существует!"))
                .when(userValidator).checkingUserIdAndNotReturns(anyLong());

        DataNotFoundException entityNotFoundException = assertThrows(DataNotFoundException.class,
                () -> requestService.getRequestById(1L, 1L));

        assertEquals(entityNotFoundException.getMessage(), "Пользователь не существует!");
    }

    @Test
    void getRequestById_whenRequestNotFound_thenThrowEntityNotFoundException() {
        doNothing().when(userValidator).checkingUserIdAndNotReturns(anyLong());
        when(itemRequestValidator.validateItemRequestIdAndReturns(anyLong()))
                .thenThrow(new DataNotFoundException("Запрос не существует!"));

        DataNotFoundException entityNotFoundException = assertThrows(DataNotFoundException.class,
                () -> requestService.getRequestById(1L, 1L));

        assertEquals(entityNotFoundException.getMessage(), "Запрос не существует!");
    }
}
