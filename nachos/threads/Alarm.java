package nachos.threads;

import java.util.HashMap;
import java.util.Map;

import nachos.machine.*;

/**
 * Uses the hardware timer to provide preemption, and to allow threads to sleep
 * until a certain time.
 */
public class Alarm {
	HashMap<KThread, Long> threadMap = new HashMap<KThread, Long>();
	
	/**
	 * Allocate a new Alarm. Set the machine's timer interrupt handler to this
	 * alarm's callback.
	 * 
	 * <p>
	 * <b>Note</b>: Nachos will not function correctly with more than one alarm.
	 */
	public Alarm() {
		Machine.timer().setInterruptHandler(new Runnable() {
			public void run() {
				timerInterrupt();
			}
		});
	}

	/**
	 * The timer interrupt handler. This is called by the machine's timer
	 * periodically (approximately every 500 clock ticks). Causes the current
	 * thread to yield, forcing a context switch if there is another thread that
	 * should be run.
	 */
	public void timerInterrupt() {
		long currentTime = Machine.timer().getTime();
		
		for (Map.Entry thread : threadMap.entrySet()) {
			KThread getThread = (KThread) thread.getKey();
			Long getThreadTime = (Long) thread.getValue();
			
			if(getThreadTime <= currentTime && getThread != null) {
				System.out.println("Thread Ready");
				getThread.ready();
				threadMap.remove(getThread);
			}
		}
		KThread.currentThread().yield();
	}

	/**
	 * Put the current thread to sleep for at least <i>x</i> ticks, waking it up
	 * in the timer interrupt handler. The thread must be woken up (placed in
	 * the scheduler ready set) during the first timer interrupt where
	 * 
	 * <p>
	 * <blockquote> (current time) >= (WaitUntil called time)+(x) </blockquote>
	 * 
	 * @param x the minimum number of clock ticks to wait.
	 * 
	 * @see nachos.machine.Timer#getTime()
	 */
	public void waitUntil(long x) {
		if(x <= 0) {
			return;
		}
		
		// for now, cheat just to get something working (busy waiting is bad)
		long wakeTime = Machine.timer().getTime() + x;
		while (wakeTime > Machine.timer().getTime()) {
			KThread.yield();
			
			KThread currentThread = KThread.currentThread();
			threadMap.put(currentThread, wakeTime);
			
			boolean intStatus = Machine.interrupt().disable();
			KThread.sleep();
			Machine.interrupt().restore(intStatus);
		}
		
	}
	
    // Add Alarm testing code to the Alarm class
    public static void alarmTest1() {
    	System.out.println("Begin Alarm test 1, Test for different durations");
    	
		int durations[] = {1000, 10*1000, 100*1000};
		long t0, t1;

		for (int d : durations) {
		    t0 = Machine.timer().getTime();
		    ThreadedKernel.alarm.waitUntil (d);
		    t1 = Machine.timer().getTime();
		    System.out.println ("alarmTest1: waited for " + (t1 - t0) + " ticks");
		    Lib.assertTrue(((t1 - t0) - d) < 500);
		}
    }
    
    public static void alarmTest2() {
    	System.out.println("Begin Alarm test 2, Test for edge cases x <= 0");

    	int x = 0;
    	int y = -1;
    	long t0, t1;
    	
	    t0 = Machine.timer().getTime();
	    ThreadedKernel.alarm.waitUntil(x);
	    t1 = Machine.timer().getTime();
	    System.out.println ("alarmTest2: waited for " + (t1 - t0) + " ticks");
	    

	    t0 = Machine.timer().getTime();
	    ThreadedKernel.alarm.waitUntil(x);
	    t1 = Machine.timer().getTime();
	    System.out.println ("alarmTest2: waited for " + (t1 - t0) + " ticks");
	    
    }
    
    

    // Implement more test methods here ...

    // Invoke Alarm.selfTest() from ThreadedKernel.selfTest()
    public static void selfTest() {
    	System.out.println("Beginning Tests");
    	alarmTest1();
    	alarmTest2();

	// Invoke your other test methods here ...
    }
}
