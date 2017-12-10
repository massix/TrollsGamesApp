package rocks.massi.trollsgames.events;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class LoginFailEvent {
    private final String error;
}
