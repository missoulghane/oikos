import { describe, expect, it } from 'vitest';
import { render, screen } from '@testing-library/react';
import { AttendanceSummaryCard } from '@/features/property-mngt/general-meetings/components/AttendanceSummaryCard';
import type { AttendanceSummary } from '@/features/property-mngt/general-meetings/types/convocation.types';

const base: AttendanceSummary = {
  totalUnits: 10,
  sentCount: 10,
  attendingCount: 4,
  notAttendingCount: 2,
  noReplyCount: 4,
  checkedInCount: 3,
  totalWeight: 1000,
  presentWeight: 400,
  quorumPercentage: 0,
  quorumRequired: false,
  quorumReached: true,
};

describe('AttendanceSummaryCard', () => {
  it('distinguishes "no quorum required" from a threshold that happens to be met', () => {
    // Both read as a passing check to a machine; only the second is a decision
    // somebody took, and the syndic has to be able to tell them apart.
    render(<AttendanceSummaryCard summary={base} />);

    expect(screen.getByText('Aucun quorum requis')).toBeInTheDocument();
  });

  it('shows the threshold and whether it is reached when one is configured', () => {
    render(
      <AttendanceSummaryCard summary={{ ...base, quorumPercentage: 50, quorumRequired: true, quorumReached: false }} />,
    );

    expect(screen.getByText(/Quorum 50 %/)).toBeInTheDocument();
    expect(screen.getByText(/non atteint/)).toBeInTheDocument();
  });

  it('reports the present weight against the whole copropriété, not against the lots that answered', () => {
    render(<AttendanceSummaryCard summary={base} />);

    expect(screen.getByText('400 voix sur 1 000')).toBeInTheDocument();
  });

  it('drops the émargement count and the quorum badge where the session has not opened', () => {
    // The Convocations tab: both are facts of the session, and both would read "0" and
    // "non atteint" from the first sending to the opening, about a room nobody has entered.
    render(<AttendanceSummaryCard summary={{ ...base, quorumPercentage: 50, quorumRequired: true }} showAttendance={false} />);

    expect(screen.getByText('Convocations envoyées')).toBeInTheDocument();
    expect(screen.queryByText('Émargés')).not.toBeInTheDocument();
    expect(screen.queryByText(/Quorum/)).not.toBeInTheDocument();
  });
});
