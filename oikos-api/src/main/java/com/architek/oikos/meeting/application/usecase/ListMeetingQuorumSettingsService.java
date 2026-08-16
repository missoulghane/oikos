package com.architek.oikos.meeting.application.usecase;

import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.meeting.application.dto.MeetingQuorumSettingView;
import com.architek.oikos.meeting.application.port.in.ListMeetingQuorumSettingsUseCase;
import com.architek.oikos.meeting.application.query.ListMeetingQuorumSettingsQuery;
import com.architek.oikos.meeting.domain.repository.MeetingQuorumSettingRepository;

/**
 * Returns only the thresholds actually configured - at most two rows. An
 * unconfigured meeting type is absent rather than reported as zero: the
 * screen has to be able to tell "no quorum required" from "never decided".
 */
@Component
public class ListMeetingQuorumSettingsService implements ListMeetingQuorumSettingsUseCase {

    private final MeetingQuorumSettingRepository quorumSettingRepository;

    public ListMeetingQuorumSettingsService(MeetingQuorumSettingRepository quorumSettingRepository) {
        this.quorumSettingRepository = quorumSettingRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<MeetingQuorumSettingView> listSettings(ListMeetingQuorumSettingsQuery query) {
        return quorumSettingRepository.findByProperty(query.propertyId()).stream()
                .map(MeetingQuorumSettingView::from).toList();
    }
}
