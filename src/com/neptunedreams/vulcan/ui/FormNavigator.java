package com.neptunedreams.vulcan.ui;

import java.util.Hashtable;
import com.codename1.ui.Command;
import com.codename1.ui.Display;
import com.codename1.ui.Form;
import com.codename1.ui.Image;
import com.codename1.ui.NavigationCommand;
import com.codename1.ui.animations.BubbleTransition;
import com.codename1.ui.animations.CommonTransitions;
import com.codename1.ui.animations.Transition;
import com.codename1.ui.events.ActionEvent;
import com.neptunedreams.Assert;
import com.neptunedreams.util.NotNull;
import com.neptunedreams.util.Nullable;

/**
 * <p>Created by IntelliJ IDEA.
 * <p>Date: 4/29/16
 * <p>Time: 6:11 PM
 *
 * @author Miguel Mu\u00f1oz
 */
@SuppressWarnings("unused")
public final class FormNavigator {
	private static final Hashtable<String, Form> formMap = new Hashtable<>();
	public  static final int HALF_SECOND = 500;
	@SuppressWarnings("StaticNonFinalField")
//	private static Transition defaultTransition = createSlideTransition();
	private FormNavigator() { }

	private static Transition createSlideTransition() {
		return createSlideTransition(false);
	}
	
	private static Transition createSlideTransition(boolean reverse) {
		return CommonTransitions.createSlide(CommonTransitions.SLIDE_HORIZONTAL, reverse, HALF_SECOND);
	}

	private static Transition createReverseSlideTransition() {
		return createSlideTransition(true);
	}
	
	private static Transition createBubbleTransition() { return new BubbleTransition(); }

	/**
	 * This may also be used in place of Form.show(). This sets a backCommand into the specified destination Form 
	 * that will return the user to specified starting Form. Then it shows the destination form.
	 * <p>
	 * If, for some reason, you need to get the backCommand from the destination form, you should do so after calling
	 * this method. Any backCommand you get before calling this will be invalid.
	 *  @param destination The destination Form to show
	 * @param transition The transition to use, or null to use the existing transitions in the form.
	 */
	public static void navigateTo(@NotNull final Form destination, @NotNull final Form BackDestination, @Nullable Transition transition) {
		Command backCommand = createBackCommand(BackDestination);
		if (transition != null) {
			Form currentForm = Display.getInstance().getCurrent();
			currentForm.setTransitionOutAnimator(transition);
		}
		destination.show(); // installs a new MenuBar, into which the back command goes...
		destination.setBackCommand(backCommand); // So this needs to be done after the show()!
	}

	@NotNull
	static Command createBackCommand(@NotNull final Form startingForm) {
		return new Command("Back") {
			@Override
			public void actionPerformed(final ActionEvent evt) {
//				Log.p("BackCommand: type:" + evt.getEventType() + " source:" + evt.getSource() + " of " + evt.getSource().getClass() + " Command:" + evt.getCommand() + " of " + evt.getCommand().getClass() + " at (" + evt.getX() + ", " + evt.getY() + ')');
				startingForm.showBack();
			}
		};
	}

	/**
	 * Use this in place of Form.show(). This sets a backCommand into the destination Form that will return the user to
	 * the current form. Then it shows the destination form, using the default transition, which is a slide 
	 * transition unless it has been changed.
	 * (If you've already specified a back command, you don't need to use this method.)
	 * <p>
	 * If, for some reason, you need to get the backCommand from the destination form, you should do so after calling
	 * this method. Any backCommand you get before calling this will be invalid.
	 * @param destination The destination form.
//	 * @see #setDefaultTransition(Transition)  
	 */
	public static void slideTo(@NotNull final Form destination) {
		final Form currentForm = Display.getInstance().getCurrent();
		navigateTo(destination, currentForm, createSlideTransition());
	}

