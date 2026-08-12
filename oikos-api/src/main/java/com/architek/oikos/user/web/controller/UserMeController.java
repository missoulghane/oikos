package com.architek.oikos.user.web.controller;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;
import com.architek.oikos.shared.domain.valueobject.EmailVO;
import com.architek.oikos.shared.domain.valueobject.RawPassword;
import com.architek.oikos.shared.exception.BusinessException;
import com.architek.oikos.user.application.command.ChangePasswordCommand;
import com.architek.oikos.user.application.command.UpdateAvatarCommand;
import com.architek.oikos.user.application.command.UpdateUserProfileCommand;
import com.architek.oikos.user.application.dto.AvatarView;
import com.architek.oikos.user.application.dto.UserView;
import com.architek.oikos.user.application.port.in.ChangePasswordUseCase;
import com.architek.oikos.user.application.port.in.GetAvatarUseCase;
import com.architek.oikos.user.application.port.in.GetMyInstallmentsUseCase;
import com.architek.oikos.user.application.port.in.GetMyMembershipRequestsUseCase;
import com.architek.oikos.user.application.port.in.GetMyPaymentsUseCase;
import com.architek.oikos.user.application.port.in.GetMyUnitsUseCase;
import com.architek.oikos.user.application.port.in.GetUserUseCase;
import com.architek.oikos.user.application.port.in.RemoveAvatarUseCase;
import com.architek.oikos.user.application.port.in.UpdateAvatarUseCase;
import com.architek.oikos.user.application.port.in.UpdateUserProfileUseCase;
import com.architek.oikos.user.application.query.GetMyInstallmentsQuery;
import com.architek.oikos.user.application.query.GetMyMembershipRequestsQuery;
import com.architek.oikos.user.application.query.GetMyPaymentsQuery;
import com.architek.oikos.user.application.query.GetMyUnitsQuery;
import com.architek.oikos.user.application.query.GetUserQuery;
import com.architek.oikos.user.domain.valueobject.UserId;
import com.architek.oikos.user.web.request.ChangePasswordRequest;
import com.architek.oikos.user.web.request.UpdateProfileRequest;
import com.architek.oikos.user.web.response.OwnedInstallmentResponse;
import com.architek.oikos.user.web.response.OwnedMembershipRequestResponse;
import com.architek.oikos.user.web.response.OwnedPaymentResponse;
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
    private final GetMyMembershipRequestsUseCase getMyMembershipRequestsUseCase;
    private final GetMyPaymentsUseCase getMyPaymentsUseCase;
    private final UpdateAvatarUseCase updateAvatarUseCase;
    private final RemoveAvatarUseCase removeAvatarUseCase;
    private final GetAvatarUseCase getAvatarUseCase;

    public UserMeController(GetUserUseCase getUserUseCase,
                             UpdateUserProfileUseCase updateUserProfileUseCase,
                             ChangePasswordUseCase changePasswordUseCase,
                             GetMyUnitsUseCase getMyUnitsUseCase,
                             GetMyInstallmentsUseCase getMyInstallmentsUseCase,
                             GetMyMembershipRequestsUseCase getMyMembershipRequestsUseCase,
                             GetMyPaymentsUseCase getMyPaymentsUseCase,
                             UpdateAvatarUseCase updateAvatarUseCase,
                             RemoveAvatarUseCase removeAvatarUseCase,
                             GetAvatarUseCase getAvatarUseCase) {
        this.getUserUseCase = getUserUseCase;
        this.updateUserProfileUseCase = updateUserProfileUseCase;
        this.changePasswordUseCase = changePasswordUseCase;
        this.getMyUnitsUseCase = getMyUnitsUseCase;
        this.getMyInstallmentsUseCase = getMyInstallmentsUseCase;
        this.getMyMembershipRequestsUseCase = getMyMembershipRequestsUseCase;
        this.getMyPaymentsUseCase = getMyPaymentsUseCase;
        this.updateAvatarUseCase = updateAvatarUseCase;
        this.removeAvatarUseCase = removeAvatarUseCase;
        this.getAvatarUseCase = getAvatarUseCase;
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

    @GetMapping("/membership-requests")
    public List<OwnedMembershipRequestResponse> myMembershipRequests(Authentication authentication) {
        return getMyMembershipRequestsUseCase.getMyMembershipRequests(new GetMyMembershipRequestsQuery(currentUserId(authentication)))
                .stream()
                .map(OwnedMembershipRequestResponse::from)
                .toList();
    }

    @GetMapping("/payments")
    public List<OwnedPaymentResponse> myPayments(Authentication authentication) {
        return getMyPaymentsUseCase.getMyPayments(new GetMyPaymentsQuery(currentUserId(authentication))).stream()
                .map(OwnedPaymentResponse::from)
                .toList();
    }

    @PatchMapping("/profile")
    public UserResponse updateProfile(Authentication authentication, @Valid @RequestBody UpdateProfileRequest request) {
        UpdateUserProfileCommand command = new UpdateUserProfileCommand(
                currentUserId(authentication), request.fullName(), EmailVO.of(request.email()), request.phone());
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

    @PutMapping(value = "/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public UserResponse updateAvatar(Authentication authentication, @RequestParam MultipartFile file) {
        if (file.isEmpty()) {
            throw new BusinessException("The uploaded avatar must not be empty");
        }
        UpdateAvatarCommand command = new UpdateAvatarCommand(
                currentUserId(authentication), readBytes(file), file.getContentType());
        return UserResponse.from(updateAvatarUseCase.updateAvatar(command));
    }

    @DeleteMapping("/avatar")
    public UserResponse removeAvatar(Authentication authentication) {
        return UserResponse.from(removeAvatarUseCase.removeAvatar(currentUserId(authentication)));
    }

    @GetMapping("/avatar")
    public ResponseEntity<ByteArrayResource> getAvatar(Authentication authentication) {
        AvatarView avatar = getAvatarUseCase.getAvatar(currentUserId(authentication));
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(avatar.contentType()))
                .body(new ByteArrayResource(avatar.content()));
    }

    private static byte[] readBytes(MultipartFile file) {
        try {
            return file.getBytes();
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to read uploaded avatar", e);
        }
    }

    private UserId currentUserId(Authentication authentication) {
        return UserId.of(authentication.getName());
    }
}
