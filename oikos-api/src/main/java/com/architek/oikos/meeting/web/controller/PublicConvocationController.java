package com.architek.oikos.meeting.web.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import com.architek.oikos.meeting.application.command.ConfirmConvocationByCodeCommand;
import com.architek.oikos.meeting.application.command.ConfirmConvocationByTokenCommand;
import com.architek.oikos.meeting.application.port.in.ConfirmConvocationByCodeUseCase;
import com.architek.oikos.meeting.application.port.in.ConfirmConvocationByTokenUseCase;
import com.architek.oikos.meeting.application.port.in.GetConvocationByCodeUseCase;
import com.architek.oikos.meeting.application.port.in.GetConvocationByTokenUseCase;
import com.architek.oikos.meeting.application.query.GetConvocationByCodeQuery;
import com.architek.oikos.meeting.application.query.GetConvocationByTokenQuery;
import com.architek.oikos.meeting.domain.valueobject.ShortCode;
import com.architek.oikos.meeting.web.request.ConfirmConvocationByTokenRequest;
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
    private final GetConvocationByCodeUseCase getConvocationByCodeUseCase;
    private final ConfirmConvocationByCodeUseCase confirmConvocationByCodeUseCase;

    public PublicConvocationController(GetConvocationByTokenUseCase getConvocationByTokenUseCase,
                                        ConfirmConvocationByTokenUseCase confirmConvocationByTokenUseCase,
                                        GetConvocationByCodeUseCase getConvocationByCodeUseCase,
                                        ConfirmConvocationByCodeUseCase confirmConvocationByCodeUseCase) {
        this.getConvocationByTokenUseCase = getConvocationByTokenUseCase;
        this.confirmConvocationByTokenUseCase = confirmConvocationByTokenUseCase;
        this.getConvocationByCodeUseCase = getConvocationByCodeUseCase;
        this.confirmConvocationByCodeUseCase = confirmConvocationByCodeUseCase;
    }

    /** What the landing page shows: which assembly, which lot, and where the answer stands. */
    @GetMapping("/{token}")
    public ConvocationConfirmationResponse preview(@PathVariable String token) {
        return ConvocationConfirmationResponse.from(
                getConvocationByTokenUseCase.getByToken(new GetConvocationByTokenQuery(token)));
    }

    /**
     * Answering takes the token <em>and</em> the lot's six-character code, the
     * one printed beside the QR code on the convocation. Reading the page needs
     * only the link; recording an answer in a lot's name asks for the letter
     * itself (ADR 0002 §16).
     *
     * <p>The caller's IP is read here exactly as on the paper path below, and
     * never trusted from the body: it feeds the attempt cap that keeps a
     * six-character code from being tried in a loop.
     */
    @PutMapping("/{token}/reply")
    public ConvocationConfirmationResponse confirm(@PathVariable String token,
                                                     @Valid @RequestBody ConfirmConvocationByTokenRequest body,
                                                     HttpServletRequest request) {
        return ConvocationConfirmationResponse.from(confirmConvocationByTokenUseCase.confirm(
                new ConfirmConvocationByTokenCommand(token, ShortCode.ofNullable(body.confirmationCode()),
                        body.attendanceReply(), callerIdOf(request))));
    }

    /**
     * The same two operations, reached by the pair of six-character codes printed
     * on the letter - for whoever has no phone to scan the QR code and will not
     * type a 43-character token.
     *
     * <p>Both codes travel in the path rather than the body, GET included, so the
     * page can be reached from a bookmark or a re-typed URL. Neither is a secret
     * worth hiding from a proxy log the way a password would be: the reference is
     * printed publicly, and the code is capped, single-lot and single-meeting.
     *
     * <p>The caller's IP is read here and never trusted from the body: it feeds
     * the attempt cap, and a client that could state its own identity would be
     * handed the means to reset its own counter.
     */
    @GetMapping("/by-reference/{meetingReference}/{code}")
    public ConvocationConfirmationResponse previewByCode(@PathVariable String meetingReference,
                                                           @PathVariable String code, HttpServletRequest request) {
        return ConvocationConfirmationResponse.from(getConvocationByCodeUseCase.getByCode(
                new GetConvocationByCodeQuery(ShortCode.ofNullable(meetingReference), ShortCode.ofNullable(code),
                        callerIdOf(request))));
    }

    @PutMapping("/by-reference/{meetingReference}/{code}/reply")
    public ConvocationConfirmationResponse confirmByCode(@PathVariable String meetingReference,
                                                           @PathVariable String code,
                                                           @Valid @RequestBody ConfirmConvocationRequest body,
                                                           HttpServletRequest request) {
        return ConvocationConfirmationResponse.from(confirmConvocationByCodeUseCase.confirm(
                new ConfirmConvocationByCodeCommand(ShortCode.ofNullable(meetingReference),
                        ShortCode.ofNullable(code), body.attendanceReply(), callerIdOf(request))));
    }

    /**
     * Who is trying, for the attempt cap only. X-Forwarded-For is honoured
     * because the product runs behind a reverse proxy, where every request would
     * otherwise share the proxy's own address and one attacker would lock out
     * every copropriétaire at once. It is a header, so it is forgeable - which
     * only means the cap raises the cost of walking the code space rather than
     * making it impossible, exactly as its javadoc says.
     */
    private static String callerIdOf(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
