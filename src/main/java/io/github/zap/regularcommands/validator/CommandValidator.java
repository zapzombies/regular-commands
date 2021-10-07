package io.github.zap.regularcommands.validator;

import io.github.zap.regularcommands.commands.Context;

/**
 * Used to validate against the command context. Validators can 'depend' on the success of a single other validator,
 * which will be executed first.
 *
 * Validators support covariance. If A is a superclass of B, and Validator 1 depends on validator of type A, Validator 1
 * can be constructed with a validator of type B.
 * @param <T> The type of data object this CommandValidator produces
 * @param <V> The type of data object this CommandValidator receives
 */
public class CommandValidator<T, V> {
   private final ValidationStep<T, V> step;
   private final CommandValidator<? extends V, ?> depend;

   /**
    * Creates a new CommandValidator instance that depends on the success of another validator, which will be tested
    * first. If it fails, this CommandValidator will not execute.
    * @param step The ValidationStep used by this validator. This is the code that will perform the actual, contextual
    *             testing
    * @param depend The CommandValidator whose success determines whether this instances gets tested or not
    */
   public CommandValidator(ValidationStep<T, V> step, CommandValidator<? extends V, ?> depend) {
      this.step = step;
      this.depend = depend;
   }

   /**
    * Creates a new CommandValidator from this CommandValidator that depends on the given CommandValidator.
    * @param depend The CommandValidator the new validator will depend on
    * @return The new validator
    */
   public CommandValidator<T, V> from(CommandValidator<? extends V, ?> depend) {
      return new CommandValidator<>(step, depend);
   }

   /**
    * Creates a new CommandValidator instance, which does not depend on any other validators.
    * @param step The ValidationStep used by this validator
    */
   public CommandValidator(ValidationStep<T, V> step) {
      this(step, null);
   }

   /**
    * Runs validation, given a context and an Object array of arguments. Validators chained last are checked first, and
    * earlier validators will not be executed if later validators fail.
    * @param context The validation context
    * @param arguments The command arguments
    * @return A ValidationResult object indicating the success or failure of this validator.
    */
   public ValidationResult<T> validate(Context context, Object[] arguments) {
      if(depend == null) {
         return step.validate(context, arguments, null);
      }

      ValidationResult<? extends V> result = depend.validate(context, arguments);

      if(result.isValid()) {
         return step.validate(context, arguments, result.getData());
      }

      return ValidationResult.of(false, result.getErrorMessage(), null);
   }
}