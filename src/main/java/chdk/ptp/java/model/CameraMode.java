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
 * CHDK available camera operation modes
 *
 * @see <a
 *      href="http://chdk.wikia.com/wiki/Script_commands#set_record.28state.29">CHDK
 *      set_record(0)</a>
 * @see <a href="http://chdk.wikia.com/wiki/Lua/Lua_Reference#get_mode">CHDK
 *      get_mode)</a> first param
 * 
 * 
 * @author <a href="mailto:mikolajd@man.poznan.pl">Mikolaj Dobski</a>
 *
 */
public enum CameraMode {
	UNKNOWN(-1), PLAYBACK(0), RECORD(1);

	private int value;

	private static Map<Integer, CameraMode> map = new HashMap<>();

	static {
		for (CameraMode camMode : CameraMode.values()) {
			map.put(camMode.value, camMode);
		}
	}

	private CameraMode(int value) {
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
	 * @param camMode
	 *            value of first parameter returned by @see <a
	 *            href="http://chdk.wikia.com/wiki/Lua/Lua_Reference#get_mode"
	 *            >CHDK get_mode)</a>
	 * @return relevant {@link CameraMode}
	 */
	public static CameraMode valueOf(int camMode) {
		CameraMode cameraMode = map.get(camMode);
		if (cameraMode == null)
			cameraMode = UNKNOWN;
		return cameraMode;
	}
}
