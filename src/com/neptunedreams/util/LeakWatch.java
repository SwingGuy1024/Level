package com.neptunedreams.util;

//import java.lang.ref.PhantomReference;
//import java.lang.ref.ReferenceQueue;
//import java.util.HashMap;
//import java.util.Map;
//import com.codename1.io.Log;

/**
 * <p>Created by IntelliJ IDEA.
 * <p>Date: 12/9/16
 * <p>Time: 10:00 PM
 *
 * @author Miguel Mu\u00f1oz
 */
@SuppressWarnings("unused")
public enum LeakWatch {
	watch;
//	
//	LeakWatch() {
//		start();
//	}
//	
//	@NotNull
//	private final ReferenceQueue<Object> queue = new ReferenceQueue<>();
//
//	@NotNull
//	private final Map<PhantomReference<Object>, String> map = new HashMap<>();
//
////	@SuppressWarnings("unused")
////	public void watch(@NotNull Object object) {
////		//noinspection StringConcatenation
////		watch(object, object.getClass().toString() + " @ " + object.hashCode());
////	}
//	
//	public void watch(@NotNull Object object, @NotNull String name) {
//		PhantomReference<Object> reference = new PhantomReference<>(object, queue);
//		//noinspection StringConcatenation
//		String keyedName = name + " # " + reference.hashCode();
//		map.put(reference, keyedName);
//		Log.p("*** watch " + keyedName);
//		System.gc();
//	}
//	
//	private void start() {
//		Thread thread = new Thread(this::run,"LeakWatch");
//		thread.setDaemon(true);
//		thread.start();
//	}
//	
//	private void run() {
//		//noinspection InfiniteLoopStatement
//		while (true) {
//			PhantomReference<Object> reference = null;
//			try {
//				//noinspection unchecked
//				reference = (PhantomReference<Object>) queue.remove();
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			}
//			String name = map.remove(reference);
//			//noinspection StringConcatenationInLoop
//			Log.p("                                        GC " + name);
//		}
//	}
//	
//	public void showAll() {
//		System.gc();
//		Log.p("Total of " + map.size() + " watches.");
//		for (PhantomReference<Object> key: map.keySet()) {
//			//noinspection StringConcatenationInLoop
//			Log.p("Watching " + map.get(key));
//		}
//		Log.p("----");
//	}
}
