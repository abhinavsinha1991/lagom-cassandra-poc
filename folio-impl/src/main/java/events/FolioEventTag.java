package events;

import com.lightbend.lagom.javadsl.persistence.AggregateEventTag;

public class FolioEventTag {

    public static final AggregateEventTag<FolioEvent> INSTANCE = AggregateEventTag.of(FolioEvent.class);
}
