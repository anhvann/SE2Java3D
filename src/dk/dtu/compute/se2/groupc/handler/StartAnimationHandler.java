package dk.dtu.compute.se2.groupc.handler;

import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;

import dk.dtu.compute.se2.groupc.engine.AnimationEngine;
import dk.dtu.compute.se2.groupc.engine.Item;
import dk.dtu.compute.se2.groupc.geometry.Geometry;
import dk.dtu.compute.se2.groupc.geometry.Line;
import dk.dtu.compute.se2.groupc.geometry.Point;

public class StartAnimationHandler extends AbstractHandler implements IHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
	    ISelection selection = HandlerUtil.getActiveWorkbenchWindow(event)
	            .getActivePage().getSelection();
	        if (selection != null & selection instanceof IStructuredSelection) {
	          IStructuredSelection structuredSelection = (IStructuredSelection) selection;
				if (structuredSelection.size() == 1) {
					Object first = structuredSelection.getFirstElement();
					Line line = getLine(structuredSelection.getFirstElement());
					if (line != null) {
						Geometry geometry = getGeometry(line);
						if (geometry != null) {
							AnimationEngine engine = new AnimationEngine(null);
							engine.initialize(geometry, line);
							Item item = engine.createItem();
							engine.startAnimation(item, line);
						}
					} else {
						Point point = getPoint(first);
						if(point != null){
							if(!point.getOut().isEmpty()){
								Line l0 = point.getOut().get(0);
								Geometry geometry = getGeometry(l0);
								AnimationEngine engine = new AnimationEngine(null);
								engine.initialize(geometry, l0);
								for (Line l: point.getOut()){
									System.out.println(point.getOut().size());
									Item item = engine.createItem();
									engine.startAnimation(item, l);
								}
							}
						}
					}
				}
	        }
	        return null;
	}
	
	private boolean enabled = true;
	
	@Override
	public boolean isEnabled() {
		return enabled;
	}
	
	@Override
	public void setEnabled(Object evaluationContext) {
		Object object = ((IEvaluationContext) evaluationContext).getDefaultVariable();
		if (object instanceof List) {
			@SuppressWarnings("rawtypes")
			List list = (List) object;
			if (list.size() == 1) {
				Line line = getLine(list.get(0));
				if (line != null) {
					Geometry geometry = getGeometry(line);
					if (geometry != null) {
						enabled = true;
						return;
					}
				}
			}
		}
		enabled = false;
	}

	private static Line getLine(Object object) {
		if (object instanceof Line) {
			return (Line) object;
		} else if (object instanceof IAdaptable) {
			return ((IAdaptable) object).getAdapter(Line.class);
		}
		return null;
	}
	
	private Point getPoint(Object object) {
		if (object instanceof Point){
			return (Point) object;
		} else if (object instanceof IAdaptable){
			return ((IAdaptable) object).getAdapter(Point.class);
		}
		return null;
	}
	
	private static Geometry getGeometry(Line line) {
		if (line != null) {
			EObject container = line.eContainer();
			if (container instanceof Geometry) {
				return (Geometry) container;
			}
		}
		return null;
	}

}
