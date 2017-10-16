package rocks.massi.trollsgames.data;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class PhilibertSearchResponse {
    private final int id_product;
    private final String pname;
    private final String product_link;
}
