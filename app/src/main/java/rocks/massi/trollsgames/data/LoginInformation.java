package rocks.massi.trollsgames.data;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@Data
@ToString
@RequiredArgsConstructor
public class LoginInformation {
    private final String email;
    private final String password;
}
