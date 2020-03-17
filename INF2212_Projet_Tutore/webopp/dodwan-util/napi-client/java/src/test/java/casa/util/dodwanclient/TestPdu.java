package casa.util.dodwanclient;

import casa.util.pdu.Pdu;
import java.util.HashMap;;
import java.util.Map;

public class TestPdu extends Pdu
{
    private static Map<String, Class> types = new HashMap<>();

    static {
        types.put("name", String.class);
        types.put("tkn", String.class);
    }

    @Override
    public Map <String, Class> pduTypes() {
        return types;
    }
}
