package rocks.massi.trollsgames.events;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import rocks.massi.trollsgames.data.Quote;

@Data
@RequiredArgsConstructor
public class QuoteReceivedEvent {
    private final Quote quote;
}
