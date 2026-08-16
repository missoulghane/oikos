package com.architek.oikos.meeting.web.controller;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

import com.architek.oikos.meeting.application.command.SetMeetingQuorumSettingCommand;
import com.architek.oikos.meeting.application.port.in.ListMeetingQuorumSettingsUseCase;
import com.architek.oikos.meeting.application.port.in.SetMeetingQuorumSettingUseCase;
import com.architek.oikos.meeting.application.query.ListMeetingQuorumSettingsQuery;
import com.architek.oikos.meeting.domain.valueobject.QuorumPercentage;
import com.architek.oikos.meeting.web.request.SetMeetingQuorumSettingRequest;
import com.architek.oikos.meeting.web.response.MeetingQuorumSettingResponse;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * Per-property quorum thresholds, one per meeting type. The list returns only
 * the types actually configured: an absent type means the threshold was never
 * decided, which the screen has to be able to tell from "no quorum required"
 * (an explicit zero).
 */
@RestController
public class MeetingQuorumSettingController {

    private final SetMeetingQuorumSettingUseCase setMeetingQuorumSettingUseCase;
    private final ListMeetingQuorumSettingsUseCase listMeetingQuorumSettingsUseCase;

    public MeetingQuorumSettingController(SetMeetingQuorumSettingUseCase setMeetingQuorumSettingUseCase,
                                           ListMeetingQuorumSettingsUseCase listMeetingQuorumSettingsUseCase) {
        this.setMeetingQuorumSettingUseCase = setMeetingQuorumSettingUseCase;
        this.listMeetingQuorumSettingsUseCase = listMeetingQuorumSettingsUseCase;
    }

    @PreAuthorize("@propertyAccess.canReadMeetings(authentication, #propertyId)")
    @GetMapping("/properties/{propertyId}/meeting-quorum-settings")
    public List<MeetingQuorumSettingResponse> list(@PathVariable String propertyId) {
        return listMeetingQuorumSettingsUseCase.listSettings(new ListMeetingQuorumSettingsQuery(EntityId.of(propertyId)))
                .stream().map(MeetingQuorumSettingResponse::from).toList();
    }

    @PreAuthorize("@propertyAccess.canManageMeetings(authentication, #propertyId)")
    @PutMapping("/properties/{propertyId}/meeting-quorum-settings")
    public MeetingQuorumSettingResponse set(@PathVariable String propertyId,
                                              @Valid @RequestBody SetMeetingQuorumSettingRequest request) {
        return MeetingQuorumSettingResponse.from(setMeetingQuorumSettingUseCase.set(new SetMeetingQuorumSettingCommand(
                EntityId.of(propertyId), request.meetingType(), QuorumPercentage.of(request.quorumPercentage()))));
    }
}
