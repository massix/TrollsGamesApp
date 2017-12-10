package rocks.massi.trollsgames.events;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import rocks.massi.trollsgames.data.User;

@Data
@RequiredArgsConstructor
public class LoginSuccessEvent {
    private final User user;
    private final String token;
}
