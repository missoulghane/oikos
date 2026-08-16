package com.architek.oikos.meeting.web.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

import com.architek.oikos.meeting.application.command.ConfirmConvocationByTokenCommand;
import com.architek.oikos.meeting.application.port.in.ConfirmConvocationByTokenUseCase;
import com.architek.oikos.meeting.application.port.in.GetConvocationByTokenUseCase;
import com.architek.oikos.meeting.application.query.GetConvocationByTokenQuery;
import com.architek.oikos.meeting.web.request.ConfirmConvocationRequest;
import com.architek.oikos.meeting.web.response.ConvocationConfirmationResponse;

/**
 * The confirmation link a copropriétaire receives with their convocation.
 *
 * <p>Both endpoints are anonymous - see SecurityConfiguration's permitAll entry
 * for "/api/v1/convocations/by-token/**". Note that the write is anonymous too,
 * unlike the invitation flow where only the preview is: the whole point is the
 * owner who has no account and never will. Requiring authentication would leave
 * exactly the population this was built for with nothing but the telephone.
 *
 * <p>No @PreAuthorize, therefore, and no principal: the token is the entire
 * credential. What keeps that acceptable is that it grants one narrow thing -
 * seeing and answering one lot's convocation - and that ConvocationByTokenService
 * refuses to widen it (no ids returned, no source accepted from the body, no
 * answer once the session has opened).
 */
@RestController
@RequestMapping("/convocations/by-token")
public class PublicConvocationController {

    private final GetConvocationByTokenUseCase getConvocationByTokenUseCase;
    private final ConfirmConvocationByTokenUseCase confirmConvocationByTokenUseCase;

    public PublicConvocationController(GetConvocationByTokenUseCase getConvocationByTokenUseCase,
                                        ConfirmConvocationByTokenUseCase confirmConvocationByTokenUseCase) {
        this.getConvocationByTokenUseCase = getConvocationByTokenUseCase;
        this.confirmConvocationByTokenUseCase = confirmConvocationByTokenUseCase;
    }

    /** What the landing page shows: which assembly, which lot, and where the answer stands. */
    @GetMapping("/{token}")
    public ConvocationConfirmationResponse preview(@PathVariable String token) {
        return ConvocationConfirmationResponse.from(
                getConvocationByTokenUseCase.getByToken(new GetConvocationByTokenQuery(token)));
    }

    @PutMapping("/{token}/reply")
    public ConvocationConfirmationResponse confirm(@PathVariable String token,
                                                     @Valid @RequestBody ConfirmConvocationRequest request) {
        return ConvocationConfirmationResponse.from(confirmConvocationByTokenUseCase.confirm(
                new ConfirmConvocationByTokenCommand(token, request.attendanceReply())));
    }
}
