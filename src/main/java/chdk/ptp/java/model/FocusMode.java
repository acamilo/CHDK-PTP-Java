/*
 * ---------------------------------------------------------------------
 * Kiwi Remote Instrumentation Platform
 * http://kiwi.man.poznan.pl
 * Copyright (C) 2010-2014
 * ---------------------------------------------------------------------
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Contributors:
 *
 */
package chdk.ptp.java.model;

import java.util.HashMap;
import java.util.Map;

/**
 * CHDK available focusing modes
 *
 * @see <a href="http://chdk.wikia.com/wiki/Script_commands#get_focus_mode">CHDK
 *     get_focus_mode()</a>
 * @author <a href="mailto:mikolajd@man.poznan.pl">Mikolaj Dobski</a>
 */
public enum FocusMode {
  UNKNOWN(-1),
  AUTO(0),
  MF(1),
  INF(3),
  MACRO(4),
  SUPERMACRO(5);

  private int value;

  private static Map<Integer, FocusMode> map = new HashMap<>();

  static {
    for (FocusMode focMode : FocusMode.values()) {
      map.put(focMode.value, focMode);
    }
  }

  private FocusMode(int value) {
    this.value = value;
  }

  /**
   * Get value of field: value
   *
   * @return the value
   */
  public int getValue() {
    return value;
  }

  /**
   * @param rawFocusMode value returned from
   * @see <a href="http://chdk.wikia.com/wiki/Script_commands#get_focus_mode">CHDK
   *     get_focus_mode()</a>
   * @return relevant {@link FocusMode}
   */
  public static FocusMode valueOf(int rawFocusMode) {
    FocusMode focusMode = map.get(rawFocusMode);
    if (focusMode == null) focusMode = FocusMode.UNKNOWN;
    return focusMode;
  }
}
