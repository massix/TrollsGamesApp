package rocks.massi.trollsgames.events;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import rocks.massi.trollsgames.data.User;

import java.util.List;

@Data
@RequiredArgsConstructor
public class UsersFetchedEvent {
    private final List<User> users;
    private final boolean fromCache;
}
