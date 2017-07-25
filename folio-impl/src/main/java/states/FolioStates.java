package states;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.knoldus.Folio;
import com.lightbend.lagom.serialization.CompressedJsonable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import java.util.Optional;

@Value
@Builder
@JsonDeserialize
@AllArgsConstructor
public class FolioStates implements CompressedJsonable {
    Optional<Folio> folio;
    String timestamp;
}
