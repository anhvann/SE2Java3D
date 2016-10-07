package dk.dtu.compute.se2.groupc.engine;

import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

import javax.media.j3d.Behavior;
import javax.media.j3d.WakeupOnElapsedFrames;

import dk.dtu.compute.se2.groupc.geometry.Line;
import dk.dtu.compute.se2.groupc.geometry.Point;



public class AnimateBehaviour extends Behavior {
	
	private AnimationEngine engine; 
	
	private Set<Item> runningAnimations;
	
	// Wakeup condition: wakeup on every frame
	private WakeupOnElapsedFrames wakeUpOnEveryFrame = new WakeupOnElapsedFrames(0);
	
	private long timeLastFrame;

	public AnimateBehaviour(AnimationEngine engine) {
		super();
		this.engine = engine;
	    setEnable(true);
	    runningAnimations = new HashSet<Item>();
	}

	@Override
	public void initialize() {
	    wakeupOn(wakeUpOnEveryFrame);
		timeLastFrame = System.currentTimeMillis();
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void processStimulus(Enumeration arg0) {
		long timeThisFrame = System.currentTimeMillis();
		long timeDelta = timeThisFrame - timeLastFrame; 
		
		// update animations only, if at least 5ms have elapsed
		if (timeDelta < 5) {
		    wakeupOn(wakeUpOnEveryFrame);
			return;
		}
		
		float delta = timeDelta * 0.001f;
		System.out.println("Elapsed time: " + delta*1000 +"ms");
		
		Collection<Item> terminatedItems = new HashSet<Item>();
		for (Item item: runningAnimations) {
			if (item.animationStep(delta)) {
				terminatedItems.add(item);
			}
		}
		
		for (Item item: terminatedItems) {
			runningAnimations.remove(item);
		}
		
		if (!terminatedItems.isEmpty()) {
			
			// TODO this would be the point where the simulator would need to be notified
			//      instead we start a new animation on one of the lines that start at
			//      the end point of the current animation (which is chosen at random).
			
			for (Item item: terminatedItems) {
				engine.removeItem(item);
				Line line = item.getLine();
				Point end = line.getEnd();
				if (end != null && !end.getOut().isEmpty()) {
					int i = (int) Math.floor(Math.random()*end.getOut().size());
					Line next =end.getOut().get(i);
					Item newItem = engine.createItem();
					engine.startAnimation(newItem, next);
				}
			}
		}
		
		// issue execution of this behaviour at next frame
	    wakeupOn(wakeUpOnEveryFrame);
	    timeLastFrame = timeThisFrame;
	}
	
	void addItem(Item item) {
		runningAnimations.add(item);
	}
	
	void removeItem(Item item) {
		runningAnimations.remove(item);
	}

}
