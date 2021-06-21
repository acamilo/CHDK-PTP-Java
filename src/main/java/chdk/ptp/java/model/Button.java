/*
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
 * CHDK available buttons
 *
 * @see <a href= "http://chdk.wikia.com/wiki/CHDK_scripting#Camera_Button_Commands"> available
 *     buttons</a>
 */
public enum Button {
  UP("up"),
  DOWN("down"),
  LEFT("left"),
  RIGHT("right"),
  SET("set"),
  MODE("mode"),
  SHOOT_HALF("shoot_half"),
  SHOOT_FULL("shoot_full"),
  HELP("help"),
  ZOOM_IN("zoom_in"),
  ZOOM_OUT("zoom_out"),
  MENU("menu"),
  DISPLAY("display"),
  PRINT("print"),
  ERASE("erase"),
  UNDEFINED("");

  private String command;

  private static Map<String, Button> map = new HashMap<>();

  static {
    for (Button button : Button.values()) {
      map.put(button.command, button);
    }
  }

  public String getCommand() {
    return command;
  }

  private Button(String command) {
    this.command = command;
  }
}
