package dslabs.clientserver;

import dslabs.framework.Message;
import dslabs.atmostonce.AMOCommand;
import dslabs.atmostonce.AMOResult;
import lombok.Data;

@Data
class Request implements Message {
  private final AMOCommand command;
}

@Data
class Reply implements Message {
  private final AMOResult result;
}
