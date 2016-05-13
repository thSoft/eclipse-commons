package org.eclipse.ui.views.pdf;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.commands.IHandlerListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.views.file.FileView;
import org.eclipse.ui.views.file.IFileViewType;

public class PdfViewScrollHandler implements IHandler{

	private PdfViewPage getPage(){
		//handler activation ensures active part is FileView
		FileView view=(FileView)PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActivePart();
		IFileViewType<?> type = view.getType();
		if(type instanceof PdfViewType){
			return ((PdfViewType) type).getPage();
		}else{
			return null;
		}
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		PdfViewPage page = getPage();
		if(page!=null && (event.getTrigger()) instanceof Event){
			int key=((Event)event.getTrigger()).keyCode;
			int hInc=page.getHorizontalBar().getIncrement();
			int vInc=page.getVerticalBar().getIncrement();
			Point currentOrigin=page.getOrigin();
			int xToSet=currentOrigin.x;
			int yToSet=currentOrigin.y;
			switch(key){
				case SWT.ARROW_UP:yToSet=getNewValue(yToSet, -vInc);break;
				case SWT.ARROW_DOWN:yToSet=getNewValue(yToSet, vInc);break;
				case SWT.ARROW_LEFT:xToSet=getNewValue(xToSet, -hInc);break;
				case SWT.ARROW_RIGHT:xToSet=getNewValue(xToSet, hInc);break;
				default:return null;
			}
			if(xToSet!=currentOrigin.x || yToSet!=currentOrigin.y){
				page.setOrigin(xToSet, yToSet);
			}
		}
		return null;
	}

	private int getNewValue(int current, int offset){
		if(current>=0){
			return current+offset;
		}else{
			//no scrolling possible, page already smaller than frame
			return current;
		}
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	@Override
	public boolean isHandled() {
		return getPage()!=null;
	}

	@Override
	public void removeHandlerListener(IHandlerListener handlerListener) {}

	@Override
	public void addHandlerListener(IHandlerListener handlerListener) {}

	@Override
	public void dispose() {}
}