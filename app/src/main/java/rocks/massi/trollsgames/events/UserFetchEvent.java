package rocks.massi.trollsgames.events;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import rocks.massi.trollsgames.data.User;

@Data
@RequiredArgsConstructor
public class UserFetchEvent {
    private final boolean finished;
    private final User user;
}
