import com.github.okra.modal.Message;
import io.netty.util.HashedWheelTimer;
import io.netty.util.Timeout;
import io.netty.util.Timer;
import io.netty.util.TimerTask;
import io.netty.util.concurrent.DefaultThreadFactory;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class BE {
  public static void main(String[] args) {
    final Timer timer =
        new HashedWheelTimer(new DefaultThreadFactory("timer-"));
    B a = new B();
    Message message = new Message();
    message.setId("123");
    message.setData("fuck");
    TimerTask task1 =
        timeout -> {
          a.send(new InetSocketAddress("127.0.0.1", 8888), message);
          timer.newTimeout(timeout.task(), 1, TimeUnit.SECONDS);
        };
    timer.newTimeout(task1, 1, TimeUnit.SECONDS);
  }
}
