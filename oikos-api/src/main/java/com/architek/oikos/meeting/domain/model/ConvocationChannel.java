package com.architek.oikos.meeting.domain.model;

import java.util.Objects;

import com.architek.oikos.meeting.domain.valueobject.ChannelCode;

/**
 * A way a convocation can reach the lot's owners, as a catalog row rather than
 * an enum constant: the list has to grow without a deployment, the same reason
 * UnitType became UnitTypeDefinition.
 *
 * <p>The catalog is global, not scoped by property: the channels are the
 * product's, not one copropriété's.
 *
 * <p>{@link #isAutomated()} is the distinction that carries a consequence.
 * Automated means the application performs the send itself; manual means a
 * person does it and then records the fact. Confusing the two would have the
 * tracking table claim a letter was posted because a button was pressed -
 * which is exactly the record a contested AG turns on.
 *
 * <p>Note the asymmetry when the catalog grows: a manual channel costs one
 * INSERT and nothing else, an automated one needs an emitter in the code too.
 * A row marked automated with no emitter behind it promises a send that never
 * happens, and SendConvocationUseCase refuses it rather than pretend.
 */
public final class ConvocationChannel {

    private final ChannelCode code;
    private final String label;
    private final boolean automated;
    private final int position;
    private final boolean active;

    private ConvocationChannel(ChannelCode code, String label, boolean automated, int position, boolean active) {
        this.code = Objects.requireNonNull(code, "code must not be null");
        this.label = Objects.requireNonNull(label, "label must not be null");
        if (label.isBlank()) {
            throw new IllegalArgumentException("channel label must not be blank");
        }
        this.automated = automated;
        this.position = position;
        this.active = active;
    }

    public static ConvocationChannel of(ChannelCode code, String label, boolean automated, int position,
                                         boolean active) {
        return new ConvocationChannel(code, label, automated, position, active);
    }

    public ChannelCode getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    public boolean isAutomated() {
        return automated;
    }

    public int getPosition() {
        return position;
    }

    public boolean isActive() {
        return active;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        return o instanceof ConvocationChannel other && code.equals(other.code);
    }

    @Override
    public int hashCode() {
        return code.hashCode();
    }
}
