package rocks.massi.trollsgames.events;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import rocks.massi.trollsgames.data.User;

@RequiredArgsConstructor
@Data
public class UserRegisteredEvent {
    private final User user;
}
