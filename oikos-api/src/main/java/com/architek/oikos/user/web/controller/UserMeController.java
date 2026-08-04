package com.architek.oikos.user.web.controller;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import com.architek.oikos.shared.domain.valueobject.EmailVO;
import com.architek.oikos.shared.domain.valueobject.RawPassword;
import com.architek.oikos.user.application.command.ChangePasswordCommand;
import com.architek.oikos.user.application.command.UpdateUserProfileCommand;
import com.architek.oikos.user.application.dto.UserView;
import com.architek.oikos.user.application.port.in.ChangePasswordUseCase;
import com.architek.oikos.user.application.port.in.GetMyInstallmentsUseCase;
import com.architek.oikos.user.application.port.in.GetMyUnitsUseCase;
import com.architek.oikos.user.application.port.in.GetUserUseCase;
import com.architek.oikos.user.application.port.in.UpdateUserProfileUseCase;
import com.architek.oikos.user.application.query.GetMyInstallmentsQuery;
import com.architek.oikos.user.application.query.GetMyUnitsQuery;
import com.architek.oikos.user.application.query.GetUserQuery;
import com.architek.oikos.user.domain.valueobject.UserId;
import com.architek.oikos.user.web.request.ChangePasswordRequest;
import com.architek.oikos.user.web.request.UpdateProfileRequest;
import com.architek.oikos.user.web.response.OwnedInstallmentResponse;
import com.architek.oikos.user.web.response.OwnedUnitResponse;
import com.architek.oikos.user.web.response.UserResponse;

/**
 * "Self" zone: the acting user's id is always taken from the authenticated
 * principal (JWT subject), never from the URL.
 */
@RestController
@RequestMapping("/users/me")
public class UserMeController {

    private final GetUserUseCase getUserUseCase;
    private final UpdateUserProfileUseCase updateUserProfileUseCase;
    private final ChangePasswordUseCase changePasswordUseCase;
    private final GetMyUnitsUseCase getMyUnitsUseCase;
    private final GetMyInstallmentsUseCase getMyInstallmentsUseCase;

    public UserMeController(GetUserUseCase getUserUseCase,
                             UpdateUserProfileUseCase updateUserProfileUseCase,
                             ChangePasswordUseCase changePasswordUseCase,
                             GetMyUnitsUseCase getMyUnitsUseCase,
                             GetMyInstallmentsUseCase getMyInstallmentsUseCase) {
        this.getUserUseCase = getUserUseCase;
        this.updateUserProfileUseCase = updateUserProfileUseCase;
        this.changePasswordUseCase = changePasswordUseCase;
        this.getMyUnitsUseCase = getMyUnitsUseCase;
        this.getMyInstallmentsUseCase = getMyInstallmentsUseCase;
    }

    @GetMapping
    public UserResponse me(Authentication authentication) {
        UserView view = getUserUseCase.getUser(new GetUserQuery(currentUserId(authentication)));
        return UserResponse.from(view);
    }

    @GetMapping("/units")
    public List<OwnedUnitResponse> myUnits(Authentication authentication) {
        return getMyUnitsUseCase.getMyUnits(new GetMyUnitsQuery(currentUserId(authentication))).stream()
                .map(OwnedUnitResponse::from)
                .toList();
    }

    @GetMapping("/installments")
    public List<OwnedInstallmentResponse> myInstallments(Authentication authentication) {
        return getMyInstallmentsUseCase.getMyInstallments(new GetMyInstallmentsQuery(currentUserId(authentication))).stream()
                .map(OwnedInstallmentResponse::from)
                .toList();
    }

    @PatchMapping("/profile")
    public UserResponse updateProfile(Authentication authentication, @Valid @RequestBody UpdateProfileRequest request) {
        UpdateUserProfileCommand command = new UpdateUserProfileCommand(
                currentUserId(authentication), request.fullName(), EmailVO.of(request.email()));
        return UserResponse.from(updateUserProfileUseCase.updateProfile(command));
    }

    @PatchMapping("/password")
    public void changePassword(Authentication authentication, @Valid @RequestBody ChangePasswordRequest request) {
        ChangePasswordCommand command = new ChangePasswordCommand(
                currentUserId(authentication),
                RawPassword.of(request.currentPassword()),
                RawPassword.of(request.newPassword()));
        changePasswordUseCase.changePassword(command);
    }

    private UserId currentUserId(Authentication authentication) {
        return UserId.of(authentication.getName());
    }
}
