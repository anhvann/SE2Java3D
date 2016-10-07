package dk.dtu.compute.se2.groupc.engine;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.GraphicsConfiguration;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.media.j3d.AmbientLight;
import javax.media.j3d.BoundingSphere;
import javax.media.j3d.Bounds;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.Canvas3D;
import javax.media.j3d.DirectionalLight;
import javax.media.j3d.Node;
import javax.media.j3d.Shape3D;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.swing.JFrame;
import javax.vecmath.Color3f;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;

import com.sun.j3d.utils.behaviors.vp.OrbitBehavior;
import com.sun.j3d.utils.geometry.ColorCube;
import com.sun.j3d.utils.geometry.Sphere;
import com.sun.j3d.utils.universe.SimpleUniverse;

import dk.dtu.compute.se2.groupc.geometry.Geometry;
import dk.dtu.compute.se2.groupc.geometry.Line;
import dk.dtu.compute.se2.groupc.geometry.Point;

public class AnimationEngine {
	
	final private float SCENE_SCALE_FACTOR = 0.01f;
	final private float OBJECT_SCALE_FACTOR = 5.0f;

	private JFrame frame;
	
	// might be used at some later stage, when drawing the tracks
	@SuppressWarnings("unused")
	private Geometry geometry;

	private BranchGroup scene;
	private SimpleUniverse universe;
	
	AnimateBehaviour animateBehaviour;
	
	private Set<Item> items;
	
	// This will allow us object picking (if an 3D shape is picked, we
	// can look up which Item this represents).
	private Map<Node,Item> shape2item;

	public AnimationEngine(JFrame frame) {
		this.frame = frame;
		animateBehaviour = new AnimateBehaviour(this);
	}

	public void initialize(Geometry geometry, Line line) {
	
		this.geometry = geometry;
		items = new HashSet<Item>();
		shape2item = new HashMap<Node,Item>();
		
		Point point = line.getStart();
		Point3d center = null;
		double size = 4000.0d;
		if (point != null) {
			// TODO Note that we might want to add a transformation of the coordinates here
			//      since the geometry editor (by default) uses a left-handed trihedron and
			//      the 3D framework uses a right-handed trihedron
			center = new Point3d(point.getX(), point.getY(), 0.0d);
			size = 10*Math.sqrt(point.getX()*point.getX() + point.getY()*point.getY() );
		} else {
			center = new Point3d(0.0d,0.0d,0.0d);
		}
	
		initializeUniverse(center, size);
				
	}

	public Item createItem() {
		Shape3D shape = new ColorCube(OBJECT_SCALE_FACTOR);
		Item item = new Item(this,shape);
		items.add(item);
		shape2item.put(shape,item);
		
		return item;
	}

	public void startAnimation(Item item, Line line) {
		if (line != null && item != null && items.contains(item)) {
			Move move = new Move();
			item.startAnimation(move, line);
			this.animateBehaviour.addItem(item);
		}
		
	}

	public void stopAnimation(Item item) {
		if (item != null ) {
			item.stopAnimation();
			this.animateBehaviour.removeItem(item);
		}
	}

	public void removeItem(Item item) {
		if (item != null) {
			item.stopAnimation();
			this.animateBehaviour.removeItem(item);
			this.shape2item.remove(item.getShape());
			this.items.remove(item);
			item.destroyItem();
		}		
	}
	
	BranchGroup getScene() {
		return scene;
	}
	
