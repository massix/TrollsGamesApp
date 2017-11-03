package rocks.massi.trollsgames.data;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class ServerInformation {
    final private String version;
    final private String artifact;
}
