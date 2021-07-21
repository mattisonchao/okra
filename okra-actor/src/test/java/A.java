import com.github.okra.Actor;
import com.github.okra.disruptor.MessageEvent;
import com.github.okra.modal.Message;

public class A extends Actor {
  @Override
  public void preStart() {
    port = 8888;
  }

  @Override
  public void receive(Message event) {
    System.out.println(event.getId());
  }
}
