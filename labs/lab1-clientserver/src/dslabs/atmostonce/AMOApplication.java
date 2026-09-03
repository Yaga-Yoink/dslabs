package dslabs.atmostonce;

import dslabs.framework.Address;
import dslabs.framework.Application;
import dslabs.framework.Command;
import dslabs.framework.Result;
import java.util.HashMap;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@EqualsAndHashCode
@ToString
@RequiredArgsConstructor
public final class AMOApplication<T extends Application> implements Application {
  @Getter @NonNull private final T application;

  private final HashMap<Address, AMOResult> results = new HashMap<>();

  @Override
  public AMOResult execute(Command command) {
    if (!(command instanceof AMOCommand amoCommand)) {
      throw new IllegalArgumentException();
    }

    AMOResult lastResult = results.getOrDefault(amoCommand.sender(), new AMOResult(null, -1));
    if (alreadyExecuted(amoCommand)) return lastResult;
    Result result = application().execute(amoCommand.command());
    AMOResult amoResult = new AMOResult(result, amoCommand.sequenceNum());
    results.put(amoCommand.sender(), amoResult);
    return amoResult;
  }

  public Result executeReadOnly(Command command) {
    if (!command.readOnly()) {
      throw new IllegalArgumentException();
    }

    if (command instanceof AMOCommand) {
      return execute(command);
    }

    return application.execute(command);
  }

  public boolean alreadyExecuted(AMOCommand amoCommand) {
    AMOResult lastResult = results.getOrDefault(amoCommand.sender(), new AMOResult(null, -1));
    return amoCommand.sequenceNum() <= lastResult.sequenceNum();
  }
}
