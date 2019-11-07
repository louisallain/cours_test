package louis.app.p2p;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

public class MPair {

  public static void main(String[] args) {

    try {
      byte code = 1;
      byte[] adresse = new byte[]{127, 0, 0, 1};
      short udp = 9002;
      byte length = 12;

      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      ByteBuffer buffer;

      buffer = ByteBuffer.allocate(1);
      buffer.put(code);
      bos.write(buffer.array());

      buffer = ByteBuffer.allocate(4);
      buffer.put(adresse);
      bos.write(buffer.array());

      bos.write(ByteBuffer.allocate(2).putShort(udp).array());

      buffer = ByteBuffer.allocate(1);
      buffer.put(length);
      bos.write(buffer.array());

      byte[] output = bos.toByteArray();
      System.out.println(output.length);

      bos.reset();
      bos.write(output, 5, 2);
      System.out.println("outudp : " + ByteBuffer.wrap(bos.toByteArray()).getShort());
      bos.reset();
    }
    catch(Exception e) {
      e.printStackTrace();
    }
  }
}
