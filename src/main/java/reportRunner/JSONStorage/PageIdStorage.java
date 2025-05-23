package reportRunner.JSONStorage;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageIdStorage {
    private String rootPage;
    private Map<String,String> childPages = new HashMap<>();
}
