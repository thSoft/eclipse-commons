package org.eclipse.ui.views.pdf;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.commands.IHandlerListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.views.file.FileView;
import org.eclipse.ui.views.file.IFileViewType;

public class PdfViewScrollHandler implements IHandler{

	public static void fixNegativeOriginMouseScrollBug(PdfViewPage page){
		exchangeScrollListener(page, true);
		exchangeScrollListener(page, false);
	}

	private static void exchangeScrollListener(final PdfViewPage page, final boolean horizontalScrollBar){
		ScrollBar scrollBar=horizontalScrollBar?page.getHorizontalBar():page.getVerticalBar();
		Listener[] listeners = scrollBar.getListeners(SWT.Selection);
		if(listeners.length==1){
			final Listener originalListener=listeners[0];
			scrollBar.removeListener(SWT.Selection, originalListener);
			scrollBar.addListener(SWT.Selection, new Listener() {

				@Override
				public void handleEvent(Event event) {
					//ScrolledComposit does not expect negative origin values
					//so mouse scroll event sets origin to minimal selection (hscroll/vscroll)
					//this has to be prevented
					Point origin = page.getOrigin();
					int originValueToCheck=horizontalScrollBar?origin.x:origin.y;
					if(originValueToCheck>=0){
						originalListener.handleEvent(event);
					}
				}
			});
		}
	}

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
			switch(key){
				case SWT.ARROW_UP:scroll(page.getVerticalBar(),true);break;
				case SWT.ARROW_DOWN:scroll(page.getVerticalBar(),false);break;
				case SWT.ARROW_LEFT:scroll(page.getHorizontalBar(),true);break;
				case SWT.ARROW_RIGHT:scroll(page.getHorizontalBar(),false);break;
				default:return null;
			}
		}
		return null;
	}

	private void scroll(ScrollBar bar, boolean up){
		int inc = up?-bar.getIncrement():bar.getIncrement();
		bar.setSelection(bar.getSelection()+inc);
		Listener[] listeners = bar.getListeners(SWT.Selection);
		for (Listener listener : listeners) {
			listener.handleEvent(new Event());
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