import com.github.okra.Node;
import com.github.okra.option.NodeOption;
import com.github.okra.option.NodeOptionBuilder;

public class Node1 {
  public static void main(String[] args) {
    NodeOption option =
        NodeOptionBuilder.getBuilder()
            .self("127.0.0.1:8899")
            .peer("127.0.0.1:8898")
            .peer("127.0.0.1:8897")
            .build();
    Node node = new Node();
    node.loadOption(option);
    node.deploy();
    System.out.println("Node start");
  }
}
