package dslabs.clientserver;

import dslabs.framework.Address;
import dslabs.framework.Application;
import dslabs.framework.Node;
import dslabs.kvstore.*;
import dslabs.kvstore.KVStore.*;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import java.util.HashMap;

/**
 * Simple server that receives requests and returns responses.
 *
 * <p>See the documentation of {@link Node} for important implementation notes.
 */
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
class SimpleServer extends Node {

  private final KVStore kvStore = new KVStore();
  private final HashMap<Address, Reply> replyMap = new HashMap<>();

  /* -----------------------------------------------------------------------------------------------
   *  Construction and Initialization
   * ---------------------------------------------------------------------------------------------*/
  public SimpleServer(Address address, Application app) {
    super(address);
  }

  @Override
  public void init() {
    // No initialization necessary
  }

  /* -----------------------------------------------------------------------------------------------
   *  Message Handlers
   * ---------------------------------------------------------------------------------------------*/
  private void handleRequest(Request m, Address sender) {
    Reply lastReply = replyMap.getOrDefault(sender, new Reply(null, -1));
    if (m.sequenceNum() < lastReply.sequenceNum()) {
      return;
    } else if (m.sequenceNum() == lastReply.sequenceNum()) {
      this.send(lastReply, sender);
      return;
    }
    KVStoreResult result = kvStore.execute(m.command());
    this.send(new Reply(result, m.sequenceNum()), sender);
    replyMap.put(sender, new Reply(result, m.sequenceNum()));
  }
}