	/**
	 * Use this in place of Form.show(). This sets a backCommand into the destination Form that will return the user to
	 * the current form. Then it shows the destination form, using the default transition, which is a slide
	 * transition unless it has been changed.
	 * (If you've already specified a back command, you don't need to use this method.)
	 * This is the symmetric form of the command, which means that the back button will perform the same transition, but
	 * in the reverse direction.
	 * <p>
	 * If, for some reason, you need to get the backCommand from the destination form, you should do so after calling
	 * this method. Any backCommand you get before calling this will be invalid.
	 *
	 * @param destination The destination form.
	 *                    //	 * @see #setDefaultTransition(Transition)
	 */
	public static void slideToSymmetric(@NotNull final Form destination) {
		slideTo(destination);
		destination.setTransitionOutAnimator(createSlideTransition());
	}
	
	public static void bubbleToSymmetric(@NotNull final Form destination) {
		// prints out stack trace, doesn't work in reverse.
		final Form currentForm = Display.getInstance().getCurrent();
		navigateTo(destination, currentForm, createBubbleTransition());

	}

	/**
	 * Use this in place of Form.show(). This sets a backCommand into the destination Form that will return the user to
	 * the current form. Then it shows the "destination" form, using the default transition, which is a slide 
	 * transition unless it has been changed.
	 * (If you've already specified a back command, you don't need to use this method.)
	 * <p>
	 * If, for some reason, you need to get the backCommand from the destination form, you should do so after calling
	 * this method. Any backCommand you get before calling this will be invalid.
	 *
	 * @param destinationKey The String key for the destination form, specified using the {@link #addForm(String, Form)}
	 *                       method.
	 * @see #addForm(String, Form)
	 */
	public static void slideTo(@NotNull String destinationKey) {
		final Form destination = getForm(destinationKey);
		navigateTo(destination, Display.getInstance().getCurrent(), createSlideTransition());
	}

	/**
	 * Use this in place of Form.show(). This sets a backCommand into the destination Form that will return the user to
	 * the specified return form. Then it shows the destination form, using the default transition, which is a slide 
	 * transition unless it has been changed.
	 * (If you've already specified a back command, you don't need to use this method.)
	 * <p>
	 * If, for some reason, you need to get the backCommand from the destination form, you should do so after calling
	 * this method. Any backCommand you get before calling this will be invalid.
	 * @param destination The destination Form
	 * @param returnFormKey The key name for the form to return to when the user hits "back", specified using {@link #addForm(String, Form)}
	 * @see #addForm(String, Form) 
	 */
	public static void slideTo(@NotNull final Form destination, @NotNull String returnFormKey) {
		final Form startingForm = getForm(returnFormKey);
		navigateTo(destination, startingForm, createSlideTransition());
	}

	/**
	 * Use this in place of Form.show(). This sets a backCommand into the destination Form that will return the user to
	 * the specified return form. Then it shows the destination form, using the default transition, which is a slide
	 * transition unless it has been changed.
	 * (If you've already specified a back command, you don't need to use this method.)
	 * <p>
	 * If, for some reason, you need to get the backCommand from the destination form, you should do so after calling
	 * this method. Any backCommand you get before calling this will be invalid.
	 *
	 * @param destination The destination Form
	 * @see #addForm(String, Form)
	 */
	public static void slideTo(@NotNull final Form destination, @NotNull Form backDestination) {
		navigateTo(destination, backDestination, createSlideTransition());
	}

	/**
	 * Use this in place of Form.show(). This sets a backCommand into the destination Form that will return the user to
	 * the current form. Then it shows the "destination" form, using whatever transitions are specified in the forms.
	 * (If you've already specified a back command, you don't need to use this method.)
	 * <p>
	 * If, for some reason, you need to get the backCommand from the destination form, you should do so after calling
	 * this method. Any backCommand you get before calling this will be invalid.
	 * 
	 * @param destination The destination form
	 */
	public static void navigateTo(@NotNull Form destination) {
		navigateTo(destination, Display.getInstance().getCurrent(), null);
	}

