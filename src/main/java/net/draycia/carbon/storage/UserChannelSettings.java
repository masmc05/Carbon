package net.draycia.carbon.storage;

import net.kyori.adventure.text.format.TextColor;
import org.checkerframework.checker.nullness.qual.Nullable;

public interface UserChannelSettings {

  boolean spying();

  default void spying(boolean spying) {
    this.spying(spying, false);
  }

  void spying(boolean spying, boolean fromRemote);

  boolean ignored();

  void ignoring(boolean ignored, boolean fromRemote);

  default void ignoring(boolean ignored) {
    this.ignoring(ignored, false);
  }

  @Nullable
  TextColor color();

  default void color(@Nullable TextColor color) {
    this.color(color, false);
  }

  void color(@Nullable TextColor color, boolean fromRemote);

}
