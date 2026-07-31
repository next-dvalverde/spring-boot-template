package es.nextdigital.atm.web.dto;

import es.nextdigital.atm.domain.Movement;
import java.util.List;
import org.springframework.data.domain.Page;

public record MovementPageResponse(List<MovementResponse> content, int page, int size, long totalElements) {

    public static MovementPageResponse from(Page<Movement> movements) {
        return new MovementPageResponse(
                movements.getContent().stream().map(MovementResponse::from).toList(),
                movements.getNumber(), movements.getSize(), movements.getTotalElements());
    }
}
