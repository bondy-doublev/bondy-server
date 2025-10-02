package org.example.interactionservice.dto;

import lombok.Data;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@Data
public class PageRequestDto {
    private int page = 0;
    private int size = 10;
    private String sortBy = "createdAt";
    private String direction = "desc";

    public Pageable toPageable() {
        Sort.Direction dir = Sort.Direction.fromString(direction);
        return PageRequest.of(page, size, Sort.by(dir, sortBy));
    }
}
