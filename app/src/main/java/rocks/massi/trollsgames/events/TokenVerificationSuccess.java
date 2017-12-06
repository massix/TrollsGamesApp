package rocks.massi.trollsgames.events;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import rocks.massi.trollsgames.data.User;
import rocks.massi.trollsgames.data.UserInformation;

@Data
@RequiredArgsConstructor
public class TokenVerificationSuccess {
    private final UserInformation userInformation;
    private final User user;
}
