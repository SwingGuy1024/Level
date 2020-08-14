package com.neptunedreams.util;

import com.codename1.io.Log;
import com.codename1.ui.Component;
import com.codename1.ui.Container;

/**
 * <p>Created by IntelliJ IDEA.
 * <p>Date: 12/9/16
 * <p>Time: 1:43 PM
 *
 * @author Miguel Mu\u00f1oz
 */
public final class Diagnose {
	private Diagnose() { }

	/**
	 * Shows the component hierarchy of a Container.
	 * @param container The container to show. This is declared as a component because it calls itself recursively.
	 */
	public static void showStructure(@NotNull Container container) {
		StringBuilder builder = new StringBuilder();
		showStructure(container, builder, 0);
	}
	
	private static void showStructure(@NotNull Component component, @NotNull StringBuilder builder, int level) {
		String spaces = getIndent(level, builder);
		if (component instanceof Container) {
			Container container = (Container) component;
			int count = container.getComponentCount();
			Log.p(spaces + identify(container) + " with " + count + " children");
			for (int ii = 0; ii < count; ++ii) {
				Component child = container.getComponentAt(ii);
				showStructure(child, builder, level + 1);
			}
		} else {
			Log.p(spaces + identify(component));
		}
	}

	private static String identify(@NotNull Component component) {
		StringBuilder name = new StringBuilder();
		final Class<? extends Component> componentClass = component.getClass();
		String cName = componentClass.toString();
		name.append(cName);
		
//		// Put this code back when you're running in the simulator. Remove it for an actual build.
//		if (cName.indexOf("$") >= 0) {
//			name.append(" extends ");
//			name.append(componentClass.getSuperclass());
//		}

		return name.toString();
	}

	private static String getIndent(int level, @NotNull StringBuilder builder) {
		int strLen = level * 2;
		while (builder.length() < strLen) {
			builder.append("  "); // 2 spaces
		}
		return builder.toString().substring(0, strLen);
	}

}
