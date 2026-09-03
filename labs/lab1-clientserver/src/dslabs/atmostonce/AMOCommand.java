package dslabs.atmostonce;

import dslabs.framework.Command;
import dslabs.framework.Address;
import lombok.Data;

@Data
public final class AMOCommand implements Command {
  private final Command command;
  private final Address sender;
  private final int sequenceNum;
}
