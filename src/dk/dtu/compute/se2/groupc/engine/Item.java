package dk.dtu.compute.se2.groupc.engine;

import javax.media.j3d.BranchGroup;
import javax.media.j3d.Node;
import javax.media.j3d.TransformGroup;

import dk.dtu.compute.se2.groupc.geometry.Line;

public class Item {
	
	private AnimationEngine engine;
	
	private Animation animation;
	private Line line;
	private Node shape;
	
	private BranchGroup bg;
	private TransformGroup transformGroup;
	
	private boolean destroyed; 
	
	Item(AnimationEngine engine, Node shape) {
		this.engine = engine;
		this.shape = shape;
		this.destroyed = false;
	}
	
	void startAnimation(Animation animation, Line line) {
		if (!destroyed) {
			this.line = line;
			if (transformGroup == null) {
				transformGroup = new TransformGroup();
				transformGroup.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
				transformGroup.setCapability(TransformGroup.ALLOW_CHILDREN_EXTEND);
				
				bg = new BranchGroup();
				bg.setCapability(BranchGroup.ALLOW_DETACH);
				bg.addChild(transformGroup);
				transformGroup.addChild(shape);
				BranchGroup scene = engine.getScene();
				scene.addChild(bg);
				animation.init(line, transformGroup);

			} else {
				animation.init(line, transformGroup);
			}
			
			this.animation = animation;
		}
	}
	
	void stopAnimation() {
		if (!destroyed) {
			if (this.animation != null) {
				animation.terminate();
			}

			animation = null;
		}
	}
	
	boolean animationStep(float delta) {
		if (animation != null) {
			boolean terminated = animation.update(delta);
			if (terminated) {
				animation = null;
			}
			return terminated;
		}
		
		return false;
	}
	
	void destroyItem() {
		if (!destroyed) {
			stopAnimation();
			bg.detach();
			
			bg = null;
			destroyed = true;
			engine = null;
			transformGroup = null;
			shape = null;
		}
	}
	
	Node getShape() {
		return shape;
	}
	
	Line getLine() {
		return line;
	}
	
}
