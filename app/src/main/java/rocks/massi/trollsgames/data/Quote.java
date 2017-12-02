package rocks.massi.trollsgames.data;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class Quote {
    private final String author;
    private final String quote;
}