	private void initializeUniverse(Point3d center, double size) {
		if (frame == null) {
			frame = new JFrame();
		    frame.setLayout(new BorderLayout());
            frame.setSize(600,400);
		}
		
		GraphicsConfiguration config =
				SimpleUniverse.getPreferredConfiguration();
		Canvas3D canvas = new Canvas3D(config);
		frame.add(canvas);
		
		universe = new SimpleUniverse(canvas);

		new WindowCloser(frame, canvas, universe);
		
		OrbitBehavior orbBeh = new OrbitBehavior(canvas); //zoom, pan with mouse
		BoundingSphere sceneBounds = new BoundingSphere(center, size);
		orbBeh.setSchedulingBounds(sceneBounds); //boundaries
		universe.getViewingPlatform().setViewPlatformBehavior(orbBeh);
		orbBeh.getViewingPlatform().setNominalViewingTransform();

		// Create an initial scene and attach it to the virtual universe
		scene = initializeSceneGraph(sceneBounds); //set up scene
		
		// create animation behaviour		
		animateBehaviour.setSchedulingBounds(sceneBounds);
		scene.addChild(animateBehaviour);
		scene.setCapability(BranchGroup.ALLOW_CHILDREN_EXTEND);
		scene.setCapability(BranchGroup.ALLOW_CHILDREN_WRITE);
		scene.setCapability(BranchGroup.ALLOW_DETACH);
		scene.setCapability(BranchGroup.ALLOW_CHILDREN_READ);
		
		// Scale and center scene (makes it easier to adjust scene)
		TransformGroup transformGroup2 = new TransformGroup();
		Transform3D transform2 = new Transform3D();
		transformGroup2.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
		transformGroup2.setCapability(TransformGroup.ALLOW_CHILDREN_EXTEND);
		
		transform2.setScale(SCENE_SCALE_FACTOR);
		transform2.setTranslation(new Vector3d(-center.x*SCENE_SCALE_FACTOR,-center.y*SCENE_SCALE_FACTOR,-center.z*SCENE_SCALE_FACTOR));

		transformGroup2.setTransform(transform2);
		BranchGroup bg2 = new BranchGroup();
		bg2.setCapability(BranchGroup.ALLOW_CHILDREN_EXTEND);
		bg2.setCapability(BranchGroup.ALLOW_CHILDREN_WRITE);
		bg2.setCapability(BranchGroup.ALLOW_DETACH);
		bg2.setCapability(BranchGroup.ALLOW_CHILDREN_READ);
		bg2.addChild(transformGroup2);
		universe.addBranchGraph(bg2);
		
		// Add the actual scene
		transformGroup2.addChild(scene);
				
		// make frame visible
	    frame.setVisible(true);
	}	
	
	private BranchGroup  initializeSceneGraph(Bounds bounds)  {
		BranchGroup scene = new BranchGroup();
		
		// Set direct light
		Color3f directColor = new Color3f(1.0f, 1.0f, 1.0f);
		Vector3f direction = new Vector3f(-1.0f, -1.0f, -1.0f);
		DirectionalLight directLight = new DirectionalLight(directColor, direction);
		directLight.setInfluencingBounds(bounds);
		scene.addChild(directLight);
		
		// Set ambient light
		Color3f ambientColor = new Color3f(0.5f, 0.5f, 0.5f);
		AmbientLight ambientLight = new AmbientLight(ambientColor);
		ambientLight.setInfluencingBounds(bounds);
		scene.addChild(ambientLight);
		
		// TODO: here the static objects of the scene could be added
		
		// ... here we just add a grey sphere to the scene indicating the
		// origin of the geometry (
		TransformGroup transformGroup = new TransformGroup();

		Transform3D transform = new Transform3D(); 
		transform.setTranslation(new Vector3d(0,0,0));
		transformGroup.setTransform(transform);
		transformGroup.addChild(new Sphere(OBJECT_SCALE_FACTOR)); //New sphere
		
		BranchGroup bg = new BranchGroup();
		bg.addChild(transformGroup);

        scene.addChild(bg);
        
		return scene;
	}
	
	/**
	 * This is a hack for being able to close a frame. Should
	 * be done in a bit nicer way.
	 * 
	 * @author ekki@dtu.dk
	 *
	 */
	private class WindowCloser implements WindowListener {
		
		private Frame frame;
		private Canvas3D canvas;
		private SimpleUniverse universe;
		
		WindowCloser(Frame frame, Canvas3D canvas, SimpleUniverse universe) {
			this.frame = frame;
			this.canvas = canvas;
			this.universe = universe;
			frame.addWindowListener(this);
		}

		@Override
		public void windowActivated(WindowEvent arg0) {}

		@Override
		public void windowClosed(WindowEvent arg0) {}

		@Override
		public void windowClosing(WindowEvent arg0) {
			canvas.stopRenderer();
			universe.cleanup();
			frame.dispose();
		}

		@Override
		public void windowDeactivated(WindowEvent arg0) {}
		@Override
		public void windowDeiconified(WindowEvent arg0) {}

		@Override
		public void windowIconified(WindowEvent arg0) {}


		@Override
		public void windowOpened(WindowEvent arg0) {}
	}
	
}
