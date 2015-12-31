package org.eclipse.ui.views.pdf;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.commands.IHandlerListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.views.file.FileView;
import org.eclipse.ui.views.file.IFileViewType;

public class PdfViewScrollHandler implements IHandler{

	private static final String SCROLL_DIRECTION_PARAMETER = "org.eclipse.ui.views.pdf.scrollDirection";//$NON-NLS-1$

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
		if(page!=null){
			String dir=event.getParameter(SCROLL_DIRECTION_PARAMETER);
			if(!dir.isEmpty()){
				int hInc=page.getHorizontalBar().getIncrement();
				int vInc=page.getVerticalBar().getIncrement();
				Point currentOrigin=page.getOrigin();
				int xToSet=currentOrigin.x;
				int yToSet=currentOrigin.y;
				if(xToSet>=0 && yToSet>=0){
					switch (dir.charAt(0)){
					case 'u':yToSet-=vInc;break;
					case 'd':yToSet+=vInc;break;
					case 'l':xToSet-=hInc;break;
					case 'r':xToSet+=hInc;break;
					}
					page.setOrigin(xToSet, yToSet);
				}
			}
		}
		return null;
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