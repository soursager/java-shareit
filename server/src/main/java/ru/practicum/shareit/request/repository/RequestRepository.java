package ru.practicum.shareit.request.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.request.model.ItemRequest;

import java.util.List;

public interface RequestRepository extends JpaRepository<ItemRequest, Long> {

    List<ItemRequest> findAllByRequester_Id(Long requesterId);

    @Query("SELECT ir " +
            "FROM ItemRequest ir " +
            "WHERE ir.requester.id != :userId")
    List<ItemRequest> findAllByAllOtherUsers(Long userId, Pageable page);

}
