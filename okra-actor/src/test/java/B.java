import com.github.okra.Actor;
import com.github.okra.modal.Message;

public class B extends Actor {
  @Override
  public void preStart() {
    port = 8889;
  }

  @Override
  public void receive(Message event) {
    System.out.println(event.getId());
  }
}
