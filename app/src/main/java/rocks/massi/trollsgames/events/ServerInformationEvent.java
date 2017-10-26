package rocks.massi.trollsgames.events;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import rocks.massi.trollsgames.data.ServerInformation;

@Data
@RequiredArgsConstructor
public class ServerInformationEvent {
    private final ServerInformation serverInformation;
}
