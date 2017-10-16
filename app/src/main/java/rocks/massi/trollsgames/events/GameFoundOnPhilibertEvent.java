package rocks.massi.trollsgames.events;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import rocks.massi.trollsgames.data.PhilibertSearchResponse;

@Data
@RequiredArgsConstructor
public class GameFoundOnPhilibertEvent {
    private final PhilibertSearchResponse philibertSearchResponse;
}
