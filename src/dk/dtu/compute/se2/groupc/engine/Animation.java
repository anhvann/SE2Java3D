package dk.dtu.compute.se2.groupc.engine;

import javax.media.j3d.TransformGroup;

import dk.dtu.compute.se2.groupc.geometry.Line;

public abstract class Animation {
	
	public abstract void init(Line line, TransformGroup transformGroup);
	
    public abstract boolean update(float delta);
    
    public abstract boolean isTerminated();
    
    public abstract void terminate();
	
}
