package dslabs.clientserver;

import dslabs.atmostonce.AMOCommand;
import dslabs.atmostonce.AMOResult;
import dslabs.framework.Address;
import dslabs.framework.Client;
import dslabs.framework.Command;
import dslabs.framework.Node;
import dslabs.framework.Result;
import dslabs.clientserver.Request;
import dslabs.clientserver.Reply;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Simple client that sends requests to a single server and returns responses.
 *
 * <p>See the documentation of {@link Client} and {@link Node} for important implementation notes.
 */
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
class SimpleClient extends Node implements Client {
  private final Address serverAddress;

  // invariant: sequenceNum == -1 if no request or reply was processed
  private Request lastRequest = new Request(new AMOCommand(null, this.address(), -1));
  private Reply lastReply = new Reply(new AMOResult(null, -1));

  /* -----------------------------------------------------------------------------------------------
   *  Construction and Initialization
   * ---------------------------------------------------------------------------------------------*/
  public SimpleClient(Address address, Address serverAddress) {
    super(address);
    this.serverAddress = serverAddress;
  }

  @Override
  public synchronized void init() {
    // No initialization necessary
  }

  /* -----------------------------------------------------------------------------------------------
   *  Client Methods
   * ---------------------------------------------------------------------------------------------*/
  @Override
  public synchronized void sendCommand(Command command) {
    Request request = new Request(new AMOCommand(command, this.address(), lastRequest.command().sequenceNum()+1));
    this.send(request, serverAddress);
    lastRequest = request;
    this.set(new ClientTimer(), ClientTimer.CLIENT_RETRY_MILLIS);
  }

  @Override
  public synchronized boolean hasResult() {
    return lastRequest.command().sequenceNum() == lastReply.result().sequenceNum();
  }

  @Override
  public synchronized Result getResult() throws InterruptedException {
    while (!hasResult()) {
      this.wait();
    }
    return lastReply.result().result();
  }

  /* -----------------------------------------------------------------------------------------------
   *  Message Handlers
   * ---------------------------------------------------------------------------------------------*/
  private synchronized void handleReply(Reply m, Address sender) {
    if (m.result().sequenceNum() <= lastReply.result().sequenceNum()) {
      return;
    }
    lastReply = m;
  }

  /* -----------------------------------------------------------------------------------------------
   *  Timer Handlers
   * ---------------------------------------------------------------------------------------------*/
  private synchronized void onClientTimer(ClientTimer t) {
    if (!hasResult()) {
      this.send(lastRequest, serverAddress);
      this.set(new ClientTimer(), ClientTimer.CLIENT_RETRY_MILLIS);
    }
  }
}