	/**
	 * Use this in place of Form.show(). This sets a backCommand into the destination Form that will return the user to
	 * the current form. Then it shows the "destination" form, using whatever transitions are specified in the forms.
	 * (If you've already specified a back command, you don't need to use this method.)
	 * <p>
	 * If, for some reason, you need to get the backCommand from the destination form, you should do so after calling
	 * this method. Any backCommand you get before calling this will be invalid.
	 *
	 * @param destinationFormKey The key for the destination form, specified using {@link #addForm(String, Form)}
	 * @see #addForm(String, Form)
	 */
	public static void navigateTo(@NotNull String destinationFormKey) {
		final Form destination = getForm(destinationFormKey);
		navigateTo(destination, Display.getInstance().getCurrent(), null);
	}

	/**
	 * Use this in place of Form.show(). This sets a backCommand into the destination Form that will return the user to
	 * the current form. Then it shows the "destination" form, using whatever transitions are specified in the forms.
	 * (If you've already specified a back command, you don't need to use this method.)
	 * <p>
	 * If, for some reason, you need to get the backCommand from the destination form, you should do so after calling
	 * this method. Any backCommand you get before calling this will be invalid.
	 *
	 * @param destination The destination form
	 * @see #addForm(String, Form)
	 * @param forwardTransition The forward transition
	 */
	public static void navigateTo(@NotNull Form destination, @Nullable Transition forwardTransition) {
		navigateTo(destination, Display.getInstance().getCurrent(), forwardTransition);
	}

	/**
	 * Use this in place of Form.show(). This sets a backCommand into the destination Form that will return the user to
	 * the current form. Then it shows the "destination" form, using whatever transitions are specified in the forms.
	 * (If you've already specified a back command, you don't need to use this method.)
	 * <p>
	 * If, for some reason, you need to get the backCommand from the destination form, you should do so after calling
	 * this method. Any backCommand you get before calling this will be invalid.
	 *
	 * @param destinationFormKey The key for the destination form, specified using {@link #addForm(String, Form)}
	 * @param forwardTransition The forward transition
	 * @see #addForm(String, Form)
	 */
	public static void navigateTo(@NotNull String destinationFormKey, @Nullable Transition forwardTransition) {
		final Form destination = getForm(destinationFormKey);
		navigateTo(destination, Display.getInstance().getCurrent(), forwardTransition);
	}

	/**
	 * Use this in place of Form.show(). This sets a backCommand into the destination Form that will return the user to
	 * the current form. Then it shows the "destination" form, using whatever transitions are specified in the forms.
	 * (If you've already specified a back command, you don't need to use this method.)
	 * <p>
	 * If, for some reason, you need to get the backCommand from the destination form, you should do so after calling
	 * this method. Any backCommand you get before calling this will be invalid.
	 *
	 * @param destinationFormKey The key for the destination form, specified using {@link #addForm(String, Form)}
	 * @param forwardTransition The forward transition
	 * @see #addForm(String, Form)
	 */
	public static void navigateTo(@NotNull String destinationFormKey, @NotNull Form backDestination, @Nullable Transition forwardTransition) {
		final Form destination = getForm(destinationFormKey);
		navigateTo(destination, backDestination, forwardTransition);
	}

	public static void addForm(@NotNull String key, @NotNull Form form) {
		formMap.put(key, form);
	}
	
	@NotNull
	public static Form getForm(@NotNull String key) {
		final Form form = formMap.get(key);
		assert form != null : key;
		return form;
	}

	public static void navigateBack(@NotNull final Form form) {
		final Command backCommand = form.getBackCommand();
//		assert backCommand != null;
		Assert.doAssert(backCommand != null);
		Display.getInstance().getCurrent().setTransitionOutAnimator(createReverseSlideTransition());
		backCommand.actionPerformed(new ActionEvent(backCommand, ActionEvent.Type.Done));
	}
	
	public static NavigationCommand createReturningNavigationCommand(
			@NotNull String command, 
			@Nullable Image icon, 
	    int id, 
			@NotNull final Form startingForm)
	{
		return new NavigationCommand(command, icon, id) {
			@Override
			public void actionPerformed(final ActionEvent evt) {
				final Form n = getNextForm();
				if (n != null) {
					n.setBackCommand(createBackCommand(startingForm));
					n.show();
				}
			}
		};
	}
}
