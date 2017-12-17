package rocks.massi.trollsgames.data;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@Data
@ToString
@RequiredArgsConstructor
public class UserInformation {
    private final String user;
    private final String email;
}
