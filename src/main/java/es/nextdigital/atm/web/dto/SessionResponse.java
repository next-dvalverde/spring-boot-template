package es.nextdigital.atm.web.dto;

import es.nextdigital.atm.service.SessionService.OpenedSession;

public record SessionResponse(String token, boolean pinChangeRequired) {

    public static SessionResponse from(OpenedSession session) {
        return new SessionResponse(session.token(), session.pinChangeRequired());
    }
}
